package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleMessageBundleTest {

    public static final File dir = new File("./src/test/resources/pf1");

    public static final Message SIMPLE = new MessageBuilder("simple")
            .withDefault("Hello world")
            .withTranslation(Locale.GERMANY, "Hallo welt - Deutschland")
            .withTranslation(Locale.GERMAN, "Hallo welt - Deutsch")
            .withComment("abc")
            .build();

    public static final Message EMBED = new MessageBuilder("embedded")
            .withDefault("Embedded: <msg:simple>")
            .build();
    public static final Message EMBED_NO_BLEED = new MessageBuilder("embedded")
            .withDefault("Embedded: <msg:simple:true>")
            .build();

    static MessageBundle translations = Translations.builder("test")
            .withDefaultLocale(Locale.ENGLISH)
            .withLogger(Logger.getLogger("TestLog"))
            .withPropertiesStorage(dir)
            .withEnabledLocales(Locale.US, Locale.UK, Locale.GERMAN, Locale.ENGLISH, Locale.GERMANY, Locale.forLanguageTag("de-AT"))
            .build();

    @BeforeAll
    public static void beforeAll() {
        translations.addMessage(SIMPLE);
    }

    @AfterAll
    public static void afterAll() {
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    @Test
    void loadStyles() {
    }

    @Test
    void addMessage() {
    }

    @Test
    void addResolver() {
    }

    @Test
    void translateRaw() {
    }

    @Test
    void translate() {
        assertEquals(Component.text("Hello world"), translations.translate(SIMPLE));
        assertEquals(Component.text("Hallo welt - Deutschland"), translations.translate(SIMPLE, Locale.GERMANY));
        assertEquals(Component.text("Hallo welt - Deutsch"), translations.translate(SIMPLE, Locale.GERMAN));
        assertEquals(Component.text("Hallo welt - Deutsch"), translations.translate(SIMPLE, Locale.forLanguageTag("de-AT")));

        assertEquals(
                Component.text("Embedded: Hello world"),
                translations.translate(EMBED, Locale.US)
        );
        assertEquals(
                Component.text("Embedded: Hello world"),
                translations.translate(EMBED_NO_BLEED, Locale.US)
        );
    }

    @Test
    void getStylesAsResolver() {
    }

    @Test
    void addStyle() {
    }
}