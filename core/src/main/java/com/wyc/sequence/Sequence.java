package com.wyc.sequence;

import com.wyc.component.InstanceComponent;
import com.wyc.enums.EnumSeqType;
import com.wyc.exceptions.OneNodeSequenceServingByOtherException;
import com.wyc.exceptions.SeqNameNotFoundInDbException;
import com.wyc.generated.entity.Node;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.mapper.SeqCoreMapper;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.OperatingSeqSegment;
import com.wyc.model.PlainSeqSegmentResult;
import com.wyc.model.SeqCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public class Sequence {
    protected ISeqInfoService seqInfoService;
    protected SeqCoreMapper seqCoreMapper;
    protected INodeService nodeService;
    protected PlatformTransactionManager transactionManager;
    protected SeqCache seqCache = new SeqCache();

    //同一个时刻，只有一个线程能seqCache
    protected ReentrantLock updateSeqCacheLock = new ReentrantLock(true);

    //查询同一个序列，不一定是顺序执行的，主要是需要用到nextCondition用于通知
    protected ReentrantLock waitingFetchLock = new ReentrantLock(true);
    protected Condition waitingFetchCondition = waitingFetchLock.newCondition();
    //防止并发查完数据库后，修改end值的时候，先后顺序错乱
    protected ExecutorService fetchService;

    protected ReentrantLock nextLock = new ReentrantLock(true);

    protected SeqInfo seqInfo;
    protected Node node;

    //正在拉取的数量，因为拉取是异步的，防止过量拉取
    AtomicInteger fetchingCount = new AtomicInteger(0);

    public void init(String seqName,
                     ISeqInfoService seqInfoService,
                     INodeService nodeService,
                     SeqCoreMapper seqCoreMapper,
                     PlatformTransactionManager transactionManager,
                     ExecutorService executorService) {
        this.nodeService = nodeService;
        this.seqInfoService = seqInfoService;
        this.seqCoreMapper = seqCoreMapper;
        this.transactionManager = transactionManager;
        this.fetchService = executorService;
        this.seqInfo = seqInfoService.getByName(seqName);
        this.node = nodeService.getByIpAndPort(InstanceComponent.getIp(), InstanceComponent.getPort());
        if (this.seqInfo == null) {
            throw new SeqNameNotFoundInDbException(String.format("seqName=%s", seqName));
        }
        checkCanServeSequence();
    }

    protected OperatingSeqSegment getSegmentFromDb(int count) {

        //检查能否服务序列，如果不能服务，则内层会抛出异常
        checkCanServeSequence();


        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            SeqCore seqCore = seqCoreMapper.getByIdForUpdate(seqInfo.getCoreId());
            if (seqCore.getLastMax() < seqCache.getMax()) {//可能等于，但是不可能小于
                throw new RuntimeException("lastMax is less than max");
            }
            long start = seqCore.getLastMax();
            long maxUpdated = start + count;
            seqCore.setLastMax(maxUpdated);
            seqCoreMapper.updateById(seqCore);
            transactionManager.commit(status);
            return new OperatingSeqSegment(new AtomicLong(start), new AtomicLong(maxUpdated));
        } catch (Exception e) {
            log.error(String.format("getSegmentFromDb error, seqName=%s", seqInfo.getName()), e);
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

    public PlainSeqSegmentResult next(Integer count) {
        try {
            nextLock.lock();
            checkCanServeSequence();
            if (needFetch(count)) {//判断是否需要提前拉取
                //如果需要拉取的大于总数，则提交一个拉取任务该数量的任务
                int fetchCount = Math.max(count, seqInfo.getServerCacheSize());
                fetchingCount.addAndGet(fetchCount);//添加正在拉取的数量
                fetchService.execute(() -> fetchTask().accept(fetchCount));//如果count大于cacheSize，则拉取count数，比如10万个
            }
            while (seqCache.getTotal() < count) {
                //log.info("seqCache.getTotal() < count await,count={}", count);
                waitingFetchLock.lock();
                boolean await = waitingFetchCondition.await(3L, TimeUnit.SECONDS);//对于单节点切换的情况，有可能永远都不会苏醒，这里需要有超时机制
                waitingFetchLock.unlock();
                //log.info("seqCache.getTotal() < count awake,count={}", count);
                if (!await) {
                    //如果超时没有收到缓存拉取完成的通知，则说明需要检查节点是否切换
                    checkCanServeSequence();
                }
            }
            PlainSeqSegmentResult plainSequenceResult = seqCache.getPlainSequenceResult(count);
            plainSequenceResult.setClientCacheSize(seqInfo.getClientCacheSize());
            return plainSequenceResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            nextLock.unlock();
        }
    }

    public boolean needFetch(int count) {
        //如果序列缓存小于服务端缓存的一半，则需要拉取缓存
        //需要加上正在拉取的缓存数量，防止过量拉取
        return seqCache.getTotal() - count + fetchingCount.get() < seqInfo.getServerCacheSize();
    }

    public Consumer<Integer> fetchTask() {
        return (count) -> {

            //加锁主要是为了防止多次拉取任务提交的情况下，拉取结果乱序的问题
            updateSeqCacheLock.lock();

            try {
                // todo 这个锁可能可以去掉，因为数据库操作不可能比内存操作还要快的
                log.info("fetchTask start, seqName={}, count={}, get lock", seqInfo.getName(), count);
                seqCache.addSegment(getSegmentFromDb(count));
                log.info("fetchTask end, seqName={}, count={}", seqInfo.getName(), count);
                waitingFetchLock.lock();
                waitingFetchCondition.signalAll();//更新完segment之后，通知获取数据
                waitingFetchLock.unlock();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                updateSeqCacheLock.unlock();
                fetchingCount.addAndGet(-count);//减去正在拉取的数量
                if (fetchingCount.get() < 0) {
                    fetchingCount.set(0);
                }
            }
        };
    }

    protected void checkCanServeSequence() {
        //如果是OneNodeSequence，则每次都要检查节点是否变化，如果变化，则抛异常
        if (EnumSeqType.OneNodeSequence.name().equals(seqInfo.getType())) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            TransactionStatus status = transactionManager.getTransaction(def);
            try {
                SeqCore seqCore = seqCoreMapper.getByIdForUpdate(seqInfo.getCoreId());
                if (seqCore.getNodeId() == null) {
                    seqCore.setNodeId(node.getId());
                    seqCoreMapper.updateById(seqCore);
                }
                if (!node.getId().equals(seqCore.getNodeId())) {
                    throw new OneNodeSequenceServingByOtherException(String.format("seqName=%s", seqInfo.getName()));
                }
                //如果OneNodeSequence没有设置节点，则可以提供服务，则需要检查是否是第一个节点
                transactionManager.commit(status);
            } catch (Exception e) {
                transactionManager.rollback(status);
                throw e;
            }
        }
    }
}
