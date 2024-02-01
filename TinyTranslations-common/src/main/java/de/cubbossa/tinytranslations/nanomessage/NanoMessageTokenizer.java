package de.cubbossa.tinytranslations.nanomessage;

import lombok.Getter;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NanoMessageTokenizer {

    public static final Token ESC = new Token("Esc", "\\");
    public static final Token TAG_OPEN = new Token("<", "<");
    public static final Token TAG_CLOSE = new Token(">",">");
    public static final Token TAG_END = new Token("/","/");
    public static final Token PH_OPEN = new Token("{", "{");
    public static final Token PH_CLOSE = new Token("}", "}");
    public static final Token SEPARATOR = new Token(":",":");
    public static final Token SQUOTE = new Token("'","'");
    public static final Token DQUOTE = new Token("\"","\"");
    public static final Token CHOICE = new Token("?","?");
    public static final Token WS = new Token(" ", Pattern.compile("^[ \t]+"));
    public static final Token LIT = new Token("Literal", Pattern.compile("^[a-zA-Z0-9#._,-]+"));
    public static final Token MISC = new Token("MISC", Pattern.compile("^(.|\n)"));

    public static final List<Token> TOKENS = List.of(ESC, PH_OPEN, PH_CLOSE, TAG_OPEN, TAG_CLOSE, TAG_END, CHOICE, SEPARATOR, WS, SQUOTE, DQUOTE, LIT, MISC);
    private final List<Token> tokens = new ArrayList<>();
    private State state = State.ANY;
    public NanoMessageTokenizer() {
        this.tokens.addAll(TOKENS);
    }

    public NanoMessageTokenizer(List<Token> tokens) {
        this.tokens.addAll(tokens);
    }

    public List<TokenValue> tokenize(@Language("NanoMessage") String s) {
        List<TokenValue> values = new ArrayList<>();
        int offset = 0;
        while (offset < s.length()) {
            if (state == State.ANY) {
                for (Token token : tokens) {

                    String content;
                    if (token.pattern == null) {
                        if (!s.startsWith(token.text, offset)) {
                            continue;
                        }
                        offset += token.text.length();
                        content = token.text;
                    } else {
                        Matcher m = token.pattern.matcher(s.substring(offset));
                        if (!m.find()) {
                            continue;
                        }
                        offset += m.end();
                        content = m.group();
                    }
                    values.add(new TokenValue(token, content));

                    if (token.equals(ESC)) {
                        state = State.ESC;
                    }
                    break;
                }
            } else if (state == State.ESC) {
                String l = s.substring(offset, offset + 1);
                values.add(new TokenValue(MISC, l));
                state = State.ANY;
                offset++;
            }
        }
        return values;
    }

    private enum State {
        ANY,
        ESC
    }

    @Getter
    public static class Token {

        private final int diff;
        private final String name;
        private final Pattern pattern;
        private final String text;

        public Token(String name, Pattern pattern) {
            this.name = name;
            this.diff = this.name.hashCode();
            this.pattern = pattern;
            this.text = null;
        }

        public Token(String name, String text) {
            this.name = name;
            this.diff = this.name.hashCode();
            this.pattern = null;
            this.text = text;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Token token = (Token) object;
            return getDiff() == token.getDiff();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getDiff());
        }
    }

    public record TokenValue(Token type, String text) {
        @Override
        public String toString() {
            return text;
        }
    }
}
