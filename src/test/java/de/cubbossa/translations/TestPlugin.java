package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

public class TestPlugin {

    public static final File dir = new File("./src/test/resources/pf");

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

    @BeforeAll
    public static void beforeAll() {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @AfterAll
    public static void afterAll() {
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    @Test
    public void testPreventDuplicateKey() {
        GlobalMessageBundle.applicationTranslationsBuilder("a", dir).build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> GlobalMessageBundle.applicationTranslationsBuilder("a", dir).build());
    }

    @Test
    public void testFileCreation() {
        MessageBundle translations = GlobalMessageBundle.applicationTranslationsBuilder("testpl", dir)
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
    }

    @Test
    public void testLoad() {

        MessageBundle translations = GlobalMessageBundle.applicationTranslationsBuilder("testpl1", dir)
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
