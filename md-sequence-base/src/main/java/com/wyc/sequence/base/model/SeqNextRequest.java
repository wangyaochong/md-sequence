package com.wyc.sequence.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeqNextRequest {
    String name;
    Integer count;
}
