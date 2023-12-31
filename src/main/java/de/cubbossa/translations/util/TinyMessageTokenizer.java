package de.cubbossa.translations.util;

import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyMessageTokenizer {

	public static final Token ESC = new Token("Esc", Pattern.compile("^\\\\"));
	public static final Token PH_OPEN = new Token("{", Pattern.compile("^\\{"));
	public static final Token PH_CLOSE = new Token("}", Pattern.compile("^}"));
	public static final Token TAG_OPEN = new Token("<", Pattern.compile("^<"));
	public static final Token TAG_CLOSE = new Token(">", Pattern.compile("^\\>"));
	public static final Token TAG_END = new Token("/", Pattern.compile("^/"));
	public static final Token CHOICE = new Token("?", Pattern.compile("^\\?"));
	public static final Token SEPARATOR = new Token(":", Pattern.compile("^:"));
	public static final Token WS = new Token(" ", Pattern.compile("^[ \t]+"));
	public static final Token SQUOTE = new Token("'", Pattern.compile("^'"));
	public static final Token DQUOTE = new Token("\"", Pattern.compile("^\""));
	public static final Token LIT = new Token("Literal", Pattern.compile("^[a-zA-Z0-9#._,-]+"));
	public static final Token MISC = new Token("MISC", Pattern.compile("^."));

	public static final List<Token> TOKENS = List.of(ESC, PH_OPEN, PH_CLOSE, TAG_OPEN, TAG_CLOSE, TAG_END, CHOICE, SEPARATOR, WS, SQUOTE, DQUOTE, LIT, MISC);
	private enum State {
		ANY,
		ESC
	}

	private final List<Token> tokens = new ArrayList<>();
	private State state = State.ANY;

	public TinyMessageTokenizer() {
		this.tokens.addAll(TOKENS);
	}

	public TinyMessageTokenizer(List<Token> tokens) {
		this.tokens.addAll(tokens);
	}

	public record Token(String name, Pattern pattern) {
	}

	public record TokenValue(Token type, String text) {
		@Override
		public String toString() {
			return text;
		}
	}

	public List<TokenValue> tokenize(@Language("TranslationsFormat") String s) {
		List<TokenValue> values = new ArrayList<>();
		while (!s.isEmpty()) {
			if (state == State.ANY) {
				for (Token token : tokens) {
					Matcher m = token.pattern.matcher(s);
					if (!m.find()) {
						continue;
					}
					s = s.substring(m.end());
					values.add(new TokenValue(token, m.group()));

					if (token.equals(ESC)) {
						state = State.ESC;
					}
					break;
				}
			} else if (state == State.ESC) {
				String l = s.substring(0, 1);
				s = s.substring(1);
				values.add(new TokenValue(MISC, l));
				state = State.ANY;
			}
		}
		return values;
	}
}
