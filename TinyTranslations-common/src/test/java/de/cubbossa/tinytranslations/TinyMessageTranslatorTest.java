package de.cubbossa.tinytranslations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TinyMessageTranslatorTest extends TestBase {

    @Test
    public void testPreventDuplicateKey() {
        MessageTranslator a = translator.fork("a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> translator.fork("a"));

        a.close();
    }
}
