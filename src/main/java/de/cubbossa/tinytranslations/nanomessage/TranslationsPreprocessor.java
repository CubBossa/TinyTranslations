package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.util.SimpleStringParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import org.intellij.lang.annotations.Language;

import java.util.stream.Collectors;

import static de.cubbossa.tinytranslations.nanomessage.NanoMessageParser.*;
import static de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer.*;

public class TranslationsPreprocessor {

	private boolean[] freeMask;

	/**
	 * Handles the removal of spaces that are invalid in actual MiniMessage, transforms choices and placeholders into
	 * self-closing tags and turns pre content-tags into self-closing parametrized tags.
	 * @param value The message String to convert into valid MiniMessage
	 * @return the valid MiniMessage String
	 */
	public String apply(@Language("TranslationsFormat") String value) {
		NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
		var tokens = tokenizer.tokenize(value);
		NanoMessageParser parser = new NanoMessageParser(tokens);
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
