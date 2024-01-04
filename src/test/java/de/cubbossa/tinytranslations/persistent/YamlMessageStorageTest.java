package de.cubbossa.tinytranslations.persistent;

import java.io.File;

public class YamlMessageStorageTest extends MessageStorageTest {
    @Override
    MessageStorage getMessageStorage(File dir) {
        return new YamlMessageStorage(dir);
    }
}
