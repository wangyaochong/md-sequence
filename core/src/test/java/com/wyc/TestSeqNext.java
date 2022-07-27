package com.wyc;
import com.wyc.component.SeqManager;
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
@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
public class TestSeqNext {
    @Autowired SeqManager seqManager;

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
        Result<PlainSeqSegmentResult> seq1 = seqManager.next("seq", count);
        System.out.println(UtilJson.toJson(seq1));
        List<PlainSeqSegment> segmentList = seq1.getData().getSegmentList();
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
        List<PlainSeqSegmentResult> segmentList = new Vector<>();
        for (int i = 0; i < taskCount; i++) {
            executorService.execute(() -> {
                while (System.currentTimeMillis() - start < 100) {
                    Result<PlainSeqSegmentResult> nextResult = seqManager.next("seq", 9999);
                    //add from result.data.start to result.data.end to list
                    segmentList.add(nextResult.getData());
                    for (PlainSeqSegment plainSeqSegment : nextResult.getData().getSegmentList()) {
                        for (Long j = plainSeqSegment.getStart(); j < plainSeqSegment.getEnd(); j++) {
                            number.add(j);
                        }
                    }
                }
                latch.countDown();
            });
        }
        latch.await();
        number.sort((o1, o2) -> (int) (o1 - o2));
//        segmentList.sort((o1, o2) -> (int) (o1.getStart() - o2.getStart()));
        //display number
        System.out.println("end");
    }
}
