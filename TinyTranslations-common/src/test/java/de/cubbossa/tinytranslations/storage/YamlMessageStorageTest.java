package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.storage.yml.YamlMessageStorage;

import java.io.File;

public class YamlMessageStorageTest extends MessageStorageTest {
    @Override
    String fileName(String languageTag) {
        return languageTag + ".yml";
    }

    @Override
    MessageStorage getMessageStorage(File dir) {
        return new YamlMessageStorage(dir);
    }
}
