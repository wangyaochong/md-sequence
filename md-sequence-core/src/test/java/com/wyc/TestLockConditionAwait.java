package com.wyc;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestLockConditionAwait {
    @Test
    public void test() throws InterruptedException {
        Lock lock = new ReentrantLock();
        lock.lock();
        Condition condition = lock.newCondition();
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            lock.lock();
            condition.signalAll();
            lock.unlock();
        });
        //thread.start();
        //在超时前通知返回true，在超时后还没收到通知返回false
        boolean await = condition.await(3, TimeUnit.SECONDS);
        System.out.println(await);
        lock.unlock();
    }
}
