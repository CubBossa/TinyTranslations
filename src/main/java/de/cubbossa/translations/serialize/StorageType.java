package de.cubbossa.translations.serialize;

import de.cubbossa.translations.LanguageFileHandle;
import de.cubbossa.translations.StyleFileHandle;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

public enum StorageType {
    SQL,
    YML,
    PROPERTIES;

    public LanguageFileHandle languageFileHandle() {
        return new PropertiesFileHandle(Logger.getLogger("test"), new File("src/main/resources/"));
    }

    public StyleFileHandle styleFileHandle() {
        return new PropertiesFileHandle(Logger.getLogger("test"), new File("src/main/resources/"));
    }
}
