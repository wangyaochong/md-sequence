package com.wyc.sequence.core.generated.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改
 * </p>
 *
 * @author wyc
 * @since 2022-07-28
 */
@Getter
@Setter
@TableName("seq_info")
@ApiModel(value = "SeqInfo对象", description = "序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改")
public class SeqInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long coreId;

    private String name;

    @ApiModelProperty("服务端缓存")
    private Integer serverCacheSize;

    @ApiModelProperty("客户端缓存")
    private Integer clientCacheSize;

    private String type;


}
