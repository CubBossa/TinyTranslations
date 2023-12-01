package de.cubbossa.translations.persistent;

import java.io.File;

class PropertiesMessageStorageTest extends MessageStorageTest{

    public MessageStorage getMessageStorage(File dir) {
        return new PropertiesMessageStorage(dir);
    }
}