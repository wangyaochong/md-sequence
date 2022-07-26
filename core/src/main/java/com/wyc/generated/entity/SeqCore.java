package com.wyc.generated.entity;

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

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("上一次取值的最大值，这次取需要+1开始")
    private Long maxSeq;

    @ApiModelProperty("服务节点id，可能是null，如果是全局递增，则必须不为null")
    private Long nodeId;


}
