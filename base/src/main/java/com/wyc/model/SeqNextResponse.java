package com.wyc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeqNextResponse {
    String message;
    Boolean success;
    String body;
    String bodyType;
    //有两种类型，一种是返回序列段，另一种是返回目标机器的ip和port
}
