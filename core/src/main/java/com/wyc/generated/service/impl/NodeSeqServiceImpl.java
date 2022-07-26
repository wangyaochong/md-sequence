package com.wyc.generated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.generated.entity.NodeSeq;
import com.wyc.generated.mapper.NodeSeqMapper;
import com.wyc.generated.service.INodeSeqService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 节点序列关系表，全局严格递增下一个序列只有一个节点服务 服务实现类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
@Service
public class NodeSeqServiceImpl extends ServiceImpl<NodeSeqMapper, NodeSeq> implements INodeSeqService {

    @Override
    public List<NodeSeq> getBySeqCoreId(Long seqCoreId) {
        return list(new LambdaQueryWrapper<NodeSeq>().eq(NodeSeq::getSeqCoreId, seqCoreId));
    }

    @Override public List<NodeSeq> getBySeqCoreIdList(List<Long> seqCoreIdList) {
        return list(new LambdaQueryWrapper<NodeSeq>().in(NodeSeq::getSeqCoreId, seqCoreIdList));
    }

}
