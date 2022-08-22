package com.wyc.sequence.sdk;

import com.sun.istack.internal.NotNull;
import com.wyc.sequence.base.enums.EnumErrorCode;
import com.wyc.sequence.base.enums.EnumSeqNextResponseBodyType;
import com.wyc.sequence.base.exceptions.SeqNameNotFoundInDbException;
import com.wyc.sequence.base.model.*;
import com.wyc.sequence.base.util.UtilJson;
import com.wyc.sequence.base.util.UtilRestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public class Sequence {
    SeqCache seqCache = new SeqCache();
    ReentrantLock nextLock = new ReentrantLock(true);

    ReentrantLock waitingFetchLock = new ReentrantLock(true);
    Condition waitingFetchCondition = waitingFetchLock.newCondition();

    ReentrantLock updateSeqCacheLock = new ReentrantLock(true);

    AtomicInteger fetchingCount = new AtomicInteger(0);
    ExecutorService fetchService;
    Integer clientCacheSize = 0;
    List<String> serverAddressList = new ArrayList<>();
    String seqName;

    String usingAddr;

    public Sequence(String seqName, List<String> serverAddressList, ExecutorService executorService) {
        this.fetchService = executorService;
        this.serverAddressList = serverAddressList;
        this.seqName = seqName;
    }

    public boolean needFetch(int count) {
        if (clientCacheSize == 0) {//如果是0，说明没有初始化，则是第一次拉取
            return true;
        }
        //如果序列缓存小于服务端缓存的一半，则需要拉取缓存
        //需要加上正在拉取的缓存数量，防止过量拉取
        return seqCache.getTotal() - count + fetchingCount.get() < clientCacheSize;
    }

    Consumer<Integer> fetchTask() {
        return (count) -> {
            //加锁主要是为了防止多次拉取任务提交的情况下，拉取结果乱序的问题
            updateSeqCacheLock.lock();
            try {
                // todo 这个锁可能可以去掉，因为数据库操作不可能比内存操作还要快的
                //log.info("fetchTask start, seqName={}, count={}, get lock", seqName, count);
                List<OperatingSeqSegment> segmentFromServer = getSegmentFromServer(count, 0);
                for (OperatingSeqSegment operatingSeqSegment : segmentFromServer) {
                    seqCache.addSegment(operatingSeqSegment);
                }
                //log.info("fetchTask end, seqName={}, count={}", seqName, count);
                waitingFetchLock.lock();
                waitingFetchCondition.signalAll();//更新完segment之后，通知获取数据
                waitingFetchLock.unlock();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                updateSeqCacheLock.unlock();
                fetchingCount.addAndGet(-count);//减去正在拉取的数量
                if (fetchingCount.get() < 0) {
                    fetchingCount.set(0);
                }
            }
        };
    }

    private SeqNextResponse postForSeqSegment(String addr, Integer count) {
        SeqNextRequest request = new SeqNextRequest(seqName, count);
        return UtilRestTemplate.post("http://" + addr + "/seq/next", new HashMap<>(), new HashMap<>(), request, SeqNextResponse.class);
    }

    private List<OperatingSeqSegment> getSegmentFromServer(Integer count, Integer tryCount) {
        if (tryCount >= 10) {
            log.error("getSegmentFromServer tryCount >= 10, seqName={}, count={}", seqName, count);
            throw new RuntimeException(String.format("getSegmentFromServer tryCount >= 10, seqName=%s, count=%s", seqName, count));
        }
        List<String> addrList = new ArrayList<>();
        if (this.usingAddr != null) {//如果已经有正在使用的地址，则放到最前面使用
            addrList.add(usingAddr);
        }
        RandomAddrProvider provider = new RandomAddrProvider(serverAddressList);
        while (provider.hasMore()) {
            String provide = provider.provide();
            if (!addrList.contains(provide)) {
                addrList.add(provide);
            }
        }

        for (String addr : addrList) {
            try {
                SeqNextResponse seqNextResponse = postForSeqSegment(addr, count);
                //       log.info("getSegmentFromServer, seqName={}, count={}, addr={}, seqNextResponse={}", seqName, count, addr, UtilJson.toJson(seqNextResponse));
                if (EnumSeqNextResponseBodyType.Segment.name().equals(seqNextResponse.getBodyType())) {
                    return getOperatingSeqSegments(addr, seqNextResponse);
                } else if (EnumSeqNextResponseBodyType.ErrorMsg.name().equals(seqNextResponse.getBodyType())) {
                    log.error("getSegmentFromServer error, seqName={}, count={}, errorMsg={}", seqName, count, seqNextResponse.getMsg());
                    if (seqNextResponse.getErrCode() == EnumErrorCode.SeqNameNotFoundInDbException) {
                        throw new SeqNameNotFoundInDbException("序列名称不存在");
                    }
                    this.usingAddr = null;
                } else if (EnumSeqNextResponseBodyType.NodeAddress.name().equals(seqNextResponse.getBodyType())) {
                    this.usingAddr = seqNextResponse.getBody().toString();
                    log.info("switch to addr={}", this.usingAddr);
                    return getSegmentFromServer(count, tryCount + 1);//切换到指定的节点，重新获取
                } else {
                    String errMsg = String.format("unsupport body type:%s,addr=%s", seqNextResponse.getBodyType(), addr);
                    this.usingAddr = null;
                    throw new RuntimeException(errMsg);
                }
            } catch (Exception e) {
                if (e instanceof SeqNameNotFoundInDbException) {
                    throw e;
                }
                log.error(e.getMessage(), e);
            }
        }
        throw new RuntimeException("无法连接到客户端");
    }

//    public List<OperatingSeqSegment> abc(String addr, Integer count, Integer tryCount) {
//        try {
//            SeqNextResponse seqNextResponse = postForSeqSegment(addr, count);
//            //      log.info("getSegmentFromServer, seqName={}, count={}, addr={}, seqNextResponse={}", seqName, count, addr, UtilJson.toJson(seqNextResponse));
//
//            if (EnumSeqNextResponseBodyType.Segment.name().equals(seqNextResponse.getBodyType())) {
//                this.usingAddr = addr;
//                return getOperatingSeqSegments(addr, seqNextResponse);
//            } else if (EnumSeqNextResponseBodyType.ErrorMsg.name().equals(seqNextResponse.getBodyType())) {
//                log.error("getSegmentFromServer error, seqName={}, count={}, errorMsg={}", seqName, count, seqNextResponse.getMsg());
//                if (seqNextResponse.getErrCode() == EnumErrorCode.SeqNameNotFoundInDbException) {
//                    throw new SeqNameNotFoundInDbException("序列名称不存在");
//                }
//                this.usingAddr = null;
//            } else if (EnumSeqNextResponseBodyType.NodeAddress.name().equals(seqNextResponse.getBodyType())) {
//                this.usingAddr = seqNextResponse.getBody().toString();
//                log.info("switch to addr={}", this.usingAddr);
//                return getSegmentFromServer(count, tryCount + 1);//切换到指定的节点，重新获取
//            } else {
//                String errMsg = String.format("unsupport body type:%s,addr=%s", seqNextResponse.getBodyType(), addr);
//                throw new RuntimeException(errMsg);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            if (e instanceof SeqNameNotFoundInDbException) {
//                throw e;
//            }
//        }
//    }

    @NotNull private List<OperatingSeqSegment> getOperatingSeqSegments(String addr, SeqNextResponse seqNextResponse) {
        Object body = seqNextResponse.getBody();
        PlainSeqSegmentResult result = UtilJson.fromJson(UtilJson.toJson(body), PlainSeqSegmentResult.class);
        List<PlainSeqSegment> segmentList = result.getSegmentList();
        List<OperatingSeqSegment> operatingSegmentList = new ArrayList<>();
        for (PlainSeqSegment plainSeqSegment : segmentList) {
            OperatingSeqSegment operatingSeqSegment = new OperatingSeqSegment();
            operatingSeqSegment.setStart(new AtomicLong(plainSeqSegment.getStart()));
            operatingSeqSegment.setEnd(new AtomicLong(plainSeqSegment.getEnd()));
            operatingSegmentList.add(operatingSeqSegment);
        }
        this.clientCacheSize = result.getClientCacheSize();
        this.usingAddr = addr;
        return operatingSegmentList;
    }

    public PlainSeqSegmentResult next(Integer count) {
        try {
            nextLock.lock();
            if (needFetch(count)) {//判断是否需要提前拉取
                //如果需要拉取的大于总数，则提交一个拉取任务该数量的任务
                int fetchCount = Math.max(count, clientCacheSize);
                fetchingCount.addAndGet(fetchCount);//添加正在拉取的数量
                fetchService.execute(() -> fetchTask().accept(fetchCount));//如果count大于cacheSize，则拉取count数，比如10万个
            }
            int waitingCount = 0;
            while (seqCache.getTotal() < count) {
                //log.info("seqCache.getTotal() < count await,count={}", count);
                waitingFetchLock.lock();
                boolean await = waitingFetchCondition.await(3L, TimeUnit.SECONDS);//对于单节点切换的情况，有可能永远都不会苏醒，这里需要有超时机制
                waitingFetchLock.unlock();
                //log.info("seqCache.getTotal() < count awake,count={},seqCacheCount={}", count, seqCache.getTotal());
                if (waitingCount >= 3) {
                    throw new RuntimeException("获取序列失败");
                }
                waitingCount++;
            }
            return seqCache.getPlainSequenceResult(count);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            nextLock.unlock();
        }
    }
}
