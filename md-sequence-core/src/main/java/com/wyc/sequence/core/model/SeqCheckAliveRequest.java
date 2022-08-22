package com.wyc.sequence.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeqCheckAliveRequest {
    String targetIp;
    Integer targetPort;
}
