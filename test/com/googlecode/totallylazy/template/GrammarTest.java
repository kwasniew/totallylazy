package com.googlecode.totallylazy.template;

import com.googlecode.totallylazy.template.ast.AnonymousTemplate;
import com.googlecode.totallylazy.template.ast.Attribute;
import com.googlecode.totallylazy.template.ast.Grammar;
import com.googlecode.totallylazy.template.ast.FunctionCall;
import com.googlecode.totallylazy.template.ast.Mapping;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Lists.list;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Predicates.is;

public class GrammarTest {
    @Test
    public void canParseSingleAttribute() throws Exception {
        Attribute attribute = Grammar.ATTRIBUTE.parse("foo").value();
        assertThat(attribute.value(), is(list("foo")));
    }

    @Test
    public void canParseAttributeWithSubAttribute() throws Exception {
        Attribute attribute = Grammar.ATTRIBUTE.parse("foo.bar").value();
        assertThat(attribute.value(), is(list("foo", "bar")));
    }

    @Test
    public void canParseText() throws Exception {
        CharSequence text = Grammar.TEXT.parse("Some other text").value();
        assertThat(text.toString(), is("Some other text"));
    }

    @Test
    public void literalCanBeSingleOrDoubleQuoted() throws Exception {
        assertThat(Grammar.LITERAL.parse("\"foo\"").value().toString(), is("foo"));
        assertThat(Grammar.LITERAL.parse("'foo'").value().toString(), is("foo"));
    }

    @Test
    public void canParseAnExpression() throws Exception {
        assertThat(Grammar.EXPRESSION.parse("$template()$").value(), instanceOf(FunctionCall.class));
        assertThat(Grammar.EXPRESSION.parse("$template$").value(), instanceOf(Attribute.class));
    }

    @Test
    public void canParseFunctionCallWithNoParameters() throws Exception {
        FunctionCall unnamed = Grammar.FUNCTION_CALL.parse("template()").value();
        assertThat(unnamed.name(), is("template"));
        List<Attribute> arguments = unnamed.arguments();
        assertThat(arguments.isEmpty(), is(true));
    }

    @Test
    public void canParseFunctionCallWithNamedParameters() throws Exception {
        FunctionCall namedArguments = Grammar.FUNCTION_CALL.parse("template(foo=bar, baz=dan)").value();
        assertThat(namedArguments.name(), is("template"));
        Map<String, Attribute> arguments = namedArguments.arguments();
        assertThat(arguments.get("foo").value(), is(list("bar")));
        assertThat(arguments.get("baz").value(), is(list("dan")));
    }

    @Test
    public void canParseFunctionCallImplicitParameters() throws Exception {
        FunctionCall unnamed = Grammar.FUNCTION_CALL.parse("template(foo, bar, baz)").value();
        assertThat(unnamed.name(), is("template"));
        List<Attribute> arguments = unnamed.arguments();
        assertThat(arguments.get(0).value(), is(list("foo")));
        assertThat(arguments.get(1).value(), is(list("bar")));
        assertThat(arguments.get(2).value(), is(list("baz")));
    }


    @Test
    public void canParseFunctionCallLiteralParameters() throws Exception {
        FunctionCall unnamed = Grammar.FUNCTION_CALL.parse("template(\"foo\")").value();
        assertThat(unnamed.name(), is("template"));
        List<CharSequence> arguments = unnamed.arguments();
        assertThat(arguments.get(0).toString(), is("foo"));
    }

    @Test
    public void canParseImplicits() throws Exception {
        assertThat(Grammar.IMPLICIT_ARGUMENTS.parse("a").value().get(0), instanceOf(Attribute.class));
        assertThat(Grammar.IMPLICIT_ARGUMENTS.parse("a,b").value().get(1), instanceOf(Attribute.class));
        assertThat(Grammar.IMPLICIT_ARGUMENTS.parse("a,b").value().get(1), instanceOf(Attribute.class));
        assertThat(Grammar.IMPLICIT_ARGUMENTS.parse("\"a\"").value().get(0), instanceOf(CharSequence.class));
    }

    @Test
    public void canParseValue() throws Exception {
        assertThat(Grammar.VALUE.parse("a").value(), instanceOf(Attribute.class));
        assertThat(Grammar.VALUE.parse("\"a\"").value(), instanceOf(CharSequence.class));
    }

    @Test
    public void canParseLiteral() throws Exception {
        CharSequence text = Grammar.LITERAL.parse("\"Some other text\"").value();
        assertThat(text.toString(), is("Some other text"));
    }

    @Test
    public void canParseAnonymousTemplate() throws Exception {
        AnonymousTemplate template = Grammar.ANONYMOUS_TEMPLATE.parse("{ name | Hello $name$ }").value();
        assertThat(template.paramaeterNames(), is(list("name")));
    }

    @Test
    public void supportsMapping() throws Exception {
        Mapping mapping = Grammar.MAPPING.parse("users:{ user | Hello $user$ }").value();
        assertThat(mapping.attribute().value(), is(list("users")));
        assertThat(mapping.expression(), is(instanceOf(AnonymousTemplate.class)));
    }

    @Test
    public void supportsIndirection() throws Exception {
        FunctionCall unnamed = Grammar.FUNCTION_CALL.parse("(template)(foo, bar, baz)").value();
        Attribute attribute = (Attribute) unnamed.name();
        assertThat(attribute.value(), is(list("template")));
    }
}