package com.wyc.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.alturkovic.lock.Lock;
import com.wyc.component.InstanceComponent;
import com.wyc.component.SeqManager;
import com.wyc.config.DbLockConfig;
import com.wyc.enums.EnumErrorCode;
import com.wyc.enums.EnumSeqNextResponseBodyType;
import com.wyc.exceptions.OneNodeSequenceServingByOtherException;
import com.wyc.exceptions.SeqNameNotFoundInDbException;
import com.wyc.generated.entity.Node;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.mapper.NodeMapper;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.*;
import com.wyc.util.UtilRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seq")
@Slf4j
public class SeqController {

    @Autowired SeqManager seqManager;
    @Autowired INodeService nodeService;
    @Autowired ISeqCoreService seqCoreService;
    @Autowired ISeqInfoService seqInfoService;
    @Autowired NodeMapper nodeMapper;
    @Autowired
    @Qualifier("simpleJdbcLock")
    private Lock lock;

    @GetMapping("/checkAlive")
    public Boolean checkAlive() {
        return true;
    }

    @PostMapping("/checkTargetAlive")
    public Boolean checkTargetAlive(@RequestBody SeqCheckAliveRequest request) {
        return UtilRestTemplate.get("http://" + request.getTargetIp() + ":" + request.getTargetPort() + "/seq/checkAlive", Boolean.class);
    }


    @PostMapping("/next")
    public SeqNextResponse next(@RequestBody SeqNextRequest request, Integer retryCount) throws InterruptedException {
        if (retryCount == null) {
            retryCount = 0;
        }
        if (retryCount >= 10) {
            return new SeqNextResponse("next重试次数太多，无法提供服务", EnumErrorCode.TooMuchRetry, false, null, EnumSeqNextResponseBodyType.ErrorMsg.name());
        }
        try {
            PlainSeqSegmentResult next = seqManager.next(request.getName(), request.getCount());
            return new SeqNextResponse(null, EnumErrorCode.NoError, true, next, EnumSeqNextResponseBodyType.Segment.name());//返回segment的值
        } catch (OneNodeSequenceServingByOtherException e) {
            SeqInfo seqInfo = seqInfoService.getByName(request.getName());
            SeqCore seqCore = seqCoreService.getById(seqInfo.getCoreId());
            Long nodeId = seqCore.getNodeId();
            Node node = nodeService.getById(nodeId);
            Result<Boolean> checkAlive = null;
            if (node == null) {
                checkAlive = new Result<>(0, "node不存在", true, false);
            } else {
                checkAlive = checkAliveByPeer(node.getIp(), node.getPort());
            }
            if (checkAlive.isSuccess()) {//如果是存活的，则返回ip的响应
                if (checkAlive.getData()) {//如果检查后是存活的
                    return new SeqNextResponse("请连接指定ip端口的服务器", EnumErrorCode.NotUsingTargetNodeException, true, node.getIp() + ":" + node.getPort(), EnumSeqNextResponseBodyType.NodeAddress.name());
                } else {
                    //如果不是存活的，则更新节点状态为自身，这个地方有可能有并发
                    //这个地方，必须要用分布式锁，不然可能会并发修改
                    List<String> lockKey = Collections.singletonList(request.getName());
                    String acquire = lock.acquire(lockKey, DbLockConfig.LOCK_TABLE_NAME, 10000);//说明有另一个节点在更新了，这时候可以让
                    if (acquire == null) {//说明其他实例正在操作，等待100ms重试
                        Thread.sleep(100);
                        return next(request, retryCount + 1);//如果获取锁失败，则重试  //todo 计数，次数过多需要抛异常，防止死循环
                    } else {
                        Node selfNode = nodeService.getByIpAndPort(InstanceComponent.getIp(), InstanceComponent.getPort());
                        seqCore.setNodeId(selfNode.getId());
                        SeqCore updateSeqCore = new SeqCore();
                        updateSeqCore.setNodeId(selfNode.getId());
                        seqCoreService.update(updateSeqCore, new LambdaUpdateWrapper<SeqCore>().eq(SeqCore::getId, seqCore.getId()));
                    }
                    lock.release(lockKey, DbLockConfig.LOCK_TABLE_NAME, acquire);
                    return next(request, retryCount + 1);//更新完成后，再次调用next方法获取序列
                }
            } else {//检查失败，可能是没有存活的实例节点，可能是网络不通，可能是其他原因，则返回错误信息
                return new SeqNextResponse(checkAlive.getMsg(), EnumErrorCode.CommonError, false, null, EnumSeqNextResponseBodyType.ErrorMsg.name());
            }
        } catch (SeqNameNotFoundInDbException e) {
            return new SeqNextResponse("序列名称不存在", EnumErrorCode.SeqNameNotFoundInDbException, false, null, EnumSeqNextResponseBodyType.ErrorMsg.name());
        }
    }

    private Result<Boolean> checkAliveByPeer(String ip, Integer port) {
        List<Node> aliveNode = nodeMapper.getAliveNode();
        //取最近更新的3个节点
        List<Node> collect = aliveNode.stream().sorted((o1, o2) -> -o1.getUpdateTime().compareTo(o2.getUpdateTime())).limit(3).collect(Collectors.toList());
        if (collect.size() == 0) {
            return new Result<>(EnumErrorCode.NoAvailableNode, "没有可用的节点", false, false);
        }
        int confirmCount = Math.max(1, collect.size() - 1);//至少要有一个确认节点
        int realConfirmCount = 0;
        for (Node node : collect) {
            SeqCheckAliveRequest seqCheckAliveRequest = new SeqCheckAliveRequest(ip, port);
            try {
                Boolean check = UtilRestTemplate.post("http://" + node.getIp() + ":" + node.getPort() + "/seq/checkTargetAlive", new HashMap<>(), new HashMap<>(), seqCheckAliveRequest, Boolean.class);
                if (check) {
                    realConfirmCount++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        if (realConfirmCount >= confirmCount) {
            return new Result<>(0, "", true, true);
        } else {
            return new Result<>(0, "", true, false);
        }
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
