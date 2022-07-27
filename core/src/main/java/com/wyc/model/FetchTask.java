package com.wyc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchTask implements Runnable {
    String seqName;
    Runnable task;


    @Override
    public void run() {
        task.run();
    }
}
