package de.cubbossa.translations.persistent;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface StylesStorage {

    void writeStyles(Map<String, Style> styles);

    Map<String, Style> loadStyles();
}
