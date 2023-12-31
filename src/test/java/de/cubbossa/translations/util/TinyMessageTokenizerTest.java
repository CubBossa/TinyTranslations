package de.cubbossa.translations.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cubbossa.translations.util.TinyMessageTokenizer.*;
import static org.junit.jupiter.api.Assertions.*;

class TinyMessageTokenizerTest {

	@Test
	void tokenize() {
		Assertions.assertEquals(
				List.of(new TokenValue(CHOICE, "?"), new TokenValue(LIT, "abc")),
				new TinyMessageTokenizer().tokenize("?abc")
		);
	}
}