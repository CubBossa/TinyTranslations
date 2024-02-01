package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.intellij.lang.annotations.Language;

import java.util.HashMap;

public class StyleSet extends HashMap<String, MessageStyle> {

    private final MiniMessage miniMessage = MiniMessage.builder().strict(true).build();

    public StyleSet() {
        super();
    }

    public void put(String key, Style style) {
        String representation = miniMessage.serialize(Component.text("{slot}", style));
        put(key, representation);
    }

    public void put(String key, @Language("NanoMessage") String serializedStyle) {
        this.put(key, MessageStyle.messageStyle(key, serializedStyle));
    }
}
