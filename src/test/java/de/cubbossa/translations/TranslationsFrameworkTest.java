package de.cubbossa.translations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

public class TranslationsFrameworkTest extends TestBase {

    @Test
    public void testPreventDuplicateKey() {
        translations = TranslationsFramework.application("test");
        Translations a = translations.fork("a");
        Assertions.assertThrows(IllegalArgumentException.class, () -> translations.fork("a"));

        a.close();
    }
}
