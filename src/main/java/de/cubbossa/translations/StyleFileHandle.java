package de.cubbossa.translations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;

public interface StyleFileHandle {

    Collection<TagResolver> loadStyles();
}
