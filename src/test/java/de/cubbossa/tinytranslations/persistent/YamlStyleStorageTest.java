package de.cubbossa.tinytranslations.persistent;

import java.io.File;

public class YamlStyleStorageTest extends StyleStorageTest {
  @Override
  StyleStorage getStyleStorage(File dir, String name) {
    return new YamlStyleStorage(new File(dir, name + ".yml"));
  }
}
