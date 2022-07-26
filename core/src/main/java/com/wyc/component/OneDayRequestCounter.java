package com.wyc.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Vector;

public class OneDayRequestCounter {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class OneHourCount {
        String hourString;
        Long count;
    }

    List<OneHourCount> oneHourCountList = new Vector<>();

    public void addCount() {
        if (oneHourCountList.size() == 0) {
            oneHourCountList.add(new OneHourCount());
        }
    }

}
