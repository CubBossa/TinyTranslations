package de.cubbossa.translations.persistent;

import net.kyori.adventure.text.format.Style;

import java.util.Map;

public interface StyleStorage {

    void writeStyles(Map<String, Style> styles);

    Map<String, Style> loadStyles();
}
