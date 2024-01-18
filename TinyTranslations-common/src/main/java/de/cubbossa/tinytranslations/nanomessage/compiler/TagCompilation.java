package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import java.util.Objects;
import java.util.stream.Collectors;

public class TagCompilation implements CompilationStep {
	@Override
	public void apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
		if (Objects.equals(node.getType(), NanoMessageParser.OPEN_TAG)) {
			node.replace(
					"<" + node.getChildren().get(0).toString() +
							node.getChildren().get(1).getChildren().stream()
									.map(Object::toString).map(s -> ":" + s).collect(Collectors.joining()) + ">"
			);
		}
	}
}
