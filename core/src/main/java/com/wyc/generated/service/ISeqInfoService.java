package com.wyc.generated.service;

import com.wyc.generated.entity.SeqInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改 服务类
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
public interface ISeqInfoService extends IService<SeqInfo> {

    List<SeqInfo> getBySeqNameList(List<String> seqName);

    SeqInfo getByName(String seqName);
}
