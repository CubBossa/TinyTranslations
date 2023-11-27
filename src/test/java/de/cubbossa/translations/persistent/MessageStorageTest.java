package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.Translations;
import de.cubbossa.translations.TranslationsFramework;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.*;

public abstract class MessageStorageTest {

    Translations translations;
    MessageStorage messageStorage;
    Message a, b, c, d;

    abstract MessageStorage getMessageStorage(File dir);

    @BeforeEach
    void beforeEach(@TempDir File dir) {
        File gDir = new File(dir, "/test/");
        gDir.mkdirs();

        TranslationsFramework.enable(gDir);
        translations = TranslationsFramework.application("TestApp");

        messageStorage = getMessageStorage(new File(gDir, "/TestApp/"));
        translations.setMessageStorage(messageStorage);
        a = translations.messageBuilder("a").withDefault("A").build();
        b = translations.messageBuilder("b").withDefault("B").build();
        c = translations.messageBuilder("c").withDefault("C").build();
        d = translations.messageBuilder("d").withDefault("D").build();
    }

    @AfterEach
    void afterEach() {
        translations.close();
        TranslationsFramework.disable();
    }


    @Test
    void readMessage() {
        Assertions.assertTrue(messageStorage.readMessages(Locale.ENGLISH).isEmpty());
        messageStorage.writeMessages(Collections.singleton(a), Locale.ENGLISH);
        Assertions.assertEquals("A", messageStorage.readMessages(Locale.ENGLISH).get(a));
    }

    @Test
    void readMessages() {
        Assertions.assertTrue(messageStorage.readMessages(Locale.ENGLISH).isEmpty());
        messageStorage.writeMessages(Set.of(a, b), Locale.ENGLISH);
        Assertions.assertEquals(2, messageStorage.readMessages(Locale.ENGLISH).size());
    }

    @Test
    void writeMessage() {
        messageStorage.writeMessages(Set.of(a), Locale.ENGLISH);
        Assertions.assertEquals(1, messageStorage.readMessages(Locale.ENGLISH).size());
        messageStorage.writeMessages(Set.of(b), Locale.ENGLISH);
        Assertions.assertEquals(2, messageStorage.readMessages(Locale.ENGLISH).size());
        a.getDictionary().put(Locale.ENGLISH, "AA");
        Assertions.assertFalse(messageStorage.writeMessages(Set.of(a, c), Locale.ENGLISH).contains(a));
        Assertions.assertEquals("A", messageStorage.readMessages(Locale.ENGLISH).get(a));
        Assertions.assertEquals(3, messageStorage.readMessages(Locale.ENGLISH).size());
    }

    @Test
    void writeMessages() {
    }
}
