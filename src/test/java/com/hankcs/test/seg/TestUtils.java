package com.hankcs.test.seg;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;

/**
 * Created by yinkun on 2017/12/12.
 */
public class TestUtils {

    public static void main(String args[]) {
        HanLP.Config.DEBUG = true;
//        HanLP.Config.Per
        Segment segment = new ViterbiSegment();
        segment.config.ner = true;
        segment.config.placeRecognize = false;
        segment.config.timeRecognize = true;
        segment.config.organizationRecognize = false;
        String sen = "我没有对刘鑫加以过激的语言，我只想让刘鑫告诉我事实，帮我论证一下案卷的内容";


        System.out.println(segment.seg(sen));
    }
}
