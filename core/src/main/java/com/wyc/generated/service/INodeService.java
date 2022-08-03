package com.wyc.generated.service;

import com.wyc.generated.entity.Node;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务节点表 服务类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
public interface INodeService extends IService<Node> {
    Node getByIpAndPort(String ip, Integer port);

}
