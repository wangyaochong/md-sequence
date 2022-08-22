package com.wyc.sequence.base.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlainSeqSegmentResult {

    Integer clientCacheSize;
    List<PlainSeqSegment> segmentList = new ArrayList<>();

    @JsonIgnore
    public Integer getTotal() {
        // sum segmentList count
        Integer total = 0;
        for (PlainSeqSegment segment : segmentList) {
            total += segment.getCount();
        }
        return total;
    }
}
