package com.wyc.entity;

import lombok.Data;

@Data
public class ServiceNode {
    String ip;
    Integer port;
    String serviceId;//服务id
}
