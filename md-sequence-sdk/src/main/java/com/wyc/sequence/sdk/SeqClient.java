package com.wyc.sequence.sdk;

import com.wyc.sequence.base.model.PlainSeqSegment;
import com.wyc.sequence.base.model.PlainSeqSegmentResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


public class SeqClient implements ISeqClient {
    protected static final int processorCount = Runtime.getRuntime().availableProcessors();
    protected ExecutorService fetchService = Executors.newFixedThreadPool(processorCount, new ThreadFactory() {
        AtomicLong count = new AtomicLong(0);

        @Override public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "fetchService-" + count.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    });
    protected List<String> serverList;
    protected ConcurrentHashMap<String, Sequence> seqMap = new ConcurrentHashMap<>();

    public SeqClient(List<String> serverList) {
        this.serverList = serverList;
    }


    public Long next(String seqName) {
        Sequence sequence = seqMap.get(seqName);
        if (sequence == null) {
            sequence = new Sequence(seqName, serverList, fetchService);
            seqMap.put(seqName, sequence);
        }

        PlainSeqSegmentResult next = sequence.next(1);
        for (int i = 0; i < next.getSegmentList().size(); i++) {
            PlainSeqSegment plainSeqSegment = next.getSegmentList().get(i);
            if (!plainSeqSegment.getStart().equals(plainSeqSegment.getEnd())) {
                return plainSeqSegment.getStart();
            }
        }
        throw new RuntimeException("no available seq");
    }

    public SeqIterator next(String seqName, Integer count) {
        Sequence sequence = seqMap.get(seqName);
        if (sequence == null) {
            sequence = new Sequence(seqName, serverList, fetchService);
            seqMap.put(seqName, sequence);
        }

        PlainSeqSegmentResult next = sequence.next(count);
        return new SeqIterator(next.getSegmentList());
    }


    public static void main(String[] args) {
//        List<String> serverList1 = Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081");
        List<String> serverList1 = Arrays.asList("127.0.0.1:8081");
        SeqClient seqClient = new SeqClient(serverList1);
        Long test = seqClient.next("seq");
        for (int i = 0; i < 100; i++) {
            Long next = seqClient.next("seq");
        }

        long start = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() < start + 1000) {
            seqClient.next("seq");
            count++;
        }
        System.out.println(count);
    }

}
