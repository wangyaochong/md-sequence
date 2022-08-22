package com.wyc.sequence.base.util;

import java.util.List;
import java.util.Vector;

public class UtilMethodTime {
    static ThreadLocal<List<Long>> methodTime = new ThreadLocal<>();

    public static void start() {
        List<Long> longs = methodTime.get();
        if (longs == null) {
            longs = new Vector<>();
            methodTime.set(longs);
        }
        methodTime.get().add(System.currentTimeMillis());
    }

    public static Long getTime() {
        checkStarted();
        List<Long> longs = methodTime.get();
        Long aLong = longs.get(longs.size() - 1);
        return System.currentTimeMillis() - aLong;
    }

    private static void checkStarted() {
        if (methodTime.get() == null || methodTime.get().size() == 0) {
            throw new RuntimeException("MethodTime Calculator Not Started");
        }
    }

    public static Long getTimeAndReset() {
        checkStarted();
        List<Long> longs = methodTime.get();
        Long aLong = longs.get(longs.size() - 1);
        long result = System.currentTimeMillis() - aLong;
        longs.remove(longs.size() - 1);
        return result;
    }

    private static void innerMethod() {
        start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long timeAndReset = getTimeAndReset();
        System.out.println("innerMethod:" + timeAndReset);
    }

    public static void main(String[] args) {
        start();
        try {
            Thread.sleep(1000);
            innerMethod();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long timeAndReset = getTimeAndReset();
        System.out.println("main:" + timeAndReset);
    }
}
