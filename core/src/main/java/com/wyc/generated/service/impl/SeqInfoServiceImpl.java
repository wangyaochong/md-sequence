package com.wyc.generated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.generated.entity.SeqInfo;
import com.wyc.generated.mapper.SeqInfoMapper;
import com.wyc.generated.service.ISeqInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改 服务实现类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
@Service
public class SeqInfoServiceImpl extends ServiceImpl<SeqInfoMapper, SeqInfo> implements ISeqInfoService {

    @Override public List<SeqInfo> getBySeqNameList(List<String> seqName) {
        return list(new LambdaQueryWrapper<SeqInfo>().in(SeqInfo::getName, seqName));
    }

    @Override public SeqInfo getByName(String seqName) {
        return getOne(new LambdaQueryWrapper<SeqInfo>().eq(SeqInfo::getName, seqName));
    }
}
