package com.wyc.generated.mapper;

import com.wyc.generated.entity.Node;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 服务节点表 Mapper 接口
 * </p>
 *
 * @author wyc
 * @since 2022-07-26
 */
public interface NodeMapper extends BaseMapper<Node> {

    @Select("select * from node where ip=#{ip} and port = #{port}")
    Node getByIpAndPort(@Param("ip") String ip, @Param("port") Integer port);
}
