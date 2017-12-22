package com.hankcs.test;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.ns.PlaceDictionary;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;

/**
 * Created by user on 2017/12/22.
 */
public class PlaceCompletion {
    public static Set<String> nsStand(String sen) {
        Set<String> allPlace = new TreeSet<>();
        //String st = "海南三亚有位老奶奶北京东城北锣鼓巷社区近日;坞城国际大都会";
        List<Term> termList = HanLP.segment(sen);
        System.err.println(termList);
        List<List<String>> allmin = new ArrayList<>();
        Map<String, String> dmindex = new HashMap<>();
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            int flag = 0;
            if ("ns".equals(term.nature.toString())) {
                dmindex.put(term.word, i + "");
                List<String> codes = PlaceDictionary.getCodeByPlace(term.word);
                if (codes.size() > 0) {
                    allmin.add(codes);
                } else {
                    codes = PlaceDictionary.getCodeByPlace(term.word + "省");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }
                    codes = PlaceDictionary.getCodeByPlace(term.word + "市");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }
                    codes = PlaceDictionary.getCodeByPlace(term.word + "区");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }
                    codes = PlaceDictionary.getCodeByPlace(term.word + "县");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }
                 /*   codes = PlaceDictionary.getCodeByPlace(term.word + "镇");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }
                    codes = PlaceDictionary.getCodeByPlace(term.word + "街道");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }
                    codes = PlaceDictionary.getCodeByPlace(term.word + "社区");
                    if (codes.size() > 0 && flag == 0) {
                        allmin.add(codes);
                        flag = 1;
                    }*/
                    if (flag == 0) {
                        //库中没匹配到
                        codes.add(term.word);
                        allmin.add(codes);
                    }
                }
            }

        }
        System.out.println(allmin);
        //记录省的位置坐标
        List<String> list = new ArrayList<>();
        for (int i = 0; i < allmin.size(); i++) {
            if (allmin.get(i).get(0).contains("##")) {
                String[] st = allmin.get(i).get(0).split("##");
                for (int j = 0; j < st.length - 1; j++) {
                    if (st[j].length() == 2 || st[j].length() == 4) {
                        list.add(String.valueOf(i));

                    }
                }
            }


        }
        List<String> splitCode = new ArrayList<>();
        for (int i = 0; i < allmin.size(); i++) { //将编码前两位截取
            if (allmin.get(i).get(0).contains("##")) {
                String[] st = allmin.get(i).get(0).split("##");
                if (st.length > 2) {  //匹配出多个邮编
                    String stCode = "";
                    for (int j = 0; j < st.length - 1; j++) {
                        stCode += st[j].substring(0, 2) + "##";
                    }
                    splitCode.add(stCode + st[st.length - 1]);
                } else {
                    splitCode.add(st[0].substring(0, 2) + st[st.length - 1]);
                }


            } else {
                splitCode.add(allmin.get(i).get(0));
            }
        }
        System.err.println(splitCode);
        for (int i = 0; i < splitCode.size(); i++) {
            if (!splitCode.get(i).contains("==")) {
                allPlace.add(splitCode.get(i));
            } else {
                String[] st = splitCode.get(i).split("==");
                String allpl = st[1];
                if (recognIndex(list, String.valueOf(i))) {  //往后匹配，暂不考虑：杨浦在上海
                    if (allmin.size() > i + 1) {   //地点后面还有地点
                        String[] st2 = splitCode.get(i + 1).split("==");
                        if (st2[0].contains("##")) {  //多个邮编的情况  61==陕西省, 15##61##==新城区
                            String[] st2s = st2[0].split("##");
                            int flag = 0;
                            for (int j = 0; j < st2s.length; j++) {   //遍历省后面的编号，判断是否可以拼接
                                if (st2s[j].equals(st[0])) {
                                    flag = 1;
                                }
                            }
                            if (flag == 1) {
                                allPlace.add(allpl + st2[1]);

                            } else {
                                allPlace.add(allpl);
                            }

                        } else {
                            for (int j = 0; j < st2.length - 1; j++) {
                                //遍历省后面的编号，判断是否可以拼接
                                if (st2[0].equals(st[0])) {
                                    allpl += st2[1];
                                    allPlace.add(allpl);
                                    i++;
                                    break;
                                } else {
                                    allPlace.add(allpl);
                                }
                            }
                        }

                    } else { //后面没地点了
                        allPlace.add(allpl);
                    }
                } else { //既不是省也不是市
                    if (st[0].contains("##")) {  // [150102##610102##==新城区]

                    }
                    allPlace.add(allpl);
                }

            }
        }


        return allPlace;
    }

    public static boolean recognIndex(List<String> list, String index) {
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            if (index.equals(list.get(i))) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
