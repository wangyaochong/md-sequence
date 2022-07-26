package com.wyc.controller;

import com.wyc.component.OneDayRequestCounter;
import com.wyc.model.SeqRequestResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seq")
public class SeqController {

    OneDayRequestCounter oneDayRequestCounter = new OneDayRequestCounter();

    @RequestMapping("/next")
    public SeqRequestResult next(String name, Long count) {
        System.out.println("");
        oneDayRequestCounter.addCount();//计数+1
        return null;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() - start < 3000) {
            count++;
        }
        System.out.println(count);
    }
}
