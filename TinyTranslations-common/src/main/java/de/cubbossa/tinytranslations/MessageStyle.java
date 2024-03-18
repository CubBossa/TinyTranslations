package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;

import java.io.Serializable;

/**
 * A {@link TagResolver} that consists of a key and its replacement and that can be represented by a string.
 * MessageTranslators resolve MessageStyles recursively.
 * <p>
 * The following properties snippet explains this
 * <pre>
 *     message = <style_a>abc</style_a>
 *     style_a = <style_b><slot/></style_b>
 *     style_c = <red><slot/></red>
 * </pre>
 * resolving message will produce
 * <pre>
 *     <red>abc</red>
 * </pre>
 *
 */
public interface MessageStyle extends Key, Serializable, TagResolver {

    static MessageStyle messageStyle(String key, @Language("NanoMessage") String representation) {
        return new MessageStyleImpl(key, representation);
    }

    /**
     * @return The key that represents the MessageStyle in storages and serves as tag name in MiniMessage context.
     */
    @KeyPattern
    String getKey();
}
