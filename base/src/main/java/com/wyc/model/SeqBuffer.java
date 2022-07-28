package com.wyc.model;

import ch.qos.logback.classic.Logger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Data @AllArgsConstructor @NoArgsConstructor
@Slf4j
public class SeqBuffer {
    //会有并发修改，所以用CopyOnWriteArrayList，在执行getCount方法时，可能有另一个线程修改queue的数据
    List<OperatingSeqSegment> queue = new CopyOnWriteArrayList<>();

    public void addSegment(OperatingSeqSegment segment) {
        queue.add(segment);
    }

    public Long getMax() {
        if (queue.size() == 0) {
            return -1L;
        }
        return queue.get(queue.size() - 1).getEnd().get();
    }

    public Long getTotal() {
        Long total = 0L;
        for (OperatingSeqSegment segment : queue) {
            total += segment.getCount();
        }
        return total;
    }

    public PlainSeqSegmentResult getPlainSequenceResult(int count) {
        if (getTotal() < count) {
            throw new RuntimeException("count must smaller than segment count, count:" + count + ",segment count:" + getTotal());
        }
        log.info("total count={}", getTotal());
        PlainSeqSegmentResult result = new PlainSeqSegmentResult();
        while (result.getTotal() < count) {
            OperatingSeqSegment firstNode = queue.get(0);
            if (result.getTotal() + firstNode.getCount() <= count) {
                queue.remove(0);
                result.getSegmentList().add(firstNode.toPlainSeqSegment());
                log.info("total count={}", getTotal());
            } else {
                result.getSegmentList().add(firstNode.getPlainSeqSegment(count - result.getTotal()));
                log.info("total count={}", getTotal());
            }
        }
        return result;
    }
}
