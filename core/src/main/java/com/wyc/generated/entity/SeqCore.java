package com.wyc.generated.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 序列核心表，只记录数据
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
@Getter
@Setter
@TableName("seq_core")
@ApiModel(value = "SeqCore对象", description = "序列核心表，只记录数据")
public class SeqCore implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long maxSeq;


}
