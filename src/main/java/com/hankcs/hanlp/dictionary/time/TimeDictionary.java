package com.hankcs.hanlp.dictionary.time;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.hanlp.corpus.tag.NS;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.tag.T;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.TransformMatrixDictionary;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;
import com.hankcs.hanlp.utility.Predefine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yinkun on 2017/12/5.
 */
public class TimeDictionary {

    /**
     * 时间词库
     */
    public static TDictionary tDictionary;

    /**
     * 转移矩阵词库
     */
    public static TransformMatrixDictionary<T> tTransformMatrixDictionary;

    /**
     * AC算法用到的Trie树
     */
    public static AhoCorasickDoubleArrayTrie<String> trie;  // 用于存放

    /**
     * 用于存放规则
     */
    public static List<Pattern> rulerList;
    /**
     * 本词典专注的词的ID
     */
    static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_TIME);
    /**
     * 本词典专注的词的属性
     */
    static final CoreDictionary.Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);

    // 用于加载存续所需要的资源
    static {
        tDictionary = new TDictionary();
        tDictionary.load(HanLP.Config.TimeDictionaryPath);  // 加载发射概率词典

        tTransformMatrixDictionary = new TransformMatrixDictionary<T>(T.class);
        tTransformMatrixDictionary.load(HanLP.Config.TimeDictionaryTrPath);  // 加载转移概率词典

        // 添加时间匹配模板
        trie = new AhoCorasickDoubleArrayTrie<String>();
        TreeMap<String, String> patternMap = new TreeMap<String, String>();
        patternMap.put("S", "S");
        patternMap.put("BH", "BH");
        patternMap.put("BCH", "BCH");
        patternMap.put("BCCH", "BCCH");
        patternMap.put("BCCCH", "BCCCH");
        patternMap.put("BDH", "BDH");
        patternMap.put("BAH", "BAH");
        trie.build(patternMap);

        try {
            loadRuler();
        } catch (IOException ex) {

        }
    }

    public static void loadRuler() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(HanLP.Config.TimeRulerPath), "UTF-8"));
        String line;
        rulerList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            rulerList.add(Pattern.compile(line));
        }
    }

    public static void parseRuler(List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll) {
        StringBuilder sentenceBuilder = new StringBuilder();
        for (Vertex vertex : vertexList) {
            sentenceBuilder.append(vertex.getRealWord());
        }
        if (rulerList != null && rulerList.size() != 0) {
            for (Pattern pattern : rulerList) {
                String sentence = sentenceBuilder.toString();
                Matcher matcher = pattern.matcher(sentence);
                int offset = 0;
                while (matcher.find()) {
                    String time = matcher.group();
//                    System.out.println("time ======== " + time);
                    int index = sentence.indexOf(time);
                    wordNetOptimum.insert(offset + index, new Vertex(Predefine.TAG_PLACE, time, ATTRIBUTE, WORD_ID), wordNetAll, true);
//                    System.out.println("wordNetOptimum ======== " + wordNetOptimum);
                    offset = offset + index;
                    sentence = sentence.substring(index);
                }
            }
        }
    }

    /**
     * @param tList          序列标注结果
     * @param vertexList
     * @param wordNetOptimum
     * @param wordNetAll
     */
    public static void parsePattern(List<T> tList, List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll) {
        StringBuilder tagList = new StringBuilder();
        for (T tag : tList) {
            tagList.append(tag.toString());
        }
        final List<Vertex> wordArray = vertexList;
        trie.parseText(tagList.toString(), new AhoCorasickDoubleArrayTrie.IHit<String>() {

            @Override
            public void hit(int begin, int end, String value) {
                StringBuilder wordBuilder = new StringBuilder();
                for (int i = begin; i < end; ++i) {
                    wordBuilder.append(wordArray.get(i).getRealWord().toString());
                }
                String time = wordBuilder.toString();
                if (!isBaseCase(begin, vertexList, time)) {
                    return;
                }
                // 正式算它是一个时间
                if (HanLP.Config.DEBUG) {
                    System.out.printf("识别出时间：%s %s\n", time, value);
                }

                int offset = 0;
                // 计算在句子中的偏移量
                for (int i = 0; i < begin; ++i) {
                    offset += wordArray.get(i).realWord.length();
                }
                wordNetOptimum.insert(offset, new Vertex(Predefine.TAG_TIME, time, ATTRIBUTE, WORD_ID), wordNetAll);
            }
        });
    }

    private static boolean isBaseCase(int begin, List<Vertex> vertexList, String time) {
        if (begin > 0) {
            // 时间词前面不能是介词短语
            Pattern dayPattern = Pattern.compile("^[0-9一二三四五六七八九十]+[日天号]$");
            Matcher dayMatcher = dayPattern.matcher(time);
            if (dayMatcher.find()) {
                Vertex v = vertexList.get(begin - 1);
                if (v != null && v.realWord != null && v.realWord.matches("的|地|得")) {
                    return false;
                }
                if (v != null && (v.hasNature(Nature.u) || v.hasNature(Nature.ude1) || v.hasNature(Nature.ude2) || v.hasNature(Nature.ude3))) {
                    return false;
                }
//                if (v.realWord.matches("的|地|得") || v.getNature().startsWith("u")) {
//                    return false;
//                }
            }
        }

        Pattern pattern = Pattern.compile("([0-9零一二三四五六七八九十]+)年");
        Matcher matcher = pattern.matcher(time);
        if (matcher.find()) {
            String m = matcher.group(1);
            if (m.length() < 2) {
                return false;
            }
        }
        return true;
    }

//    private static boolean isBaseCase(String time) {
//        EnumItem<T> nrEnumItem = tDictionary.get(time);
//        if (nrEnumItem == null) {
//            // 说明识别的是一个新词
//            return false;
//        }
//        return nrEnumItem.containsLabel(T.Z);
//    }

    public static void main(String args[]) {
//        tDictionary = new TDictionary();
//        tDictionary.load(HanLP.Config.TimeDictionaryPath);
        System.out.println(TimeDictionary.tTransformMatrixDictionary.getFrequency("A", "B"));
        System.out.println(TimeDictionary.tTransformMatrixDictionary.getTotalFrequency(T.A));
        System.out.println(TimeDictionary.tTransformMatrixDictionary.getTotalFrequency());
    }


}
