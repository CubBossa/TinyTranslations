package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import java.util.Objects;
import java.util.stream.Collectors;

import static de.cubbossa.tinytranslations.nanomessage.NanoMessageParser.CLOSE_TAG;
import static de.cubbossa.tinytranslations.nanomessage.NanoMessageParser.PRE;

public class ContentTagCompilation implements CompilationStep {
	@Override
	public boolean apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
		if (!Objects.equals(node.getType(), NanoMessageParser.CONTENT_TAG)) {
			return false;
		}
		var open = node.getChildren().get(0);
		String openKey = open.getChildren().get(0).getText();
		String attributes = open.getChildren().get(1).getChildren().stream()
				.map(Object::toString).map(s -> ":" + s).collect(Collectors.joining());

		if (node.getChildren().size() > 1) {
			var content = node.getChildren().get(1);
			if (!content.getType().equals(CLOSE_TAG)) {
				if (PRE.stream().anyMatch(s -> s.equalsIgnoreCase(openKey))) {
					attributes += ":'" + content + "'/";
					content.replace("");
					if (node.getChildren().size() == 3) {
						node.getChildren().get(2).replace("");
					}
				} else {
					context.parse(content);
				}
			}
		}
		open.replace("<" + openKey + attributes + ">");
		return true;
	}
}
