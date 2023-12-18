package de.cubbossa.translations.util;

import java.util.List;

public record Entry(@org.intellij.lang.annotations.Pattern("[a-zA-Z0-9._-]+") String key, String value, List<String> comments) {
}
