package com.wyc.component;

import com.wyc.enums.EnumSeqType;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.mapper.SeqCoreMapper;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

@Component
@Slf4j
public class SeqManager implements InitializingBean {
    //    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    static final int processorCount = Runtime.getRuntime().availableProcessors();
    ExecutorService scanService = Executors.newFixedThreadPool(processorCount, new ThreadFactory() {
        AtomicLong count = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "scanService-" + count.incrementAndGet());
        }
    });
    ExecutorService fetchService = Executors.newFixedThreadPool(processorCount, new ThreadFactory() {
        AtomicLong count = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "fetchService-" + count.incrementAndGet());
        }
    });
    @Autowired ISeqInfoService seqInfoService;
    @Autowired ISeqCoreService seqCoreService;
    @Autowired SeqCoreMapper seqCoreMapper;

    //队列尺寸是10个segment，
    //对于sdk的话，队列尺寸是3，一次取一个segment分成2个区间，一个segment的一半开始拉取下一个
    ConcurrentHashMap<String, SeqBuffer> seqBufferMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, SeqInfo> seqInfoMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ReentrantLock> seqUpdateEndLockMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ReentrantLock> seqGetNextLockMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Condition> seqNextConditionMap = new ConcurrentHashMap<>();

    //一定要有个独立的servingSequenceSet，因为只有全局底层的序列，需要校验
    @Getter
    Set<SeqInfo> servingSequenceSet = Collections.newSetFromMap(new ConcurrentHashMap<>());//服务中的序列列表
    Set<SeqInfo> fetchingSequenceSet = Collections.newSetFromMap(new ConcurrentHashMap<>());//服务中的序列列表

    ReentrantLock scanLock = new ReentrantLock();
    Condition scanCondition = scanLock.newCondition();
    @Autowired PlatformTransactionManager transactionManager;

    int serverCacheMultiplier = 10;//服务端的缓存是客户端缓存的10倍

    private OperatingSeqSegment getSegmentFromDb(String seqName, int count) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            SeqCore seqCore = seqCoreMapper.getByIdForUpdate(seqInfoMap.get(seqName).getCoreId());
            long start = seqCore.getLastMax();
            seqCore.setLastMax(seqCore.getLastMax() + count);
            seqCoreService.updateById(seqCore);
            transactionManager.commit(status);
            return new OperatingSeqSegment(new AtomicLong(start), new AtomicLong(start + count));
        } catch (Exception e) {
            log.error(String.format("startServe error, seqName=%s", seqName), e);
            transactionManager.rollback(status);
            return null;
        }

    }

    BiConsumer<String, Integer> fetchTask = (seqName, count) -> {//定义拉取序列的任务，一次拉取多少个

        // todo 这个锁可能可以去掉，因为数据库操作不可能比内存操作还要快的
        log.info("fetchTask start, seqName={}, count={}", seqName, count);
        seqUpdateEndLockMap.get(seqName).lock();
        log.info("fetchTask start, seqName={}, count={}, get lock", seqName, count);

        try {
            //这个地方已经通过数据库加锁了，所以锁不用再加锁了
            SeqBuffer seqBuffer = seqBufferMap.get(seqName);
            seqBuffer.addSegment(getSegmentFromDb(seqName, count));
        } catch (Exception e) {
            log.error(String.format("fetchTask error, seqName=%s,count=%s", seqName, count), e);
        } finally {
            seqUpdateEndLockMap.get(seqName).unlock();
            fetchingSequenceSet.remove(seqInfoMap.get(seqName));//从拉取队列中移除
            scanLock.lock();
            scanCondition.signalAll();
            scanLock.unlock();
            seqGetNextLockMap.get(seqName).lock();
            seqNextConditionMap.get(seqName).signalAll();
            seqGetNextLockMap.get(seqName).unlock();
            log.info("finally fetchTask end, seqName={}, count={}", seqName, count);
        }
    };

    public boolean startServe(String seqName) {
        seqInfoMap.putIfAbsent(seqName, seqInfoService.getByName(seqName));
        seqGetNextLockMap.putIfAbsent(seqName, new ReentrantLock());
        seqNextConditionMap.putIfAbsent(seqName, seqGetNextLockMap.get(seqName).newCondition());
        seqUpdateEndLockMap.putIfAbsent(seqName, new ReentrantLock());
        servingSequenceSet.add(seqInfoMap.get(seqName));
        seqBufferMap.putIfAbsent(seqName, new SeqBuffer());
        OperatingSeqSegment segmentFromDb = getSegmentFromDb(seqName, seqInfoMap.get(seqName).getCacheSize() * serverCacheMultiplier);
        seqBufferMap.get(seqName).addSegment(segmentFromDb);
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        scanService.execute(() -> {//提交一个永远执行的扫描任务
            while (true) {
                boolean needScan = false;
                for (Map.Entry<String, SeqBuffer> entry : seqBufferMap.entrySet()) {
                    if (!fetchingSequenceSet.contains(seqInfoMap.get(entry.getKey())) && entry.getValue().getTotal() < (long) seqInfoMap.get(entry.getKey()).getCacheSize() * serverCacheMultiplier / 2) {//只要用掉了缓存，就拉取到满
                        //这个地方提交任务是为了防止死循环，cpu占用高，实际上可能有多个线程同时执行一个序列的任务
                        fetchingSequenceSet.add(seqInfoMap.get(entry.getKey()));
                        fetchService.execute(() -> fetchTask.accept(entry.getKey(), seqInfoMap.get(entry.getKey()).getCacheSize() * serverCacheMultiplier));
                    }
                }
                if (!needScan) {//如果都是满的，就等待
                    try {
                        scanLock.lock();
                        scanCondition.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        scanLock.unlock();
                    }
                }
            }
        });
    }

    private boolean checkServingSeq(String seqName) {
        SeqInfo seqInfo = seqInfoMap.get(seqName);
        if (seqInfo == null) {
            seqInfo = seqInfoService.getByName(seqName);
            seqInfoMap.put(seqName, seqInfo);//序列基本信息
        }
        if (EnumSeqType.NumberBasedUnique.name().equals(seqInfo.getType()) ||
                EnumSeqType.TimeBasedUnique.name().equals(seqInfo.getType())) {
            return true;//如果只是唯一，则可以提供服务
        } else {//如果是另外两种类型，则需要检查servingList
            return checkServingSequenceSet(seqName);
        }
    }

    public boolean checkServingSequenceSet(String seqName) {
        //检查servingSequenceSet中是否含有seqName的SeqInfo
        return servingSequenceSet.stream().anyMatch(seqInfo -> seqInfo.getName().equals(seqName));
    }

    public Result<PlainSeqSegmentResult> next(String seqName, Integer count) {
        //先检查能否为该序列服务，对于全局递增的，只有一个节点能服务
        //对于普通唯一的，所有节点都能服务
        int cacheSize = seqInfoMap.get(seqName).getCacheSize() * serverCacheMultiplier;
        if (!checkServingSeq(seqName)) {
            return Result.error("can't serve seqName:" + seqName);
        }
        ReentrantLock lock = seqGetNextLockMap.get(seqName);
        lock.lock();
        try {
            SeqBuffer seqBuffer = seqBufferMap.get(seqName);
            if (seqBuffer.getTotal() - count < cacheSize / 2) {
                //如果需要拉取的大于总数，则提交一个拉取任务该数量的任务
                fetchService.execute(() -> fetchTask.accept(seqName, Math.max(count, cacheSize)));//如果count大于cacheSize，则拉取count数，比如10万个
            }
            while (seqBuffer.getTotal() < count) {
                log.info("seqBuffer.getTotal().get()={}", seqBuffer.getTotal());
                seqNextConditionMap.get(seqName).await();
            }
            return Result.success(seqBuffer.getPlainSequenceResult(count));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
