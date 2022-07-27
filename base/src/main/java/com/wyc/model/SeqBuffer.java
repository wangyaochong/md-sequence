package com.wyc.model;

import ch.qos.logback.classic.Logger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

@Data @AllArgsConstructor @NoArgsConstructor
@Slf4j
public class SeqBuffer {
    Vector<OperatingSeqSegment> queue = new Vector<>();

    public void addSegment(OperatingSeqSegment segment) {
        queue.add(segment);
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
            if (result.getTotal() + firstNode.getCount() < count) {
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
