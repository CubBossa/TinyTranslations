package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageParser;
import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.nanomessage.tag.ObjectNotationTag;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

import java.util.Objects;

public class ObjectTagCompilation implements CompilationStep {

    @Override
    public boolean apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context) {
        if (!Objects.equals(node.getType(), NanoMessageParser.OPEN_TAG) && !Objects.equals(node.getType(), NanoMessageParser.PLACEHOLDER) && !Objects.equals(node.getType(), NanoMessageParser.CHOICE_PLACEHOLDER)) {
            return false;
        }
        var key = node.getChildren().get(0);
        if (key.toString().contains(".")) {
            key.replace(ObjectNotationTag.KEY + ":'" + key + "'");
        }
        return false;
    }
}
