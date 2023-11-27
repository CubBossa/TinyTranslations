package de.cubbossa.translations.persistent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesMessageStorageTest extends MessageStorageTest{

    public MessageStorage getMessageStorage(File dir) {
        return new PropertiesMessageStorage(dir);
    }
}