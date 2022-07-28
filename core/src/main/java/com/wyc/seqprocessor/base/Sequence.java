package com.wyc.seqprocessor.base;

import javax.swing.text.Segment;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Sequence {
    String seqName;

    public void init(String seqName) throws InterruptedException {
        this.seqName = seqName;

    }

    public String getSeqName() {
        return seqName;
    }

    abstract Segment next(Long count);

    public static void main(String[] args) throws InterruptedException {
        ArrayBlockingQueue<Object> objects = new ArrayBlockingQueue<>(2);
        ArrayBlockingQueue<Object> objects2 = new ArrayBlockingQueue<>(2);


        int threadCount = 12;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        executorService.execute(() -> {
            try {
                System.out.println(Thread.currentThread() + "objects2.take()");
                System.out.println(Thread.currentThread() + " " + objects2.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < 1000; j++) {
                    System.out.println(Thread.currentThread() + " " + j);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            });
        }

        objects.take();
    }
}
