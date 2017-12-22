/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 14:47</create-date>
 *
 * <copyright file="PersonDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.ns;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.hanlp.corpus.tag.NS;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.TransformMatrixDictionary;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;
import com.hankcs.hanlp.utility.Predefine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import static com.hankcs.hanlp.utility.Predefine.logger;


/**
 * 地名识别用的词典，实际上是对两个词典的包装
 *
 * @author hankcs
 */
public class PlaceDictionary
{

    /**
     * AC算法用到的Trie树存放地名库
     */
    public static AhoCorasickDoubleArrayTrie<String> chinaplacetrie;
    /**
     * AC算法用到的Trie树存放 地名 邮编
     */
    public static AhoCorasickDoubleArrayTrie<String> zipnametrie;
    /**
     * 地名词典
     */
    public static NSDictionary dictionary;
    /**
     * 转移矩阵词典
     */
    public static TransformMatrixDictionary<NS> transformMatrixDictionary;
    /**
     * AC算法用到的Trie树
     */
    public static AhoCorasickDoubleArrayTrie<String> trie;

    /**
     * 本词典专注的词的ID
     */
    static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_PLACE);
    /**
     * 本词典专注的词的属性
     */
    static final CoreDictionary.Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);

    /**
     * 用于存放规则
     */


    static
    {
        long start = System.currentTimeMillis();
        dictionary = new NSDictionary();
        dictionary.load(HanLP.Config.PlaceDictionaryPath);
        logger.info(HanLP.Config.PlaceDictionaryPath + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        transformMatrixDictionary = new TransformMatrixDictionary<NS>(NS.class);
        transformMatrixDictionary.load(HanLP.Config.PlaceDictionaryTrPath);
        trie = new AhoCorasickDoubleArrayTrie<String>();
        TreeMap<String, String> patternMap = new TreeMap<String, String>();
        patternMap.put(" ", "CH");
        patternMap.put("CDH", "CDH");
        patternMap.put("CDEH", "CDEH");
        patternMap.put("GH", "GH");

        //patternMap.put("GB", "GB");
//        patternMap.put("CDB", "CDB");
        trie.build(patternMap);
        chinaplacetrie = new AhoCorasickDoubleArrayTrie<String>();
        zipnametrie = new AhoCorasickDoubleArrayTrie<String>();

//        //构建地名邮编树  匹配地名
        chinaplacetrie.build(creatPCTrie(HanLP.Config.PlaceChinaPath));
//        //构建地名邮编树  通过地名查邮编
        //构建邮编地名树 通过邮编返回地名补全一个邮编多个地方匹配地方 补全省市区
        zipnametrie.build(creatCodePlaceTrie(HanLP.Config.CodePlacePath));



}



    /**
     *
     * 加载匹配规则
     * @throws IOException
     */


    public static  TreeMap<String, String> creatCodePlaceTrie (String path) {
        TreeMap<String, String> cptMap = new TreeMap<String, String>();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] paramArray = line.split("\\s+");
                if(paramArray[0].length()<=0|| paramArray[0].isEmpty()||paramArray[0]==null||"".equals(paramArray[0])||paramArray[1].length()<=0||paramArray[1].isEmpty()||paramArray[1]==null||"".equals(paramArray[1])){
                    continue;
                }else {
                    cptMap.put(paramArray[0],paramArray[1]);
                }
            }
            br.close();
        }
        catch (Exception e)
        {
            System.out.println("读取" + path + "失败" + e);
        }
        return cptMap;
    }

    public static  TreeMap<String, String> creatPCTrie(String path) {
        TreeMap<String, String> cptMap = new TreeMap<String, String>();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
            String line;
            String st = "";
            while ((line = br.readLine()) != null)
            {

                String[] paramArray = line.split("\\s+");
                int cpflag = 0;
                if(paramArray[0].length()<=0|| paramArray[0].isEmpty()||paramArray[0]==null||"".equals(paramArray[0])||paramArray[1].length()<=0||paramArray[1].isEmpty()||paramArray[1]==null||"".equals(paramArray[1])){
                    continue;
                }else {

                    if(cptMap.containsKey(paramArray[1])){
                        cptMap.put(paramArray[1],cptMap.get(paramArray[1])+paramArray[0]+"##");
                    }else {
                        cptMap.put(paramArray[1],paramArray[0]+"##");
                    }

                }
            }
            br.close();
        }
        catch (Exception e)
        {
            System.out.println("读取" + path + "失败" + e);
        }
        return cptMap;
    }
    //根据匹配出的地名获得邮编
    public static  List<String> getCodeByPlace(String placename){
        List<String> zips = new ArrayList();
        chinaplacetrie.parseText(placename, new AhoCorasickDoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                int len = placename.substring(begin,end).length() ;
                if(len>1 && len == placename.length()){
                    zips.add(value+"=="+placename.substring(begin,end));
                }


            }
        });
        return zips;
    }
// 根据 邮编和市 扩展 省
    public static List<String> expandPreName(String code,String key){
        List<String> places = new ArrayList<>();

        zipnametrie.parseText(code, new AhoCorasickDoubleArrayTrie.IHit<String>()
        {

            @Override
            public void hit(int begin, int end, String value)
            {
                // int i = StringEditDistance.getDistance(value,key);
                places.add(code.substring(begin,end)+"##"+value);
            }
        });
        return places;
    }
    public static void checkPlace(List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll) {
        StringBuilder sentenceBuilder = new StringBuilder();
        for (Vertex vertex : vertexList) {
            sentenceBuilder.append(vertex.getRealWord());
        }
        final String sentence = sentenceBuilder.toString();
        chinaplacetrie.parseText(sentence, new AhoCorasickDoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                String name = sentence.substring(begin,end);
                int offset = 0;
                int index = sentence.indexOf(name);
                wordNetOptimum.insert(offset+index, new Vertex(Predefine.TAG_PLACE, name, ATTRIBUTE, WORD_ID), wordNetAll);


            }
        });
    }

    /**
     * 模式匹配
     *
     * @param nsList         确定的标注序列
     * @param vertexList     原始的未加角色标注的序列
     * @param wordNetOptimum 待优化的图
     * @param wordNetAll
     */
    public static void parsePattern(List<NS> nsList, List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll)
    {
//        ListIterator<Vertex> listIterator = vertexList.listIterator();
        StringBuilder sbPattern = new StringBuilder(nsList.size());
        for (NS ns : nsList)
        {
            sbPattern.append(ns.toString());
        }
        String pattern = sbPattern.toString();
        final Vertex[] wordArray = vertexList.toArray(new Vertex[0]);
        trie.parseText(pattern, new AhoCorasickDoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                StringBuilder sbName = new StringBuilder();
                for (int i = begin; i < end; ++i)
                {
                    sbName.append(wordArray[i].realWord);
                }
                String name = sbName.toString();
                System.err.println("修改： "+name);
                // 对一些bad case做出调整
                if (isBadCase(name)) return;

                // 正式算它是一个名字
                if (HanLP.Config.DEBUG)
                {
                    System.out.printf("识别出地名：%s %s\n", name, value);
                }
                int offset = 0;
                for (int i = 0; i < begin; ++i)
                {
                    offset += wordArray[i].realWord.length();
                }
                wordNetOptimum.insert(offset, new Vertex(Predefine.TAG_PLACE, name, ATTRIBUTE, WORD_ID), wordNetAll);
            }
        });
    }

    /**
     * 因为任何算法都无法解决100%的问题，总是有一些bad case，这些bad case会以“盖公章 A 1”的形式加入词典中<BR>
     * 这个方法返回是否是bad case
     *
     * @param name
     * @return
     */
    static boolean isBadCase(String name)
    {
        EnumItem<NS> nrEnumItem = dictionary.get(name);
        if (nrEnumItem == null) return false;
        return nrEnumItem.containsLabel(NS.Z);
    }
}
