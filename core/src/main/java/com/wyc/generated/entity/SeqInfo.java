package com.wyc.generated.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改
 * </p>
 *
 * @author wyc
 * @since 2022-07-27
 */
@Getter
@Setter
@TableName("seq_info")
@ApiModel(value = "SeqInfo对象", description = "序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改")
@EqualsAndHashCode
public class SeqInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long coreId;

    private String name;

    private Integer cacheSize;

    private String type;


}
