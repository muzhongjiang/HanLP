package com.hankcs.test.seg;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import junit.framework.TestCase;

import java.util.List;

public class TestTerm extends TestCase {

    public void testContains(){
        List<Term> t1 = HanLP.segment("");
        System.out.println(t1);
    }
}
