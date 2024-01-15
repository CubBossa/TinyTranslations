package de.cubbossa.tinytranslations.nanomessage;

import org.junit.jupiter.api.Test;

class NanoMessageParserTest {

	@Test
	void parse() {

		NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
		var tokens = tokenizer.tokenize("<red>content{abc ? 'a' : 'b' }</red>bbb");
		NanoMessageParser parser = new NanoMessageParser(tokens);

		var root = parser.parse();
		StringBuilder b = new StringBuilder();
		root.tree(b, 0);
		System.out.println(b);
		root.getChildren().get(0).getChildren().get(0).getChildren().get(1).replace("replaced");

		System.out.println(root.getText());
	}

	@Test
	void parse2() {

		NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
		var tokens = tokenizer.tokenize("<red>content{abc ? a : b }</red>bbb");
		NanoMessageParser parser = new NanoMessageParser(tokens);

		var root = parser.parse();
		StringBuilder b = new StringBuilder();
		root.tree(b, 0);
		System.out.println(b);
		root.getChildren().get(0).getChildren().get(0).getChildren().get(1).replace("replaced");

		System.out.println(root.getText());
	}
}