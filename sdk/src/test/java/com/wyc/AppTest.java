package com.wyc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testOne(){
        SeqClient seqClient=new SeqClient(Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081"));
        Long seq = seqClient.next("seq");
    }
    @Test
    public void test() throws InterruptedException {
        List<SeqClient> seqClientList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seqClientList.add(new SeqClient(Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081")));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger count = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(seqClientList.size());
        for (int i = 0; i < seqClientList.size(); i++) {
            int finalI = i;
            executorService.execute(() -> {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 10000) {
                    Long next = seqClientList.get(finalI).next("seq");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
                    count.incrementAndGet();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println(count.get());
    }
}
