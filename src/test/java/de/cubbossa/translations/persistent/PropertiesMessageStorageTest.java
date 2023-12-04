package de.cubbossa.translations.persistent;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.Locale;

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
        Assertions.assertNotNull(translations.getMessage("no_perm"));
    }
}