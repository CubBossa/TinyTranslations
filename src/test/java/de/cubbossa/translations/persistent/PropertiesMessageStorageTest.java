package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        translations.saveLocale(Locale.ENGLISH);
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
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        translations.loadLocales();
        assertNotNull(translations.getMessage("no_perm"));
    }

    @Test
    @SneakyThrows
    void characters() {
        File dir = new File(gDir, "/TestApp/lang/");
        dir.mkdirs();
        Files.copy(Path.of("src/test/resources/character_test_UTF-8.properties"), new File(dir, "en.properties").toPath());

        translations.loadLocales();
        Message a = translations.getMessage("char_a");
        assertNotNull(a);
        assertEquals(text("äöüß"), translations.process(a));
        Message b = translations.getMessage("char_b");
        assertNotNull(b);
        assertEquals(text("ǮǬǱʁʀ"), translations.process(b));
    }
}