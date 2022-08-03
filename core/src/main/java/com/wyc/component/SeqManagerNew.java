package com.wyc.component;

import com.wyc.generated.mapper.SeqCoreMapper;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.PlainSeqSegmentResult;
import com.wyc.sequence.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class SeqManagerNew implements InitializingBean {
    static final int processorCount = Runtime.getRuntime().availableProcessors();
    ExecutorService fetchService = Executors.newFixedThreadPool(processorCount, new ThreadFactory() {
        AtomicLong count = new AtomicLong(0);

        @Override public Thread newThread(Runnable r) {
            return new Thread(r, "fetchService-" + count.incrementAndGet());
        }
    });
    ScheduledExecutorService clearService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        AtomicLong count = new AtomicLong(0);

        @Override public Thread newThread(Runnable r) {
            return new Thread(r, "clearService-" + count.incrementAndGet());
        }
    });

    @Autowired ISeqInfoService seqInfoService;
    @Autowired SeqCoreMapper seqCoreMapper;
    @Autowired INodeService nodeService;
    @Autowired PlatformTransactionManager transactionManager;
    Map<String, Sequence> servingSequenceMap = new ConcurrentHashMap<>();
    ReentrantLock startServerLock = new ReentrantLock();

    public boolean startServe(String seqName) {
        startServerLock.lock();
        try {
            Sequence sequence = new Sequence();
            sequence.init(seqName, seqInfoService, nodeService, seqCoreMapper, transactionManager, fetchService);
            servingSequenceMap.put(seqName, sequence);
            return true;
        } catch (Exception e) {
            throw e;
        } finally {
            startServerLock.unlock();
        }
    }


    public PlainSeqSegmentResult next(String seqName, Integer count) {
        if (!servingSequenceMap.containsKey(seqName)) {
            boolean b = startServe(seqName);
            if (!b) {
                throw new RuntimeException("start server error");
            }
        }
        Sequence sequence = servingSequenceMap.get(seqName);
        return sequence.next(count);
    }

    @Override public void afterPropertiesSet() throws Exception {
//        clearService.scheduleWithFixedDelay(() -> {
//            servingSequenceMap.clear();
//        }, 0, 1, TimeUnit.HOURS);//一个小时执行一次清除缓存的工作，可以确保清除异常状态的缓存
    }
}
