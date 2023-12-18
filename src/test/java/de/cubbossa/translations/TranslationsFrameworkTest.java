package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TranslationsFrameworkTest extends TestBase {

    @Test
    public void testPreventDuplicateKey() {
        Translations a = translations.fork("a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> translations.fork("a"));

        a.close();
    }

    @Test
    public void testDefaultsAvailable() {
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            translations.process("<c_positive>X").compact()
        );
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            translations.process("<positive>X").compact()
        );
    }

    @Test
    public void overwriteStyles() {
        Assertions.assertFalse(translations.getStyleSet().containsKey("negative"));
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.RED),
            translations.process("<negative>X")
        );
        translations.getStyleSet().put("negative", "<green>");
        Assertions.assertEquals(
            Component.text("X", NamedTextColor.GREEN),
            translations.process("<negative>X")
        );
    }
}
