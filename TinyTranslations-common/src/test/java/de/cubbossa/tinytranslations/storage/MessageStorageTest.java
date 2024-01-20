package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public abstract class MessageStorageTest {

    File gDir;
    MessageTranslator messageTranslator;
    MessageStorage storage;
    Message a, b, c, d;

    abstract MessageStorage getMessageStorage(File dir);

    @BeforeEach
    void beforeEach(@TempDir File dir) {
        gDir = new File(dir, "/test/");
        gDir.mkdirs();

        messageTranslator = TinyTranslations.application("TestApp");

        storage = getMessageStorage(new File(gDir, "/TestApp/lang/"));
        messageTranslator.setMessageStorage(storage);
        a = messageTranslator.messageBuilder("a").withDefault("A").build();
        b = messageTranslator.messageBuilder("h.b").withDefault("B").build();
        c = messageTranslator.messageBuilder("h.a.c").withDefault("C").build();
        d = messageTranslator.messageBuilder("h.b.d").withDefault("D").build();
    }

    @AfterEach
    void afterEach() {
        messageTranslator.close();
    }


    @Test
    void readMessage() {
        Assertions.assertTrue(storage.readMessages(Locale.ENGLISH).isEmpty());
        storage.writeMessages(Collections.singleton(a), Locale.ENGLISH);
        Assertions.assertEquals("A", storage.readMessages(Locale.ENGLISH).get(a));
    }

    @Test
    void readMessages() {
        Assertions.assertTrue(storage.readMessages(Locale.ENGLISH).isEmpty());
        storage.writeMessages(Set.of(a, b), Locale.ENGLISH);
        Assertions.assertEquals(2, storage.readMessages(Locale.ENGLISH).size());
    }

    @Test
    void writeMessage() {
        storage.writeMessages(Set.of(a), Locale.ENGLISH);
        Assertions.assertEquals(1, storage.readMessages(Locale.ENGLISH).size());
        storage.writeMessages(Set.of(b), Locale.ENGLISH);
        Assertions.assertEquals(2, storage.readMessages(Locale.ENGLISH).size());
        a.getDictionary().put(Locale.ENGLISH, "AA");
        Assertions.assertFalse(storage.writeMessages(Set.of(a, c), Locale.ENGLISH).contains(a));
        Assertions.assertEquals("A", storage.readMessages(Locale.ENGLISH).get(a));
        Assertions.assertEquals(3, storage.readMessages(Locale.ENGLISH).size());
    }

    @Test
    void writeMessages() {
    }
}
