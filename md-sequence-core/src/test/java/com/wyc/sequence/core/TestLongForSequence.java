package com.wyc.sequence.core;

import java.util.Date;

public class TestLongForSequence {
    public static void main(String[] args) {
        System.out.println(Long.MAX_VALUE);
        long time = new Date().getTime();
        long secondTime = time / 1000;
        System.out.println(secondTime);
        System.out.println(Long.MAX_VALUE/secondTime);
        Long year100 =  (60L * 60 * 24 * 365 * 100 + secondTime);
        System.out.println(year100);
        Long year1000 = (60L * 60 * 24 * 365 * 1000 + secondTime);
        Long year2500 = (60L * 60 * 24 * 365 * 2500 + secondTime);
        System.out.println(year1000);
        long shift = secondTime * 100000000;
        long shift100 = year100 * 100000000;
        long shift1000 = year1000 * 100000000;
        long shift2500 = year2500 * 100000000;
        System.out.println(Long.MAX_VALUE);
        System.out.println(shift+1);
        System.out.println(shift100+1);
        System.out.println(shift1000+1);
        System.out.println(shift2500+1);
    }
}
