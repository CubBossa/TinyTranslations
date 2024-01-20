package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
