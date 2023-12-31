package de.cubbossa.translations;

import de.cubbossa.translations.util.SimpleStringParser;
import de.cubbossa.translations.util.TinyMessageParser;
import de.cubbossa.translations.util.TinyMessageTokenizer;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.RegExp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.cubbossa.translations.util.TinyMessageParser.*;
import static de.cubbossa.translations.util.TinyMessageTokenizer.*;

public class TranslationsPreprocessor {

	private boolean[] freeMask;

	/**
	 * Handles the removal of spaces that are invalid in actual MiniMessage, transforms choices and placeholders into
	 * self-closing tags and turns pre content-tags into self-closing parametrized tags.
	 * @param value The message String to convert into valid MiniMessage
	 * @return the valid MiniMessage String
	 */
	public String apply(@Language("TranslationsFormat") String value) {
		TinyMessageTokenizer tokenizer = new TinyMessageTokenizer();
		var tokens = tokenizer.tokenize(value);
		TinyMessageParser parser = new TinyMessageParser(tokens);
		var root = parser.parse();
		cleanupTree(root);

		return root.getText();
	}

	private void cleanupTree(SimpleStringParser<Token, TokenValue, String>.Node node) {
		if (node.getType() == null) {
			for (var child : node.getChildren()) {
				cleanupTree(child);
			}
			return;
		}
		switch (node.getType()) {
			case CONTENTS -> {
				node.getChildren().forEach(this::cleanupTree);
			}
			case CHOICE_PLACEHOLDER -> {
				node.replace("<choice:'<" +
						node.getChildren().get(0).toString().trim() +
						node.getChildren().get(1).toString().trim() + "/>':" +
						node.getChildren().subList(2, node.getChildren().size()).stream()
								.map(SimpleStringParser.Node::toString)
								.map(String::trim)
								.collect(Collectors.joining(":"))
						+ "/>");
			}
			case PLACEHOLDER -> {
				node.replace("<" + node.getChildren().get(0).toString().trim() + node.getChildren().get(1) + "/>");
			}
			case CONTENT_TAG -> {
				String open = node.getChildren().get(0).getChildren().get(0).getText();
				if (PRE.stream().anyMatch(s -> s.equalsIgnoreCase(open))) {
					node.replace("<" + open + node.getChildren().get(0).getChildren().get(1) + ":'" + node.getChildren().get(1) + "'/>");
				} else {
					cleanupTree(node.getChildren().get(1));
				}
			}
		}
	}
}
