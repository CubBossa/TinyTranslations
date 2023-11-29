package de.cubbossa.translations;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface StyleSerializer {

    String serialize(Style style);

    TagResolver deserialize(String key, String string);
}
