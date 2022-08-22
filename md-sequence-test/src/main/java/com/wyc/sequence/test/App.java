package com.wyc.sequence.test;

import com.wyc.sequence.sdk.SeqClient;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        if(args.length!=0){
            List<String> strings = Arrays.asList(args);
            if(strings.contains("1node1client")){
                test1Node1Client();
            }
            if(strings.contains("1node2client")){
//                test1Node3Client();
            }
        }
    }

    static long runTime = 10000L;
    public static void test1Node1Client() {
        ConfigurableApplicationContext run8081 = SpringApplication.run(com.wyc.sequence.core.App.class, "--server.port=8081", "--spring.profiles.active=test");
        SeqClient seqClient = new SeqClient(Collections.singletonList("127.0.0.1:8081"));
        int count = 0;
        Long seq = seqClient.next("seq");
        long startTime = System.currentTimeMillis();
        List<Long> seqList = new ArrayList<>();
        while (System.currentTimeMillis() < startTime + runTime) {
            Long value = seqClient.next("seq");
            count++;
        }
        System.out.println("count:" + count);
    }
}
