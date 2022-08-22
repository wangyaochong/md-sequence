package com.wyc.sequence.core.component;

import com.wyc.sequence.base.util.UtilDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

public class OneDayRequestCounter {
    @Data @AllArgsConstructor @NoArgsConstructor
    static class OneHourCount {
        String hourString;
        AtomicLong count;//并发安全
    }

    List<OneHourCount> oneHourCountList = new Vector<>();//并发安全

    public void addCount() {
        if (oneHourCountList.size() == 0) {
            oneHourCountList.add(new OneHourCount(getTimeStr(), new AtomicLong(1)));
        } else {
            OneHourCount oneHourCount = oneHourCountList.get(oneHourCountList.size() - 1);
            if (oneHourCount.getHourString().equals(getTimeStr())) {
                oneHourCount.getCount().incrementAndGet();//如果是同一个小时，则计数加1
            } else {//如果不是同一天
                if (oneHourCountList.size() == 24) {//如果已经有24个小时了，则移除最开头的一个小时的
                    oneHourCountList.remove(0);
                }
                oneHourCountList.add(new OneHourCount(getTimeStr(), new AtomicLong(1)));
            }
        }
    }

    private String getTimeStr() {//每隔5分钟计算汇总一次
        return UtilDate.getYyyyMMddHHmmssStr(new Date(System.currentTimeMillis() / 1000 / 60 / 5 * 1000 * 60 * 5));
    }

    public long getOneDayCount() {
        long count = 0;
        for (OneHourCount oneHourCount : oneHourCountList) {
            count += oneHourCount.getCount().get();
        }
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        OneDayRequestCounter oneDayRequestCounter = new OneDayRequestCounter();
        for (int i = 0; i < 10000; i++) {
            oneDayRequestCounter.addCount();
            Thread.sleep(1);
        }
        System.out.println(oneDayRequestCounter.getOneDayCount());

    }

}
