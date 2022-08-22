package com.wyc;

import org.junit.Test;

public class TestException {
    static class ExceptionA extends RuntimeException {
        public ExceptionA(String message) {
            super(message);
        }
    }

    @Test
    public void test() {

        try{
            throw new RuntimeException(new ExceptionA("test"));
        }catch (RuntimeException a){
            throw a;
        }
    }
}
