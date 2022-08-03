package com.wyc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAddrProvider {
    private final List<String> addrList;
    private final Random random = new Random();

    public RandomAddrProvider(List<String> addrList) {
        this.addrList = new ArrayList<>(addrList);
    }

    public boolean hasMore() {
        return !addrList.isEmpty();
    }

    public String provide() {
        //get random element from addr
        int i = random.nextInt(addrList.size());
        return addrList.remove(i);
    }
}
