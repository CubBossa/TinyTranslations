package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;

import java.io.Serializable;

public interface MessageStyle extends Key, Serializable, TagResolver {

    static MessageStyle messageStyle(String key, @Language("NanoMessage") String representation) {
        return new MessageStyleImpl(key, representation);
    }

    @KeyPattern
    String getKey();

    String toString();
}
