package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import java.util.stream.Collectors;

public class ChoiceCompilation implements CompilationStep {
	@Override
	public void apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
		if (!node.getType().equals(NanoMessageParser.CHOICE_PLACEHOLDER)) {
			return;
		}
		node.replace("<choice:'<" +
				node.getChildren().get(0).toString().trim() +
				node.getChildren().get(1).toString().trim() + "/>':" +
				node.getChildren().subList(2, node.getChildren().size()).stream()
						.map(SimpleStringParser.Node::toString)
						.map(String::trim)
						.collect(Collectors.joining(":"))
				+ "/>");
	}
}
