package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TinyMessageTranslatorTest extends TestBase {

    @Test
    public void testPreventDuplicateKey() {
        MessageTranslator a = messageTranslator.fork("a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> messageTranslator.fork("a"));

        a.close();
    }

    @Test
    public void testDefaultsAvailable() {
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            messageTranslator.process("<positive>X").compact()
        );
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            messageTranslator.process("<positive>X").compact()
        );
    }

    @Test
    public void overwriteStyles() {
        Assertions.assertFalse(messageTranslator.getStyleSet().containsKey("negative"));
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.RED),
            messageTranslator.process("<negative>X")
        );
        messageTranslator.getStyleSet().put("negative", "<green>");
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            messageTranslator.process("<negative>X")
        );
    }
}
