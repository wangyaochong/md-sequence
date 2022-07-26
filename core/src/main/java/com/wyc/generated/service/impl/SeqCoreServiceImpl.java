package com.wyc.generated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.generated.entity.SeqCore;
import com.wyc.generated.mapper.SeqCoreMapper;
import com.wyc.generated.service.ISeqCoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 序列核心表，只记录数据 服务实现类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
@Service
public class SeqCoreServiceImpl extends ServiceImpl<SeqCoreMapper, SeqCore> implements ISeqCoreService {

    @Override public List<SeqCore> getBySeqCoreIdList(List<Long> seqCoreIdList) {
        return list(new LambdaQueryWrapper<SeqCore>().in(SeqCore::getId, seqCoreIdList));
    }
}
