package com.wyc.sequence.base;

import com.wyc.exceptions.SeqNameNotFoundInDbException;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.mapper.SeqCoreMapper;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.OperatingSeqSegment;
import com.wyc.model.PlainSeqSegmentResult;
import com.wyc.model.Result;
import com.wyc.model.SeqCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public abstract class Sequence {
    protected ISeqInfoService seqInfoService;
    protected ISeqCoreService seqCoreService;
    protected SeqCoreMapper seqCoreMapper;
    protected PlatformTransactionManager transactionManager;
    protected SeqCache seqCache = new SeqCache();

    //同一个时刻，只有一个线程能更新数据库
    protected ReentrantLock updateSegmentLock = new ReentrantLock();

    //查询同一个序列，不一定是顺序执行的，主要是需要用到nextCondition用于通知
    protected ReentrantLock waitingFetchLock = new ReentrantLock();
    protected Condition waitingFetchCondition = waitingFetchLock.newCondition();
    //防止并发查完数据库后，修改end值的时候，先后顺序错乱
    protected ExecutorService fetchService;

    protected ReentrantLock nextLock = new ReentrantLock();

    protected SeqInfo seqInfo;

    public void init(String seqName,
                     ISeqInfoService seqInfoService,
                     ISeqCoreService seqCoreService,
                     SeqCoreMapper seqCoreMapper,
                     PlatformTransactionManager transactionManager, ExecutorService executorService) {
        this.seqInfoService = seqInfoService;
        this.seqCoreService = seqCoreService;
        this.seqCoreMapper = seqCoreMapper;
        this.transactionManager = transactionManager;
        this.fetchService = executorService;
        this.seqInfo = seqInfoService.getByName(seqName);
        if (this.seqInfo == null) {
            throw new SeqNameNotFoundInDbException(String.format("seqName=%s", seqName));
        }
        checkCanServeSequence();
    }

    protected OperatingSeqSegment getSegmentFromDb(int count) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            SeqCore seqCore = seqCoreMapper.getByIdForUpdate(seqInfo.getCoreId());
            if (seqCore.getLastMax() < seqCache.getMax()) {//可能等于，但是不可能小于
                throw new RuntimeException("lastMax is less than max");
            }
            long start = seqCore.getLastMax();
            seqCore.setLastMax(seqCore.getLastMax() + count);
            seqCoreService.updateById(seqCore);
            transactionManager.commit(status);
            return new OperatingSeqSegment(new AtomicLong(start), new AtomicLong(start + count));
        } catch (Exception e) {
            log.error(String.format("getSegmentFromDb error, seqName=%s", seqInfo.getName()), e);
            transactionManager.rollback(status);
            return null;
        }
    }

    public Result<PlainSeqSegmentResult> next(Integer count) {
        try {
            checkCanServeSequence();
            nextLock.lock();
            if (needFetch(count)) {//判断是否需要提前拉取
                //如果需要拉取的大于总数，则提交一个拉取任务该数量的任务
                fetchService.execute(() -> fetchTask().accept(Math.max(count, seqInfo.getServerCacheSize())));//如果count大于cacheSize，则拉取count数，比如10万个
            }
            while (seqCache.getTotal() < count) {
                log.info("seqCache.getTotal() < count await,count={}", count);
                //todo 检查超时是否是因为节点切换导致，如果是，则需要返回错误了并且不能返回Segment了
                waitingFetchLock.lock();
                boolean await = waitingFetchCondition.await(5L, TimeUnit.SECONDS);//对于单节点切换的情况，有可能永远都不会苏醒，这里需要有超时机制
                waitingFetchLock.unlock();
                log.info("seqCache.getTotal() < count awake,count={}", count);
            }
            return Result.success(seqCache.getPlainSequenceResult(count));
        } catch (Exception e) {
            log.error(String.format("next error, seqName=%s", seqInfo.getName()), e);
            return Result.error(e.getMessage());
        } finally {
            nextLock.unlock();
        }
    }

    public boolean needFetch(int count) {
        //如果序列缓存小于服务端缓存的一半，则需要拉取缓存
        return seqCache.getTotal() - count < seqInfo.getServerCacheSize() / 2;
    }

    public Consumer<Integer> fetchTask() {
        return (count) -> {
            updateSegmentLock.lock();
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
                updateSegmentLock.unlock();
            }
        };
    }

    protected void checkCanServeSequence() {//默认是可以给序列服务的，但是全局唯一的，则需要另外检验
        //todo 对于MultiNode需要检查，可能要抛异常的
    }
}
