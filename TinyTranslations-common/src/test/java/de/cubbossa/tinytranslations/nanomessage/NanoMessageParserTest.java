package de.cubbossa.tinytranslations.nanomessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NanoMessageParserTest {

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

//			StringBuilder b = new StringBuilder();
//			root.tree(b, 0);
//			System.out.println(b);

			Assertions.assertEquals(
					NanoMessageParser.CHOICE_PLACEHOLDER,
					root.getChildren().get(0).getChildren().get(0).getType()
			);
		}
	}
}