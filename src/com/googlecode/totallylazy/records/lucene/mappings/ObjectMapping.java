package com.googlecode.totallylazy.records.lucene.mappings;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.records.Keyword;
import org.apache.lucene.document.Fieldable;

public class ObjectMapping implements Mapping<Object> {
    public Pair<Keyword, Object> toPair(Fieldable fieldable) {
        throw new UnsupportedOperationException();
    }

    public Fieldable toField(Pair<Keyword, Object> pair) {
        throw new UnsupportedOperationException();
    }
}
