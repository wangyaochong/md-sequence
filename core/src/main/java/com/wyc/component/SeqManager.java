package com.wyc.component;

import com.wyc.model.SeqSegment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SeqManager implements InitializingBean {

    //关于管理序列
    ConcurrentHashMap<String, ArrayBlockingQueue<SeqSegment>> seqQueueMap = new ConcurrentHashMap<>();

    @Autowired


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
