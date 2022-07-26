package com.wyc.generated.service;

import com.wyc.generated.entity.NodeSeq;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 节点序列关系表，全局严格递增下一个序列只有一个节点服务 服务类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
public interface INodeSeqService extends IService<NodeSeq> {
    List<NodeSeq> getBySeqCoreId(Long seqCoreId);
    List<NodeSeq> getBySeqCoreIdList(List<Long> seqCoreIdList);
}
