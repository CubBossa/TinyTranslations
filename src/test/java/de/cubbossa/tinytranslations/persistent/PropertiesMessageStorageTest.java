package de.cubbossa.tinytranslations.persistent;

import de.cubbossa.tinytranslations.Message;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static net.kyori.adventure.text.Component.*;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesMessageStorageTest extends MessageStorageTest {

    public MessageStorage getMessageStorage(File dir) {
        return new PropertiesMessageStorage(dir);
    }

    @Test
    void testRead() {
        translator.saveLocale(Locale.ENGLISH);
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
        translator.loadLocales();
        assertNotNull(translator.getMessage("no_perm"));
    }

    @Test
    @SneakyThrows
    void characters() {
        File dir = new File(gDir, "/TestApp/lang/");
        dir.mkdirs();
        Files.copy(Path.of("src/test/resources/character_test_UTF-8.properties"), new File(dir, "en.properties").toPath());

        translator.loadLocales();
        Message a = translator.getMessage("char_a");
        assertNotNull(a);
        assertEquals(text("äöüß"), translator.process(a));
        Message b = translator.getMessage("char_b");
        assertNotNull(b);
        assertEquals(text("ǮǬǱʁʀ"), translator.process(b));
    }
}