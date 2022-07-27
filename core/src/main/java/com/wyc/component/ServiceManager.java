package com.wyc.component;

import com.wyc.generated.entity.Node;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.model.Result;
import com.wyc.util.UtilMethodTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component @Data public class ServiceManager {
    @Autowired INodeService nodeService;
    @Autowired ISeqCoreService seqCoreService;
    @Autowired ISeqInfoService seqInfoService;

    @Autowired SeqManager seqManager;

    @Autowired InstanceComponent instanceComponent;

    public Result<Object> startServe(String seqName) {//开始服务
        SeqInfo seqInfo = seqInfoService.getByName(seqName);
        SeqCore seqCore = seqCoreService.getById(seqInfo.getCoreId());
        seqCore.setNodeId(getOrInsertNode().getId());
        seqManager.startServe(seqName);
        return null;
    }


    @Scheduled(fixedDelay = 500) public void checkServingSequenceList() {
        Node node = nodeService.getByIpAndPort(instanceComponent.getIp(), instanceComponent.getPort());
        if (node == null) {
            return;
        }
        List<String> seqNameList = seqManager.servingSequenceSet.stream().map(SeqInfo::getName).collect(Collectors.toList());
        if (seqNameList.size() == 0) {
            return;
        }
        List<SeqInfo> list = seqInfoService.getBySeqNameList(seqNameList);
        List<Long> seqCoreIdList = list.stream().map(SeqInfo::getId).collect(Collectors.toList());

        List<SeqCore> bySeqCoreIdList = seqCoreService.getBySeqCoreIdList(seqCoreIdList);
        for (SeqCore seqCore : bySeqCoreIdList) {
            //如果不是由当前节点服务了，那么需要从当前服务节点中剔除
            if (!node.getId().equals(seqCore.getNodeId())) {//如果不是由当前节点服务了，就从该节点移除
                seqManager.servingSequenceSet.removeIf(e -> e.getCoreId().equals(seqCore.getId()));
            }
        }
    }

    //注册服务
    @Scheduled(fixedDelay = 500) public void heartbeat() {
        //heartbeat是其他节点用于选择活跃节点以及用于其他节点剔除服务的确认信息
        //执行heartbeat失败的自身节点并不会主动剔除服务
        UtilMethodTime.start();
        getOrInsertNode();
        log.info("heartbeat time={} ", UtilMethodTime.getTimeAndReset());
    }

    private Node getOrInsertNode() {
        Node node = nodeService.getByIpAndPort(instanceComponent.getIp(), instanceComponent.getPort());
        if (node == null) {
            node = new Node();
            node.setVersion(0L);
        }
        if (node.getIp() == null) node.setIp(instanceComponent.getIp());
        if (node.getPort() == null) node.setPort(instanceComponent.getPort());
        node.setVersion(node.getVersion() + 1);//node
        node.setUpdateTime(null);
        nodeService.saveOrUpdate(node);
        return node;
    }
}