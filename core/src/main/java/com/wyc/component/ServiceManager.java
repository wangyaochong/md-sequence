package com.wyc.component;

import com.wyc.generated.entity.Node;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqCoreService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.util.UtilMethodTime;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Component @Data public class ServiceManager {
    @Autowired INodeService nodeService;
    @Autowired ISeqCoreService seqCoreService;
    @Autowired ISeqInfoService seqInfoService;

    List<SeqInfo> servingSequenceList = new Vector<>();//服务中的序列列表

    @Autowired InstanceComponent instanceComponent;

    @Scheduled(fixedDelay = 1000) public void checkServingSequenceList() {
        Node node = nodeService.getByIpAndPort(instanceComponent.getIp(), instanceComponent.getPort());
        if (node == null) {
            return;
        }
        List<SeqInfo> list = seqInfoService.getBySeqNameList(servingSequenceList.stream().map(SeqInfo::getSeqName).collect(Collectors.toList()));
        List<Long> seqCoreIdList = list.stream().map(SeqInfo::getId).collect(Collectors.toList());

        List<SeqCore> bySeqCoreIdList = seqCoreService.getBySeqCoreIdList(seqCoreIdList);
        for (SeqCore seqCore : bySeqCoreIdList) {
            //如果不是由当前节点服务了，那么需要从当前服务节点中剔除
            if (!node.getId().equals(seqCore.getNodeId())) {//如果不是由当前节点服务了，就从该节点移除
                servingSequenceList.removeIf(e -> e.getSeqCoreId().equals(seqCore.getId()));
            }
        }

    }

    //注册服务
    @Scheduled(fixedDelay = 1000) public void heartbeat() {
        UtilMethodTime.start();
        Node node = nodeService.getByIpAndPort(instanceComponent.getIp(), instanceComponent.getPort());
        if (node == null) {
            node = new Node();
            node.setVersion(0L);
        }
        node.setIp(instanceComponent.getIp());
        node.setPort(instanceComponent.getPort());
        node.setVersion(node.getVersion() + 1);
        node.setUpdateTime(null);
        nodeService.saveOrUpdate(node);
        System.out.println("heartbeat: " + UtilMethodTime.getTimeAndReset());
    }
}
