package com.wyc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlainSeqSegmentResult {
    List<PlainSeqSegment> segmentList = new ArrayList<>();

    public Integer getTotal() {
        // sum segmentList count
        Integer total = 0;
        for (PlainSeqSegment segment : segmentList) {
            total += segment.getCount();
        }
        return total;
    }
}
