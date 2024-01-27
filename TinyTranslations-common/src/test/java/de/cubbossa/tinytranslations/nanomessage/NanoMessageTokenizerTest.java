package de.cubbossa.tinytranslations.nanomessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cubbossa.tinytranslations.nanomessage.NanoMessageTokenizer.*;

class NanoMessageTokenizerTest {

    @Test
    void tokenize() {
        Assertions.assertEquals(
                List.of(new TokenValue(CHOICE, "?"), new TokenValue(LIT, "abc")),
                new NanoMessageTokenizer().tokenize("?abc")
        );
    }
}