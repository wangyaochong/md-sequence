package com.wyc.generated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.generated.entity.Node;
import com.wyc.generated.mapper.NodeMapper;
import com.wyc.generated.service.INodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务节点表 服务实现类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
@Service
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements INodeService {

    @Override public Node getByIpAndPort(String ip, Integer port) {
        return getOne(new LambdaQueryWrapper<Node>().eq(Node::getIp, ip).eq(Node::getPort, port));
    }
}
