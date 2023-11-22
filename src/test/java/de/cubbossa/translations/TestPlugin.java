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
        GlobalTranslations.applicationTranslationsBuilder("a", dir).build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> GlobalTranslations.applicationTranslationsBuilder("a", dir).build());
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
        MessageSet translations = GlobalTranslations.applicationTranslationsBuilder("testpl", dir)
                .withLogger(Logger.getLogger("testTranslations"))
                .withDefaultLocale(Locale.ENGLISH)
                .withEnabledLocales(Locale.ENGLISH, Locale.GERMANY, Locale.GERMAN)
                .withPropertiesStorage(dir)
                .withPropertiesStyles(new File(dir, "styles.properties"))
                .build();

        translations.loadStyles();
        translations.addMessagesClass(this.getClass());
        translations.writeLocale(Locale.ENGLISH);

        Assertions.assertTrue(dir.exists());
        Assertions.assertTrue(new File(dir, "en.properties").exists());

        File en = new File(dir, "en.properties");
        replaceInFile(en, "a", "e");
        String before = fileContent(en);
        translations.writeLocale(Locale.ENGLISH);
        Assertions.assertEquals(before, fileContent(en));
    }

    @Test
    public void testLoad(@TempDir File dir) {

        MessageSet translations = GlobalTranslations.applicationTranslationsBuilder("testpl1", dir)
                .withLogger(Logger.getLogger("testTranslations"))
                .withDefaultLocale(Locale.ENGLISH)
                .withEnabledLocales(Locale.ENGLISH, Locale.GERMANY, Locale.GERMAN)
                .withPropertiesStorage(dir)
                .build();

        translations.addMessagesClass(this.getClass());

        Assertions.assertEquals(Component.text("Hello \nworld!", NamedTextColor.RED), translations.translate(TEST_1));
        Assertions.assertEquals(Component.text("Hallo Welt!", NamedTextColor.RED), translations.translate(TEST_1, Locale.GERMAN));

        translations.writeLocale(Locale.ENGLISH).join();
        translations.clearCache();
        Assertions.assertEquals(Component.text("Hello \nworld!", NamedTextColor.RED), translations.translate(TEST_1));

        Assertions.assertEquals(Component.text("Luke - I am your father!", NamedTextColor.GREEN), translations.translate(TEST_2));
    }
}
