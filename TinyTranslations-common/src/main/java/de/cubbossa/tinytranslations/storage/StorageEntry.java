package de.cubbossa.tinytranslations.storage;

import org.intellij.lang.annotations.Pattern;

public record StorageEntry(@Pattern("[a-zA-Z0-9._:-]+") String key, String value, String comment) {
}
