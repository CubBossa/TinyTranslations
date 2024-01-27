package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.MessageStyle;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Map;

public abstract class StyleStorageTest {

    MessageTranslator messageTranslator;
    StyleStorage storage;

    abstract StyleStorage getStyleStorage(File dir, String name);

    @BeforeEach
    void beforeEach(@TempDir File dir) {
        File gDir = new File(dir, "/test/");
        gDir.mkdirs();

        messageTranslator = TinyTranslations.application("TestApp");

        new File(gDir, "/TestApp/").mkdirs();
        storage = getStyleStorage(new File(gDir, "/TestApp/"), "styles");
        messageTranslator.setStyleStorage(storage);
    }

    @AfterEach
    void afterEach() {
        messageTranslator.close();
    }

    @Test
    void loadStyles() {

        Assertions.assertTrue(storage.loadStyles().isEmpty());
        storage.writeStyles(Map.of(
                "abc", MessageStyle.messageStyle("abc", "<red>{slot}</red>")
        ));
        Assertions.assertFalse(storage.loadStyles().isEmpty());
    }
}
