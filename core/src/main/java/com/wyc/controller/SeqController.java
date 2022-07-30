package com.wyc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.alturkovic.lock.Lock;
import com.wyc.component.InstanceComponent;
import com.wyc.component.OneDayRequestCounter;
import com.wyc.component.SeqManagerNew;
import com.wyc.config.DbLockConfig;
import com.wyc.enums.EnumSeqNextResponseBodyType;
import com.wyc.exceptions.OneNodeSequenceServingByOtherException;
import com.wyc.generated.entity.Node;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/seq")
public class SeqController {

    OneDayRequestCounter oneDayRequestCounter = new OneDayRequestCounter();
    @Autowired SeqManagerNew seqManager;
    @Autowired INodeService nodeService;
    @Autowired ISeqCoreService seqCoreService;
    @Autowired ISeqInfoService seqInfoService;

    @Autowired
    @Qualifier("simpleJdbcLock")
    private Lock lock;

    @RequestMapping("/next")
    public SeqNextResponse next(@RequestBody SeqNextRequest request) throws InterruptedException {
        try {
            Result<PlainSeqSegmentResult> next = seqManager.next(request.getName(), request.getCount());
        } catch (OneNodeSequenceServingByOtherException e) {
            SeqInfo seqInfo = seqInfoService.getByName(request.getName());
            SeqCore seqCore = seqCoreService.getById(seqInfo.getCoreId());
            Long nodeId = seqCore.getNodeId();
            Node node = nodeService.getById(nodeId);
            if (checkAliveByPeer(node.getIp(), node.getPort())) {//如果是存活的，则返回ip的响应
                return new SeqNextResponse("请连接指定ip端口的服务器", true, "127.0.0.1:8080", EnumSeqNextResponseBodyType.NodeAddress.name());
            } else {
                //如果不是存活的，则更新节点状态为自身，这个地方有可能有并发
                //这个地方，必须要用分布式锁，不然可能会并发修改
                List<String> lockKey = Collections.singletonList(seqCore.getId() + "," + seqCore.getNodeId());
                String acquire = lock.acquire(lockKey, DbLockConfig.LOCK_TABLE_NAME, 10000);//说明有另一个节点在更新了，这时候可以让
                if (acquire == null) {
                    Thread.sleep(100);//等待100ms重试
                    return next(request);//如果获取锁失败，则重试  //todo 计数，次数过多需要抛异常，防止死循环
                } else {
                    Node selfNode = nodeService.getByIpAndPort(InstanceComponent.getIp(), InstanceComponent.getPort());
                    seqCore.setNodeId(selfNode.getId());
                    SeqCore updateSeqCore = new SeqCore();
                    updateSeqCore.setNodeId(selfNode.getId());
                    seqCoreService.update(updateSeqCore, new LambdaUpdateWrapper<SeqCore>().eq(SeqCore::getId, seqCore.getId()));
                }
                lock.release(lockKey, DbLockConfig.LOCK_TABLE_NAME, acquire);
                return next(request);//更新完成后，再次调用next方法获取序列
            }
        }
        oneDayRequestCounter.addCount();//计数+1
        return null;
    }

    private boolean checkAliveByPeer(String ip, Integer port) {
        return true;
    }

    @RequestMapping("checkAlive")
    public SeqCheckAliveResponse checkAlive(@RequestBody SeqCheckAliveRequest request) {

        return null;
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() - start < 3000) {
            count++;
        }
        System.out.println(count);
    }
}
