package de.cubbossa.translations.serialize;

import de.cubbossa.translations.LocalesStorage;
import de.cubbossa.translations.StylesStorage;

import java.io.File;
import java.util.logging.Logger;

public enum StorageType {
    SQL,
    YML,
    PROPERTIES;

    public LocalesStorage languageFileHandle() {
        return new PropertiesStorage(Logger.getLogger("test"), new File("src/main/resources/"));
    }

    public StylesStorage styleFileHandle() {
        return new PropertiesStorage(Logger.getLogger("test"), new File("src/main/resources/"));
    }
}
