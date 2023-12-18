package de.cubbossa.translations;

import org.intellij.lang.annotations.RegExp;

import java.util.regex.Pattern;

public class TranslationsPreprocessor {

	public String apply(String value) {
		return value
				.replaceAll("(?<!\\\\)\\{([a-zA-Z.+]+(:('[^\n:]+?'|[a-zA-Z0-9._,-]+))*)}", "<$1/>")
				.replaceAll("(?<!\\\\)<(nbt|json|gson|legacy|pre|plain)>(.*?)(?<!\\\\)</\\1>", "<$1:'$2'/>")
				.replaceAll("(?<!\\\\)<(legacy(:'[&§]'))>(.*?)(?<!\\\\)</legacy>", "<$1:'$3'/>")
				;
	}

}
