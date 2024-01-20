package de.cubbossa.tinytranslations.storage;

import java.util.List;

public record StorageEntry(@org.intellij.lang.annotations.Pattern("[a-zA-Z0-9._-]+") String key, String value, List<String> comments) {
}
