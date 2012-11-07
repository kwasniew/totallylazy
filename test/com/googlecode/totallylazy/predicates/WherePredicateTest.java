package com.googlecode.totallylazy.predicates;

import com.googlecode.totallylazy.matchers.Matchers;
import org.junit.Test;

import static com.googlecode.totallylazy.Callables.toString;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.predicates.WherePredicate.where;
import static org.hamcrest.MatcherAssert.assertThat;

public class WherePredicateTest {
    @Test
    public void supportsEqualityOnPredicateItself() throws Exception {
        assertThat(where(toString, is("13")).equals(where(toString, is("13"))), Matchers.is(true));
        assertThat(where(toString, is("13")).equals(where(toString, is("14"))), Matchers.is(false));
    }

    @Test
    public void supportsToString() throws Exception {
        assertThat(where(toString, is("13")).toString(), Matchers.is("where toString is 13"));
    }


}
