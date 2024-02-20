package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import java.util.Objects;
import java.util.stream.Collectors;

public class ChoiceCompilation implements CompilationStep {
    @Override
    public boolean apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
        if (!Objects.equals(node.getType(), NanoMessageParser.CHOICE_PLACEHOLDER)) {
            return false;
        }
        node.replace("<choice:'<" +
                node.getChildren().get(0).toString().trim() +
                node.getChildren().get(1).toString().trim() + ">':" +
                node.getChildren().subList(2, node.getChildren().size()).stream()
                        .map(context::parse)
                        .map(SimpleStringParser.Node::toString)
                        .map(String::trim)
                        .map(s -> {
                            if (!(s.startsWith("'") && s.endsWith("'"))) {
                                return "'" + s + "'";
                            }
                            return s;
                        })
                        .collect(Collectors.joining(":"))
                + ">");
        return true;
    }
}
