package com.hankcs.hanlp.recognition.time;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.algorithm.Viterbi;
import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.tag.T;
import com.hankcs.hanlp.dictionary.time.TimeDictionary;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by yinkun on 2017/12/5.
 * 时间识别
 */
public class TimeRecognition {

    /**
     * @param pWordSegResult 词语粗分结果
     * @param wordNetOptimum 词云
     * @param wordNetAll
     * @return
     */
    public static final String name_real = "始##名";
    public static final String place_real = "始##地";
    public static final String num_real = "始##数";
    public static final String nt_real = "始##机构名";

    public static boolean recogntion(List<Vertex> pWordSegResult, WordNet wordNetOptimum, WordNet wordNetAll) {
        // 使用规则抽取部分
        TimeDictionary.parseRuler(pWordSegResult, wordNetOptimum, wordNetAll);

        List<EnumItem<T>> tagList = roleTag(pWordSegResult);  // 获得所有可能的标注序列
        if (HanLP.Config.DEBUG) {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            for (EnumItem<T> numItem : tagList) {
                sbLog.append('[');
                sbLog.append(iterator.next().realWord);
                sbLog.append(' ');
                sbLog.append(numItem);
                sbLog.append(']');
            }

            System.out.printf("时间角色观察：%s\n", sbLog.toString());
        }
        List<T> roleTag = viterbiExCompute(tagList);

        if (HanLP.Config.DEBUG) {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (T t : roleTag) {
                sbLog.append(iterator.next().realWord);
                sbLog.append('/');
                sbLog.append(t.toString());
                sbLog.append(" ,");
            }
            if (sbLog.length() > 1) sbLog.delete(sbLog.length() - 2, sbLog.length());
            sbLog.append(']');
            System.out.printf("时间角色标注：%s\n", sbLog.toString());
        }

        TimeDictionary.parsePattern(roleTag, pWordSegResult, wordNetOptimum, wordNetAll);
        return true;
    }

    /**
     * 根据hmm算法，获得最佳标注序列
     *
     * @param tagList
     * @return
     */
    private static List<T> viterbiExCompute(List<EnumItem<T>> tagList) {
//        System.out.println(TimeDictionary.tTransformMatrixDictionary.getFrequency("A", "B"));
        return Viterbi.computeEnum(tagList, TimeDictionary.tTransformMatrixDictionary);
    }

    /**
     * 获得每个词组部分的发射概率
     *
     * @param vertexList 待标注的序列
     * @return 词组的发射概率
     */
    public static List<EnumItem<T>> roleTag(List<Vertex> vertexList) {
        List<EnumItem<T>> tagList = new LinkedList<EnumItem<T>>();  // 标注序列
        ListIterator<Vertex> listIterator = vertexList.listIterator();
        while (listIterator.hasNext()) {
            Vertex vertex = listIterator.next();  // 获取当前结点
//            System.out.println("vertex : " + vertex.word);
            EnumItem<T> enumItem;

            if (vertex.getNature() != null) {
                if (vertex.getNature().equals(Nature.t)) {
                    enumItem = new EnumItem(T.S);  // 说明整个是一个词语
                    tagList.add(enumItem);
                    continue;
                } else if (vertex.getNature().toString().startsWith("nr")) {
                    enumItem = TimeDictionary.tDictionary.get(name_real);  // 此处用等效词，更加精准
                    tagList.add(enumItem);
                    continue;
                } else if (vertex.getNature().toString().startsWith("ns")) {
                    enumItem = TimeDictionary.tDictionary.get(place_real);  // 此处用等效词，更加精准
                    tagList.add(enumItem);
                    continue;
                } else if (vertex.getNature().toString().startsWith("m")) {
                    enumItem = TimeDictionary.tDictionary.get(num_real);  // 此处用等效词，更加精准
                    tagList.add(enumItem);
                    continue;
                } else if (vertex.getNature().toString().startsWith("nt")) {
                    enumItem = TimeDictionary.tDictionary.get(nt_real);  // 此处用等效词，更加精准
                    tagList.add(enumItem);
                    continue;
                }
            }
            if (TimeDictionary.tDictionary.contains(vertex.word)) {
                enumItem = TimeDictionary.tDictionary.get(vertex.word);  // 此处用等效词，更加精准
            } else {
                enumItem = new EnumItem(T.Z);
            }
            tagList.add(enumItem);
        }
        return tagList;
    }

}
