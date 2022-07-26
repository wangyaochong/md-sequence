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
 * 节点序列关系表，全局严格递增下一个序列只有一个节点服务
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
@Getter
@Setter
@TableName("node_seq")
@ApiModel(value = "NodeSeq对象", description = "节点序列关系表，全局严格递增下一个序列只有一个节点服务")
public class NodeSeq implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long seqCoreId;

    private Long nodeId;


}
