package de.cubbossa.translations.persistent;

import java.io.File;

public class PropertiesStyleStorageTest extends StyleStorageTest {
    @Override
    StyleStorage getStyleStorage(File dir) {
        return new PropertiesStyleStorage(dir);
    }
}
