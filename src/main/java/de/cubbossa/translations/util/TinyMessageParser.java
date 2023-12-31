package de.cubbossa.translations.util;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static de.cubbossa.translations.util.TinyMessageTokenizer.*;

public class TinyMessageParser extends SimpleStringParser<Token, TokenValue, String> {

	public static final List<String> PRE = List.of("pre", "nbt", "json", "gson", "legacy");
	public static final String CONTENTS = "CONTENTS";
	public static final String PLACEHOLDER = "PLACEHOLDER";
	public static final String CONTENT_TAG = "CONTENT_TAG";
	public static final String CHOICE_PLACEHOLDER = "CHOICE_PLACEHOLDER";
	public static final String CHOICE_OPTION = "CHOICE_OPTION";
	public static final String TEXT_ELEMENT = "TEXT_ELEMENT";
	public static final String KEY = "KEY";
	public static final String ATTRIBUTES = "ATTRIBUTES";
	public static final String ATTRIBUTE = "ATTRIBUTE";
	public static final String OPEN_TAG = "OPEN_TAG";
	public static final String CLOSE_TAG = "CLOSE_TAG";
	public static final String SELF_CLOSING_TAG = "SELF_CLOSING_TAG";

	public TinyMessageParser(List<TokenValue> tokens) {
		super(tokens);
	}

	@Override
	public Node parse() {
		parseContents(0, () -> true);
		return buildTree();
	}

	@Override
	Token getTokenType(TokenValue value) {
		return value == null ? null : value.type();
	}


	private boolean parseContents(int l, Supplier<Boolean> predicate) {
		Marker m = mark();
		boolean ret = true;
		while (getTokenType() != null && predicate.get()) {
			boolean parsedAnything = parseContentTag(l + 1)
					|| parseSelfClosingTag(l + 1)
					|| parseChoice(l + 1)
					|| parsePlaceholder(l + 1)
					|| parseText(l + 1);
			if (!parsedAnything) {
				advance();
			}
			ret = ret && parsedAnything;
		}
		m.done(CONTENTS);
		return ret;
	}

	private boolean parsePlaceholder(int l) {
		Marker m = mark();
		if (!consumeTokens(PH_OPEN)) {
			return fail(m);
		}
		consumeWhiteSpaces();
		if (!(parseKey() && parseAttributes(l + 1))) {
			return fail(m);
		}
		consumeWhiteSpaces();
		if (!consumeTokens(PH_CLOSE)) {
			return fail(m);
		}
		m.done(PLACEHOLDER);
		return true;
	}

	private boolean parseChoice(int l) {
		Marker m = mark();
		if (!consumeTokens(PH_OPEN)) {
			return fail(m);
		}
		consumeWhiteSpaces();
		if (!(parseKey() && parseAttributes(l + 1))) {
			return fail(m);
		}
		consumeWhiteSpaces();
		if (!consumeTokens(CHOICE)) {
			return fail(m);
		}
		boolean again = true;
		while (again) {
			consumeWhiteSpaces();
			Marker opt = mark();
			if (!(parseString() && parseContents(l + 1, () -> !is(SEPARATOR) && !is(PH_CLOSE)))) {
				return fail(m);
			}
			consumeWhiteSpaces();
			opt.done(CHOICE_OPTION);
			if (!consumeTokens(SEPARATOR)) {
				again = false;
			}
			consumeWhiteSpaces();
		}
		if (!consumeTokens(PH_CLOSE)) {
			return fail(m);
		}
		m.done(CHOICE_PLACEHOLDER);
		return true;
	}

	private boolean parseSelfClosingTag(int l) {
		Marker m = mark();
		if (!(consumeTokens(TAG_OPEN) && parseKey() && parseAttributes(l + 1))) {
			return fail(m);
		}
		if (!consumeTokens(TAG_END, TAG_CLOSE)) {
			return fail(m);
		}
		m.done(SELF_CLOSING_TAG);
		return true;
	}

	private boolean parseContentTag(int l) {
		Marker m = mark();

		String open = parseOpenTag(l + 1);
		if (open == null) {
			return fail(m);
		}
		if (PRE.stream().anyMatch(open::equalsIgnoreCase)) {
			parsePreContent(open);
		} else {
			parseContents(l + 1, () -> !isCloseTag(open));
		}
		parseCloseTag(open);
		m.done(CONTENT_TAG);
		return true;
	}

	private boolean parsePreContent(String open) {
		Marker m = mark();
		while (!isCloseTag(open) && getTokenType() != null) {
			advance();
		}
		m.done(TEXT_ELEMENT);
		return true;
	}

	private @Nullable String parseOpenTag(int l) {
		Marker m = mark();
		if (!consumeTokens(TAG_OPEN)) {
			m.rollback();
			return null;
		}
		String startTag = getTokenText();
		if (!(parseKey() && parseAttributes(l + 1) && consumeTokens(TAG_CLOSE))) {
			m.rollback();
			return null;
		}
		m.done(OPEN_TAG);
		return startTag;
	}

	private boolean isCloseTag(String openTag) {
		Marker m = mark();
		boolean result = consumeTokens(TAG_OPEN, TAG_END)
				&& Objects.equals(getTokenText(), openTag)
				&& consumeTokens(LIT, TAG_CLOSE);
		m.rollback();
		return result;
	}

	private boolean parseCloseTag(String openTag) {
		Marker m = mark();
		if (!consumeTokens(TAG_OPEN, TAG_END)) {
			return fail(m);
		}
		String endTag = getTokenText();
		if (!parseKey() || !Objects.equals(openTag, endTag) || !consumeTokens(TAG_CLOSE)) {
			return fail(m);
		}
		m.done(CLOSE_TAG);
		return true;
	}

	private boolean parseText(int l) {
		if (getTokenType() == null) {
			return false;
		}
		Marker m = mark();
		if (!consumeTokens(getTokenType())) {
			return fail(m);
		}
		m.done(TEXT_ELEMENT);
		return true;
	}

	private boolean parseAttributes(int l) {
		Marker m = mark();
		while (true) {
			if (!consumeTokens(SEPARATOR)) {
				m.done(ATTRIBUTES);
				return true;
			}
			if (!parseAttribute(l + 1)) {
				return fail(m);
			}
		}
	}

	private final List<Token> delimitAttribute = List.of(SEPARATOR, TAG_CLOSE, PH_CLOSE, CHOICE);
	private boolean parseAttribute(int l) {
		Marker m = mark();

		if (!parseString()) {
			m.rollback();
			m = mark();
			while (!is(delimitAttribute)) {
				advance();
				if (getTokenType() == null) {
					return fail(m);
				}
			}
		}
		m.done(ATTRIBUTE);
		return true;
	}

	private boolean parseKey() {
		Marker m = mark();
		if (!consumeTokens(LIT)) {
			return fail(m);
		}
		m.done(KEY);
		return true;
	}

	private boolean parseString() {
		Marker m = mark();
		boolean r = false;
		if (consumeTokens(SQUOTE)) {
			while (!(is(SQUOTE))) {
				advance();
				if (getTokenType() == null) {
					m.rollback();
					return false;
				}
			}
			r = consumeTokens(SQUOTE);
		} else if (consumeTokens(DQUOTE)) {
			while (!(is(DQUOTE))) {
				advance();
				if (getTokenType() == null) {
					m.rollback();
					return false;
				}
			}
			r = consumeTokens(DQUOTE);
		}
		if (!r) {
			m.rollback();
			return false;
		}
		m.done(TEXT_ELEMENT);
		return true;
	}

	private boolean consumeWhiteSpaces() {
		boolean val = false;
		while (consumeTokens(WS)) {
			val = true;
		}
		return val;
	}

	private boolean consumeTokens(Token... types) {
		for (Token type : types) {

			if (!Objects.equals(getTokenType(), type)) {
				return false;
			}
			advance();
		}
		return true;
	}

	private boolean nextIs(Token type) {
		return Objects.equals(lookAhead(1), type);
	}

	private boolean nextIs(Iterable<Token> types) {
		for (Token type : types) {
			if (nextIs(type)) {
				return true;
			}
		}
		return false;
	}

	private boolean is(Token type) {
		return Objects.equals(getTokenType(), type);
	}

	private boolean is(Iterable<Token> types) {
		for (Token type : types) {
			if (is(type)) {
				return true;
			}
		}
		return false;
	}

	private boolean fail(Marker m) {
		m.rollback();
		return false;
	}
}
