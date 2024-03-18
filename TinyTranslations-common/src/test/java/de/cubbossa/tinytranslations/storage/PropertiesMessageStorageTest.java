package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PropertiesMessageStorageTest extends MessageStorageTest {

    public MessageStorage getMessageStorage(File dir) {
        return new PropertiesMessageStorage(dir);
    }

    @Override
    String fileName(String languageTag) {
        return languageTag + ".properties";
    }

    @Test
    void testRead() {
        messageTranslator.saveLocale(Locale.ENGLISH);
        try {
            File dir = new File(gDir, "/TestApp/lang/");
            dir.mkdirs();
            File file = new File(dir, "en.properties");
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("no_perm=\"<red>xy\"");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        messageTranslator.loadLocales();
        assertNotNull(messageTranslator.getMessage("no_perm"));
    }

    @Test
    @SneakyThrows
    void characters() {
        File dir = new File(gDir, "/TestApp/lang/");
        dir.mkdirs();
        Files.copy(Path.of("src/test/resources/character_test_UTF-8.properties"), new File(dir, "en.properties").toPath());

        messageTranslator.loadLocales();
        Message a = messageTranslator.getMessage("char_a");
        assertNotNull(a);
        assertEquals(text("äöüß"), messageTranslator.translate(a));
        Message b = messageTranslator.getMessage("char_b");
        assertNotNull(b);
        assertEquals(text("ǮǬǱʁʀ"), messageTranslator.translate(b));
    }
}