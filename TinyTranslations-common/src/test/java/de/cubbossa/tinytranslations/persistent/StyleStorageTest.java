package de.cubbossa.tinytranslations.persistent;

import de.cubbossa.tinytranslations.impl.MessageStyleImpl;
import de.cubbossa.tinytranslations.Translator;
import de.cubbossa.tinytranslations.TinyTranslations;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Map;

public abstract class StyleStorageTest {

    Translator translator;
    StyleStorage storage;

    abstract StyleStorage getStyleStorage(File dir, String name);

    @BeforeEach
    void beforeEach(@TempDir File dir) {
        File gDir = new File(dir, "/test/");
        gDir.mkdirs();

        TinyTranslations.enable(gDir);
        translator = TinyTranslations.application("TestApp");

        new File(gDir, "/TestApp/").mkdirs();
        storage = getStyleStorage(new File(gDir, "/TestApp/"), "styles");
        translator.setStyleStorage(storage);
    }

    @AfterEach
    void afterEach() {
        translator.close();
        TinyTranslations.disable();
    }

    @Test
    void loadStyles() {

        Assertions.assertTrue(storage.loadStyles().isEmpty());
        storage.writeStyles(Map.of(
                "abc", new MessageStyleImpl("abc", TagResolver.resolver("abc", Tag.styling(NamedTextColor.RED)), "<red>")
        ));
        Assertions.assertFalse(storage.loadStyles().isEmpty());
    }
}
