package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

public class PlaceholderCompilation implements CompilationStep {
	@Override
	public void apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
		if (!node.getType().equals(NanoMessageParser.PLACEHOLDER)) {
			return;
		}
		node.replace("<" + node.getChildren().get(0).toString().trim() + node.getChildren().get(1) + "/>");
	}
}
