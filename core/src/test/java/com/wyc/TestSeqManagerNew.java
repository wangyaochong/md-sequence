package com.wyc;

import com.wyc.component.SeqManagerNew;
import com.wyc.model.PlainSeqSegmentResult;
import com.wyc.model.Result;
import com.wyc.util.UtilJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
public class TestSeqManagerNew {

    @Autowired SeqManagerNew seqManagerNew;

    @Test
    public void test(){
        PlainSeqSegmentResult seq = seqManagerNew.next("seq", 100);
        System.out.println(UtilJson.toJson(seq));
    }
}
