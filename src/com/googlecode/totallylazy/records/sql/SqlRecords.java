package com.googlecode.totallylazy.records.sql;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.totallylazy.records.AbstractRecords;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Queryable;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.sql.expressions.Expression;
import com.googlecode.totallylazy.records.sql.expressions.Expressions;
import com.googlecode.totallylazy.records.sql.expressions.InsertStatement;
import com.googlecode.totallylazy.records.sql.mappings.Mappings;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.Iterator;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Streams.nullOutputStream;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.sql.expressions.DeleteStatement.deleteStatement;
import static com.googlecode.totallylazy.records.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.records.sql.expressions.TableDefinition.tableDefinition;
import static com.googlecode.totallylazy.records.sql.expressions.UpdateStatement.updateStatement;
import static java.lang.String.format;

public class SqlRecords extends AbstractRecords implements Queryable<Expression> {
    private final Connection connection;
    private final PrintStream logger;
    private final Mappings mappings;

    public SqlRecords(Connection connection, Mappings mappings, PrintStream logger) {
        this.connection = connection;
        this.mappings = mappings;
        this.logger = logger;
    }

    public SqlRecords(Connection connection) {
        this(connection, new Mappings(), new PrintStream(nullOutputStream()));
    }

    public RecordSequence get(Keyword recordName) {
        return new RecordSequence(this, from(recordName).select(definitions(recordName)), logger);
    }

    public Sequence<Record> query(final Expression expression, final Sequence<Keyword> definitions) {
        return new Sequence<Record>() {
            public Iterator<Record> iterator() {
                return SqlRecords.this.iterator(expression, definitions);
            }
        };
    }

    public RecordIterator iterator(Expression expression, Sequence<Keyword> definitions) {
        return new RecordIterator(connection, mappings, expression, definitions, logger);
    }

    public void define(Keyword recordName, Keyword<?>... fields) {
        super.define(recordName, fields);
        if (exists(recordName)) {
            return;
        }
        update(tableDefinition(recordName, fields));
    }

    private static final Keyword<Integer> one = keyword("1", Integer.class);
    public boolean exists(Keyword recordName) {
        try {
            query(from(recordName).select(one).build(), Sequences.<Keyword>empty()).realise();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Number add(final Keyword recordName, final Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }
        return update(records.map(InsertStatement.toInsertStatement(recordName)));
    }

    @Override
    public Number set(final Keyword recordName, Pair<? extends Predicate<? super Record>, Record>... records) {
        return update(sequence(records).map(new Callable1<Pair<? extends Predicate<? super Record>, Record>, Expression>() {
            public Expression call(Pair<? extends Predicate<? super Record>, Record> recordPair) throws Exception {
                return updateStatement(recordName, recordPair.first(), recordPair.second());
            }
        }));
    }

    public Number update(final Expression... expressions) {
        return update(sequence(expressions));
    }

    public Number update(final Sequence<Expression> expressions) {
        return expressions.groupBy(Expressions.text()).map(new Callable1<Group<String, Expression>, Number>() {
            public Number call(Group<String, Expression> group) throws Exception {
                logger.print(format("SQL: %s", group.key()));
                Number rowCount = using(connection.prepareStatement(group.key()),
                        mappings.addValuesInBatch(group.map(Expressions.parameters())));
                logger.println(format(" Count:%s", rowCount));
                return rowCount;
            }
        }).reduce(Numbers.<Number>sum());
    }

    public Number remove(Keyword recordName, Predicate<? super Record> predicate) {
        return update(deleteStatement(recordName, predicate));
    }

    public Number remove(Keyword recordName) {
        if (!exists(recordName)) {
            return 0;
        }
        return update(deleteStatement(recordName));
    }

}