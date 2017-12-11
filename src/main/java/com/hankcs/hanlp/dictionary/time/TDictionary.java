package com.hankcs.hanlp.dictionary.time;

import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.tag.T;
import com.hankcs.hanlp.dictionary.common.EnumItemDictionary;

/**
 * Created by yinkun on 2017/12/5.
 */
public class TDictionary extends EnumItemDictionary<T> {
    @Override
    protected T valueOf(String name) {
        return T.valueOf(name);
    }

    @Override
    protected T[] values() {
        return T.values();
    }

    @Override
    protected EnumItem<T> newItem() {
        return new EnumItem<T>();
    }
}
