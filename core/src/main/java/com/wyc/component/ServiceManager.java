package com.wyc.component;

import com.wyc.generated.entity.Node;
import com.wyc.generated.entity.NodeSeq;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.service.INodeSeqService;
import com.wyc.generated.service.INodeService;
import com.wyc.generated.service.ISeqInfoService;
import com.wyc.util.MethodTimeUtil;
import com.wyc.util.NetworkUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Component @Data public class ServiceManager {
    @Autowired INodeService nodeService;
    @Autowired INodeSeqService nodeSeqService;
    @Autowired ISeqInfoService seqInfoService;

    List<SeqInfo> servingSequenceList = new Vector<>();//服务中的序列列表

    String ip = NetworkUtil.getLocalIp();

    @Value("${server.port}") Integer port;


    @Scheduled(fixedDelay = 1000)
    public void checkServingSequenceList() {
        Node node = nodeService.getByIpAndPort(ip, port);
        if (node == null) {
            return;
        }
        List<SeqInfo> list = seqInfoService.getBySeqNameList(servingSequenceList.stream().map(SeqInfo::getSeqName).collect(Collectors.toList()));
        List<Long> seqCoreIdList = list.stream().map(SeqInfo::getId).collect(Collectors.toList());
        List<NodeSeq> bySeqCoreIdList = nodeSeqService.getBySeqCoreIdList(seqCoreIdList);
        for (NodeSeq nodeSeq : bySeqCoreIdList) {
            //如果不是由当前节点服务了，那么需要从当前服务节点中剔除
            if (!nodeSeq.getNodeId().equals(node.getId())) {//如果不是由当前节点服务了，就从该节点移除
                servingSequenceList.removeIf(e -> e.getSeqCoreId().equals(nodeSeq.getSeqCoreId()));
            }
        }

    }

    //注册服务
    @Scheduled(fixedDelay = 1000) public void heartbeat() {
        MethodTimeUtil.start();
        Node node = nodeService.getByIpAndPort(ip, port);
        if (node == null) {
            node = new Node();
            node.setVersion(0L);
        }
        node.setIp(ip);
        node.setPort(port);
        node.setVersion(node.getVersion() + 1);
        node.setUpdateTime(null);
        nodeService.saveOrUpdate(node);
        System.out.println("heartbeat: " + MethodTimeUtil.getTimeAndReset());
    }
}
