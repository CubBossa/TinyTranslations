package de.cubbossa.translations.persistent;

import de.cubbossa.translations.MessageStyleImpl;
import de.cubbossa.translations.Translations;
import de.cubbossa.translations.TranslationsFramework;
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

    Translations translations;
    StyleStorage storage;

    abstract StyleStorage getStyleStorage(File dir);

    @BeforeEach
    void beforeEach(@TempDir File dir) {
        File gDir = new File(dir, "/test/");
        gDir.mkdirs();

        TranslationsFramework.enable(gDir);
        translations = TranslationsFramework.application("TestApp");

        new File(gDir, "/TestApp/").mkdirs();
        storage = getStyleStorage(new File(gDir, "/TestApp/styles.properties"));
        translations.setStyleStorage(storage);
    }

    @AfterEach
    void afterEach() {
        translations.close();
        TranslationsFramework.disable();
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
