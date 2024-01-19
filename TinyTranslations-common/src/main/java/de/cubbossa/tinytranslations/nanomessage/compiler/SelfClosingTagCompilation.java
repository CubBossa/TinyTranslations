package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import java.util.Objects;
import java.util.stream.Collectors;

public class SelfClosingTagCompilation implements CompilationStep {

	@Override
	public boolean apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
		if (!Objects.equals(node.getType(), NanoMessageParser.SELF_CLOSING_TAG)) {
			return false;
		}
		node.replace("<" + node.getChildren().get(0).toString().trim() + node.getChildren().get(1).getChildren().stream()
				.map(SimpleStringParser.Node::toString).map(s -> ":" + s).collect(Collectors.joining()) + "/>");
		return true;
	}
}
