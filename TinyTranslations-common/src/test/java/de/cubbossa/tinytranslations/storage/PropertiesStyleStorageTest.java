package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;

import java.io.File;

public class PropertiesStyleStorageTest extends StyleStorageTest {
    @Override
    StyleStorage getStyleStorage(File dir, String name) {
        return new PropertiesStyleStorage(new File(dir, name + ".properties"));
    }
}
