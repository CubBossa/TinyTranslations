package de.cubbossa.translations;

import org.intellij.lang.annotations.RegExp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class TestBase {

    File dir;
    Translations translations;

    @BeforeEach
    void beforeEach(@TempDir File d) {
        dir = new File(d, "/test/");
        dir.mkdirs();
        TranslationsFramework.enable(dir);
        translations = TranslationsFramework.application("TestApp");
    }

    @AfterEach
    void afterEach() {
        translations.close();
        TranslationsFramework.disable();
    }


    String fileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void replaceInFile(File file, @RegExp String regex, String val) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String fileContent = new String (bytes);
            fileContent = fileContent.replaceAll(regex, val);
            try (FileWriter fw = new FileWriter(file)){
                fw.write(fileContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
