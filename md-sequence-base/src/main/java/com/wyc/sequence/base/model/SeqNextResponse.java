package com.wyc.sequence.base.model;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeqNextResponse {
    String msg;
    Integer errCode;
    Boolean success;
    Object body;

    @NotNull
    String bodyType;
    //有两种类型，一种是返回序列段，另一种是返回目标机器的ip和port
}
