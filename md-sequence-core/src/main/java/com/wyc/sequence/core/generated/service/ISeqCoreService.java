package com.wyc.sequence.core.generated.service;

import com.wyc.sequence.core.generated.entity.SeqCore;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 序列核心表，只记录数据 服务类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
public interface ISeqCoreService extends IService<SeqCore> {
    List<SeqCore> getBySeqCoreIdList(List<Long> seqCoreIdList);
}
