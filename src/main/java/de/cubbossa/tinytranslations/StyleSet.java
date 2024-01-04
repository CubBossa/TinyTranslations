package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;

public class StyleSet extends HashMap<String, MessageStyle> {

    private final StyleDeserializer deserializer;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public StyleSet() {
        deserializer = new StyleDeserializerImpl();
    }

    public void put(String key, TagResolver styleResolver) {
        this.put(key, new MessageStyleImpl(key, styleResolver));
    }

    public void put(String key, Style style) {
        this.put(key, new MessageStyleImpl(key, TagResolver.resolver(key, Tag.styling(s -> s.merge(style))),
                miniMessage.serialize(Component.text("").style(style))));
    }

    public void put(String key, String serializedStyle) {
        this.put(key, new MessageStyleImpl(key, deserializer.deserialize(key, serializedStyle), serializedStyle));
    }

}
