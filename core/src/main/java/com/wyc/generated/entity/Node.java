package com.wyc.generated.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 服务节点表
 * </p>
 *
 * @author wyc
 * @since 2022-07-27
 */
@Getter
@Setter
@ApiModel(value = "Node对象", description = "服务节点表")
public class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String ip;

    private Integer port;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新版本号")
    private Long version;

    @ApiModelProperty("更新时间，只有version会更新")
    private LocalDateTime updateTime;


}
