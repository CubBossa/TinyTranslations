package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import static de.cubbossa.tinytranslations.nanomessage.NanoMessageParser.PRE;

public class ContentTagCompilation implements CompilationStep {
	@Override
	public void apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
		if (!node.getType().equals(NanoMessageParser.CONTENT_TAG)) {
			return;
		}
		String open = node.getChildren().get(0).getChildren().get(0).getText();
		if (PRE.stream().anyMatch(s -> s.equalsIgnoreCase(open))) {
			node.replace("<" + open + node.getChildren().get(0).getChildren().get(1) + ":'" + node.getChildren().get(1) + "'/>");
		} else {
			context.parse(node.getChildren().get(1));
		}
	}
}
