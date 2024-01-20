package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.storage.yml.YamlStyleStorage;

import java.io.File;

public class YamlStyleStorageTest extends StyleStorageTest {
  @Override
  StyleStorage getStyleStorage(File dir, String name) {
    return new YamlStyleStorage(new File(dir, name + ".yml"));
  }
}
