package com.wyc.seqtest;

import ch.qos.logback.classic.Logger;
import com.wyc.App;
import com.wyc.SeqClient;
import com.wyc.enums.EnumSeqType;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.PlainSeqSegmentResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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

@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Slf4j
public class SeqServerClientOneNodeSequenceTest {

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
        seqInfo.setType(EnumSeqType.OneNodeSequence.name());
        seqInfoService.save(seqInfo);
    }

    @Test
    public void testOneNode() {
        ConfigurableApplicationContext run8081 = SpringApplication.run(App.class, "--server.port=8081", "--spring.profiles.active=test");
        List<String> serverAddrList = Collections.singletonList("127.0.0.1:8081");
        SeqClient seqClient = new SeqClient(serverAddrList);
        long start = System.currentTimeMillis();
        int count = 0;
        log.info("start get");
        while (System.currentTimeMillis() - start < 10000) {
            Long seq1 = seqClient.next("seq");
            count++;
        }
        log.info("end get, count={}", count);
    }

    /**
     * w
     * 测试单个客户端进行节点切换
     */
    @Test
    public void testOneNodeSwitch() {
        ConfigurableApplicationContext run8081 = SpringApplication.run(App.class, "--server.port=8081", "--spring.profiles.active=test");
        ConfigurableApplicationContext run8082 = SpringApplication.run(App.class, "--server.port=8082", "--spring.profiles.active=test");
        ConfigurableApplicationContext run8083 = SpringApplication.run(App.class, "--server.port=8083", "--spring.profiles.active=test");
        List<String> serverAddrList = Arrays.asList("127.0.0.1:8081", "127.0.0.1:8082", "127.0.0.1:8083");
        List<SeqClient> clientList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            clientList.add(new SeqClient(serverAddrList));
        }
        List<Long> result = new ArrayList<>();

        int oneBatchCount = 100;
        for (SeqClient seqClient : clientList) {
            for (int i = 0; i < oneBatchCount; i++) {
                result.add(seqClient.next("seq"));
            }
        }
        run8081.close();
        for (SeqClient seqClient : clientList) {
            for (int i = 0; i < oneBatchCount; i++) {
                result.add(seqClient.next("seq"));
            }
        }
        run8082.close();
        for (SeqClient seqClient : clientList) {
            for (int i = 0; i < oneBatchCount; i++) {
                result.add(seqClient.next("seq"));
            }
        }
        run8083.close();
        Assert.assertEquals(oneBatchCount * clientList.size() * 3, result.size());
        //   SeqCore seqCore = seqCoreService.getById(1L);
        //Assert.assertEquals(seqCore.getLastMax(), (Long) 8001L);
//        System.out.println(result);
    }
}
