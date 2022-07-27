package com.wyc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlainSeqSegment {
    Long start;
    Long end;

    public Integer getCount() {
        if (start > end) {
            throw new RuntimeException("end must bigger than or equal start, start:" + start + ",end:" + end);
        }
        return Math.toIntExact(end - start);
    }

    public List<Long> getSequenceList() {
        List<Long> result = new ArrayList<>();
        for (long i = start; i < end; i++) {
            result.add(i);
        }
        return result;
    }
}
