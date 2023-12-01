package de.cubbossa.translations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface StyleDeserializer {

    TagResolver deserialize(String key, String string);
}
