package com.wyc.seqtest;

import com.wyc.App;
import com.wyc.SeqClient;
import com.wyc.enums.EnumSeqType;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class ConcurrencyTest {
    static final int runTime = 10000;
    @Autowired INodeService nodeService;
    @Autowired ISeqCoreService seqCoreService;
    @Autowired ISeqInfoService seqInfoService;


    @Before
    public void prepareData() {
        nodeService.remove(null);
        seqCoreService.remove(null);
        seqInfoService.remove(null);
        SeqCore seqCore = new SeqCore();
        seqCore.setLastMax(1L);
        seqCore.setId(1L);
        seqCoreService.save(seqCore);
        SeqInfo seqInfo = new SeqInfo();
        seqInfo.setClientCacheSize(10000);
        seqInfo.setServerCacheSize(1000000);
        seqInfo.setCoreId(1L);
        seqInfo.setName("seq");
        seqInfo.setId(1L);
        seqInfo.setType(EnumSeqType.MultiNodeSequence.name());
        seqInfoService.save(seqInfo);
    }


    @Test//1个服务节点1个客户端63w/s，其实主要是网络请求耗时，一秒钟差不多60次网络IO请求，(这里其实要加上线程调度的时间)
    public void test1Node1Client() {
        ConfigurableApplicationContext run8081 = SpringApplication.run(App.class, "--server.port=8081", "--spring.profiles.active=test");
        SeqClient seqClient = new SeqClient(Collections.singletonList("127.0.0.1:8081"));
        long startTime = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() < startTime + runTime) {
            seqClient.next("seq");
            count++;
        }
        System.out.println("count:" + count);
    }


    @Test//1个服务器10个客户端800w/s
    public void test1Node10Client() throws InterruptedException {//单个服务节点单个客户端63w/s
        ConfigurableApplicationContext run8081 = SpringApplication.run(App.class, "--server.port=8081", "--spring.profiles.active=test");
        long startTime = System.currentTimeMillis();
        int seqClientCount = 10;
        final AtomicInteger count = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(seqClientCount);
        CountDownLatch countDownLatch = new CountDownLatch(seqClientCount);
        List<SeqClient> seqClientList = new ArrayList<>();
        for (int i = 0; i < seqClientCount; i++) {
            seqClientList.add(new SeqClient(Collections.singletonList("127.0.0.1:8081")));

        }
        for (int i = 0; i < seqClientCount; i++) {
            int finalI = i;
            executorService.execute(() -> {
                while (System.currentTimeMillis() < startTime + runTime) {
                    seqClientList.get(finalI).next("seq");
                    count.getAndIncrement();
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        System.out.println("count:" + count);
    }

    @Test//3个服务器10个客户端800w/s，因为网络io的时间比较长，所以客户端的数量越多，最终可以获取到的序列就越多
    public void test3Node10Client() throws InterruptedException {//单个服务节点单个客户端63w/s
        ConfigurableApplicationContext run8081 = SpringApplication.run(App.class, "--server.port=8081", "--spring.profiles.active=test");
        ConfigurableApplicationContext run8082 = SpringApplication.run(App.class, "--server.port=8082", "--spring.profiles.active=test");
        ConfigurableApplicationContext run8083 = SpringApplication.run(App.class, "--server.port=8083", "--spring.profiles.active=test");
        long startTime = System.currentTimeMillis();
        int seqClientCount = 10;
        final AtomicInteger count = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(seqClientCount);
        CountDownLatch countDownLatch = new CountDownLatch(seqClientCount);
        List<SeqClient> seqClientList = new ArrayList<>();
        for (int i = 0; i < seqClientCount; i++) {
            seqClientList.add(new SeqClient(Arrays.asList("127.0.0.1:8081", "127.0.0.1:8082", "127.0.0.1:8083")));

        }
        for (int i = 0; i < seqClientCount; i++) {
            int finalI = i;
            executorService.execute(() -> {
                while (System.currentTimeMillis() < startTime + runTime) {
                    seqClientList.get(finalI).next("seq");
                    count.getAndIncrement();
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        System.out.println("count:" + count);
    }

}
