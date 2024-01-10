package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TinyTranslatorTest extends TestBase {

    @Test
    public void testPreventDuplicateKey() {
        Translator a = translator.fork("a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> translator.fork("a"));

        a.close();
    }

    @Test
    public void testDefaultsAvailable() {
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            translator.process("<positive>X").compact()
        );
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            translator.process("<positive>X").compact()
        );
    }

    @Test
    public void overwriteStyles() {
        Assertions.assertFalse(translator.getStyleSet().containsKey("negative"));
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.RED),
            translator.process("<negative>X")
        );
        translator.getStyleSet().put("negative", "<green>");
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            translator.process("<negative>X")
        );
    }
}
