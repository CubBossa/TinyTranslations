package de.cubbossa.translations.persistent;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface StylesStorage {

    void writeStyles(Map<String, Style> styles);

    default TagResolver loadStylesAsResolver() {
        return TagResolver.resolver(loadStyles().entrySet().stream()
            .map(e -> TagResolver.resolver(e.getKey(), (argumentQueue, context) -> {
                return Tag.styling(style -> style.merge(e.getValue()));
            }))
            .collect(Collectors.toSet()));
    }

    Map<String, Style> loadStyles();
}
