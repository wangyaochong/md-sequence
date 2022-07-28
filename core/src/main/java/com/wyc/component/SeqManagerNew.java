package com.wyc.component;

import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.mapper.SeqCoreMapper;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.PlainSeqSegmentResult;
import com.wyc.model.Result;
import com.wyc.sequence.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class SeqManagerNew {
    static final int processorCount = Runtime.getRuntime().availableProcessors();
    ExecutorService fetchService = Executors.newFixedThreadPool(processorCount, new ThreadFactory() {
        AtomicLong count = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "fetchService-" + count.incrementAndGet());
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
            SeqInfo seqInfo = seqInfoService.getByName(seqName);
            Sequence sequence = new Sequence();
            sequence.init(seqName, seqInfoService, nodeService, seqCoreMapper, transactionManager, fetchService);
            servingSequenceMap.put(seqName, sequence);
            return true;
        } catch (Exception e) {
            log.error("start server error", e);
            return false;
        } finally {
            startServerLock.unlock();
        }
    }


    public Result<PlainSeqSegmentResult> next(String seqName, Integer count) {
        if (!servingSequenceMap.containsKey(seqName)) {
            boolean b = startServe(seqName);
            if (!b) {
                return Result.error("start server error");
            }
        }
        Sequence sequence = servingSequenceMap.get(seqName);
        return Result.success(sequence.next(count));
    }
}
