package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static de.cubbossa.tinytranslations.nanomessage.NanoMessageParser.*;
import static de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer.*;

public class NanoMessageCompiler {

	private final List<CompilationStep> compilationSteps = List.of(
			new TagCompilation(),
			new SelfClosingTagCompilation(),
			new PlaceholderCompilation(),
			new ChoiceCompilation(),
			new ContentTagCompilation(),
			(node, context) -> node.getChildren().forEach(context::parse)
	);

	/**
	 * Handles the removal of spaces that are invalid in actual MiniMessage, transforms choices and placeholders into
	 * self-closing tags and turns pre content-tags into self-closing parametrized tags.
	 * @param value The message String to convert into valid MiniMessage
	 * @return the valid MiniMessage String
	 */
	public String compile(@Language("NanoMessage") String value) {
		NanoMessageTokenizer tokenizer = new NanoMessageTokenizer();
		var tokens = tokenizer.tokenize(value);
		NanoMessageParser parser = new NanoMessageParser(tokens);
		var root = parser.parse();
		compile(root);

		return root.getText();
	}

	private void compile(SimpleStringParser<Token, TokenValue, String>.Node node) {
		if (node.getType() == null) {
			for (var child : node.getChildren()) {
				compile(child);
			}
			return;
		}
		compilationSteps.forEach(compilationStep -> compilationStep.apply(node, this::compile));
	}
}
