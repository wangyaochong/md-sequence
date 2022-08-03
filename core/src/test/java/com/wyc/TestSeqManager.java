package com.wyc;

import com.wyc.component.SeqManagerNew;
import com.wyc.model.PlainSeqSegment;
import com.wyc.model.PlainSeqSegmentResult;
import com.wyc.model.Result;
import com.wyc.util.UtilJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
public class TestSeqManager {
    @Autowired SeqManagerNew seqManager;

    @Test
    public void testStartServe() throws InterruptedException {
        boolean seq = seqManager.startServe("seq");
        System.out.println(seq);
        Thread.sleep(10000000);
    }

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(10000000);
    }

    @Test
    public void testStartServeAndNextOnce() {
        boolean seq = seqManager.startServe("seq");
        int count = 999;
        PlainSeqSegmentResult seq2 = seqManager.next("seq", count);
        List<PlainSeqSegment> segmentList = seq2.getSegmentList();
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < segmentList.size(); i++) {
            for (long j = segmentList.get(i).getStart(); j < segmentList.get(i).getEnd(); j++) {
                result.add(j);
            }
        }
        System.out.println(result.size());
        System.out.println(result);

    }

    @Test
    public void testStartServeAndNext() throws InterruptedException {
        seqManager.startServe("seq");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int taskCount = 10;
        CountDownLatch latch = new CountDownLatch(taskCount);
        List<Long> number = new Vector<>();
//        List<PlainSeqSegmentResult> segmentList = new Vector<>();
        AtomicInteger count = new AtomicInteger(0);
        int getCount = 100;
        for (int i = 0; i < taskCount; i++) {
            executorService.execute(() -> {
                while (System.currentTimeMillis() - start < 10000) {
                    PlainSeqSegmentResult seq = seqManager.next("seq", getCount);
                    //add from result.data.start to result.data.end to list
//                    segmentList.add(nextResult.getData());
                    count.getAndAdd(getCount);
//                    for (PlainSeqSegment plainSeqSegment : nextResult.getData().getSegmentList()) {
//                        for (Long j = plainSeqSegment.getStart(); j < plainSeqSegment.getEnd(); j++) {
//                            number.add(j);
//                        }
//                    }
                }
                latch.countDown();
            });
        }
        latch.await();
//        number.sort((o1, o2) -> (int) (o1 - o2));
        System.out.println(number.size());
//        System.out.println(segmentList.size());
        System.out.println("count:" + count.get());
//        segmentList.sort((o1, o2) -> (int) (o1.getStart() - o2.getStart()));
        //display number
        System.out.println("end");
    }

    public static void main(String[] args) {
        System.out.println(Long.MAX_VALUE);
    }
}
