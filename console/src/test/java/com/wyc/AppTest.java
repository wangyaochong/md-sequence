package com.wyc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
    public void test() {
        System.out.println(Long.MAX_VALUE / 10000000 / 100 / 365);
        //10000000服务端缓存，一百个服务端，每天都启停1次，可以使用2500万年
    }
}
