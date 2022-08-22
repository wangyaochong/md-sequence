package com.wyc.sequence.core.generated.mapper;

import com.wyc.sequence.core.generated.entity.SeqCore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 序列核心表，只记录数据 Mapper 接口
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
public interface SeqCoreMapper extends BaseMapper<SeqCore> {

    @Select("select * from seq_core where id=#{id} for update")
    SeqCore getByIdForUpdate(Long id);
}
