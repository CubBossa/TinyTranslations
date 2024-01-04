package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface StyleDeserializer {

    TagResolver deserialize(String key, String string);
}
