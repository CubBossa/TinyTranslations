package de.cubbossa.translations.util;

import org.junit.jupiter.api.Test;

class TinyMessageParserTest {

	@Test
	void parse() {

		TinyMessageTokenizer tokenizer = new TinyMessageTokenizer();
		var tokens = tokenizer.tokenize("<red>content{abc ? 'a' : 'b' }</red>bbb");
		TinyMessageParser parser = new TinyMessageParser(tokens);

		var root = parser.parse();
		StringBuilder b = new StringBuilder();
		root.tree(b, 0);
		System.out.println(b);
		root.getChildren().get(0).getChildren().get(0).getChildren().get(1).replace("replaced");

		System.out.println(root.getText());
	}
}