package de.cubbossa.tinytranslations.nanomessage.compiler;

import de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer;
import de.cubbossa.tinytranslations.util.compiler.SimpleStringParser;

public interface CompilationStep {

    boolean apply(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node, Context context);

    @FunctionalInterface
    interface Context {
        SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node parse(SimpleStringParser<NanoMessageTokenizer.Token, NanoMessageTokenizer.TokenValue, String>.Node node);
    }
}
