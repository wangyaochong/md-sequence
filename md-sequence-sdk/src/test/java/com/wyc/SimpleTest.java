package com.wyc;

import org.junit.Test;

public class SimpleTest {
    @Test
    public void test(){
        long l = System.currentTimeMillis();
        System.out.println(l);
        long pow = (long) Math.pow(2, 41);
        System.out.println(pow/1000/60/60/24/365);
        System.out.println(pow);
        System.out.println((int)Math.pow(2,12));
    }
}
