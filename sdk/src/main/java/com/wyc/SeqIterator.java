package com.wyc;
import com.wyc.model.OperatingSeqSegment;
import com.wyc.model.PlainSeqSegment;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class SeqIterator implements Iterator<Long> {
    List<OperatingSeqSegment> seqSegmentList;

    public SeqIterator(List<PlainSeqSegment> segmentList) {
        this.seqSegmentList = new Vector<>();
        for (PlainSeqSegment operatingSeqSegment : segmentList) {
            OperatingSeqSegment e = new OperatingSeqSegment(operatingSeqSegment);
            seqSegmentList.add(e);
        }
    }

    @Override public boolean hasNext() {
        return seqSegmentList.size() > 0;
    }

    @Override public Long next() {
        if (seqSegmentList.size() > 0) {
            while (seqSegmentList.get(0).getCount() <= 0) {
                seqSegmentList.remove(0);
            }
            OperatingSeqSegment firstNode = seqSegmentList.get(0);
            Long result = firstNode.getPlainSeqSegment(1).getStart();
            if (seqSegmentList.get(0).getCount() == 0) {
                seqSegmentList.remove(0);
            }
            return result;
        }
        throw new RuntimeException("no more seq");
    }
}
