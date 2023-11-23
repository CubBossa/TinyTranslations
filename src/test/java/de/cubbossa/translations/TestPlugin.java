package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.intellij.lang.annotations.RegExp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.logging.Logger;

public class TestPlugin {

    public static final Message TEST_1 = new MessageBuilder("examples.test.first")
            .withComment("Lets test this")
            .withPlaceholder("red", "The color red")
            .withPlaceholders("green", "blue")
            .withDefault("<red>Hello \nworld!")
            .withTranslation(Locale.GERMAN, "<red>Hallo Welt!")
            .build();
    public static final Message TEST_2 = new MessageBuilder("examples.test.second")
            .withComment("Another test with\nline break\n\ncomments")
            .withPlaceholder("abc")
            .withDefault("<green>Luke - I am your father!")
            .build();
    public static final Message TEST_C = new MessageBuilder("sorted.c")
            .withDefault("abC").build();
    public static final Message TEST_B = new MessageBuilder("sorted.b")
            .withDefault("aBc").build();
    public static final Message TEST_A = new MessageBuilder("sorted.a")
            .withDefault("Abc").build();

    @Test
    public void testPreventDuplicateKey(@TempDir File dir) {
        TranslationsFramework.enable(dir);
        Translations translations = TranslationsFramework.application("test");
        for (Message message : TranslationsFramework.messageFieldsFromClass(TestPlugin.class)) {
            translations.getMessageSet().put(message.getKey(), message);
        }
        translations.fork("a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> translations.fork("a"));
    }

    private String fileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceInFile(File file, @RegExp String regex, String val) {
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

    @Test
    public void testFileCreation(@TempDir File dir) {

        translations.loadStyles();
        translations.saveLocale(Locale.ENGLISH);

        Assertions.assertTrue(dir.exists());
        Assertions.assertTrue(new File(dir, "en.properties").exists());

        File en = new File(dir, "en.properties");
        replaceInFile(en, "a", "e");
        String before = fileContent(en);
        translations.saveLocale(Locale.ENGLISH);
        Assertions.assertEquals(before, fileContent(en));
    }

    @Test
    public void testLoad(@TempDir File dir) {

        Assertions.assertEquals(Component.text("Hello \nworld!", NamedTextColor.RED), translations.process(TEST_1));
        Assertions.assertEquals(Component.text("Hallo Welt!", NamedTextColor.RED), translations.process(TEST_1, Locale.GERMAN));

        translations.saveLocale(Locale.ENGLISH);
        translations.loadLocale(Locale.ENGLISH);
        Assertions.assertEquals(Component.text("Hello \nworld!", NamedTextColor.RED), translations.process(TEST_1));

        Assertions.assertEquals(Component.text("Luke - I am your father!", NamedTextColor.GREEN), translations.process(TEST_2));
    }
}
