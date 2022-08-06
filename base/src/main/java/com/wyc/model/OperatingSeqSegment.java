package com.wyc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperatingSeqSegment {//需要进行操作的segment
    AtomicLong start;//start只有取数据的时候发生改变
    AtomicLong end;//end只有放数据的时候发生改变

    public Long getCount() {
        if (start.get() > end.get()) {
            throw new RuntimeException("end must bigger than or equal start, start:" + start.get() + ",end:" + end.get());
        }
        return end.get() - start.get();
    }

    public PlainSeqSegment getPlainSeqSegment(int count) {
        if (getCount() < count) {
            throw new RuntimeException("count must smaller than segment count, count:" + count + ",segment count:" + getCount());
        }
        PlainSeqSegment result = new PlainSeqSegment(start.get(), start.get() + count);
        start.addAndGet(count);
        return result;
    }

    public OperatingSeqSegment(PlainSeqSegment plainSeqSegment) {
        this.start = new AtomicLong(plainSeqSegment.getStart());
        this.end = new AtomicLong(plainSeqSegment.getEnd());
    }

    public PlainSeqSegment toPlainSeqSegment() {
        return new PlainSeqSegment(start.get(), end.get());
    }
}
