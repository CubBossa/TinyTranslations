package de.cubbossa.translations;

import org.intellij.lang.annotations.RegExp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationsPreprocessor {

	private boolean[] freeMask;

	public String apply(String value) {
		freeMask = new boolean[value.length()];
		value = replaceAllNonOverlapping(value, "(?<!\\\\)<(nbt|json|gson|legacy|pre|plain)>(.*?)(?<!\\\\)</\\1>", "<$1:'$2'/>");
		value = replaceAllNonOverlapping(value, "(?<!\\\\)<(legacy(:'[&§]'))>(.*?)(?<!\\\\)</legacy>", "<$1:'$3'/>");
		value = replaceAllNonOverlapping(value, "(?<!\\\\)\\{([a-zA-Z.+]+(:('[^\n:]+?'|[a-zA-Z0-9._,-]+))*)}", "<$1/>");
		value = replaceAllNonOverlapping(value, "(?<!\\\\)<(nbt|json|gson|legacy|pre|plain)(:'')?/>", "");
		return value;
	}

	private String replaceAllNonOverlapping(String input, @RegExp String pattern, String replacement) {
		StringBuilder buffer = new StringBuilder();
		Matcher m = Pattern
				.compile(pattern)
				.matcher(input);

		while (m.find()) {
			if (free(m.start(), m.end())) {
				m.appendReplacement(buffer, replacement);
				for (int i = m.start(); i < m.end(); i++) {
					freeMask[i] = true;
				}
			}
		}
		m.appendTail(buffer);
		return buffer.toString();
	}

	private boolean free(int start, int end) {
		for (int i = start; i < end; i++) {
			if (freeMask[i]) return false;
		}
		return true;
	}
}
