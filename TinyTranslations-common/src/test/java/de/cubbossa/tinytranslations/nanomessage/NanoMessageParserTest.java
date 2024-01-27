package de.cubbossa.tinytranslations.nanomessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NanoMessageParserTest {

    @Test
    void parseSelfClosing() {
        String[] strings = {
                "<a/>",
                "<a:'b':'c'/>",
                "<a:b:c/>",
                "<a :  b :    c< />",
        };

        for (String string : strings) {

            NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
            var tokens = tokenizer.tokenize(string);
            NanoMessageParser parser = new NanoMessageParser(tokens);

            var root = parser.parse();

            Assertions.assertEquals(
                    NanoMessageParser.SELF_CLOSING_TAG,
                    root.getChildren().get(0).getChildren().get(0).getType()
            );
        }
    }

    @Test
    void parsePlaceholder() {
        String[] strings = {
                "{a:b:c}",
                "{a :b :c}",
                "{ a : b : c }",
                "{ a : b b : c }",
                "{a:'b':'c'}",
                "{a:\"b\":\"c\"}",
                "{ a : 'b' : 'c' }",
                "{ a : '<red>b</red>' : '<c>c</c>' }",
                "{ a : <red>b</red> : <c>c</c> }",
        };

        for (String string : strings) {

            NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
            var tokens = tokenizer.tokenize(string);
            NanoMessageParser parser = new NanoMessageParser(tokens);

            var root = parser.parse();

            Assertions.assertEquals(
                    NanoMessageParser.PLACEHOLDER,
                    root.getChildren().get(0).getChildren().get(0).getType()
            );
        }
    }

    @Test
    void parseAttr() {
        String[] strings = {
                "{a:b:c}",
                "{a :b :c}",
                "{ a : b : c }",
                "{ a :   b  : c }",
                "{ a : b : 'c' }",
        };

        for (String string : strings) {

            NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
            var tokens = tokenizer.tokenize(string);
            NanoMessageParser parser = new NanoMessageParser(tokens);

            var root = parser.parse();

            Assertions.assertEquals(
                    "b",
                    root.getChildren().get(0).getChildren().get(0).getChildren().get(1).getChildren().get(0).getText()
            );
        }
    }

    @Test
    void parseChoice() {

        String[] strings = {
                "{a?b:c}",
                "{a ?b :c}",
                "{ a ? b : c }",
                "{ a ? b b : c }",
                "{a?'b':'c'}",
                "{a?\"b\":\"c\"}",
                "{ a ? 'b' : 'c' }",
                "{ a ? <red>b</red> : <c>c</c> }",
        };

        for (String string : strings) {

            NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
            var tokens = tokenizer.tokenize(string);
            NanoMessageParser parser = new NanoMessageParser(tokens);

            var root = parser.parse();

            Assertions.assertEquals(
                    NanoMessageParser.CHOICE_PLACEHOLDER,
                    root.getChildren().get(0).getChildren().get(0).getType()
            );
        }
    }
}