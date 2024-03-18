package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translatable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The representation of one translation in a translation file.
 * Messages can be translated by {@link MessageTranslator} instances.
 * They implement the {@link Translatable} interface and are therefore also
 * translatable via MiniMessage tag <pre><translate:[message-namespaced-key]/></pre>
 */
public interface Message extends ComponentLike, Cloneable, Comparable<Message>, Formattable<Message>, Translatable, TranslatableComponent {

    /**
     * Creates an {@link UnownedMessage} instance with the provided key. The message must be added to a
     * {@link MessageTranslator} to be of use.
     *
     * @param key The message key, not including any namespace.
     * @return The new {@link UnownedMessage} instance.
     */
    static Message message(String key) {
        return new UnownedMessageImpl(key);
    }

    /**
     * Creates an {@link UnownedMessage} instance with the provided key. The message must be added to a
     * {@link MessageTranslator} to be of use.
     *
     * @param key          The message key, not including any namespace.
     * @param defaultValue The translation of the default locale, most likely english.
     * @return The new {@link UnownedMessage} instance.
     */
    static Message message(String key, @Language("NanoMessage") String defaultValue) {
        return new MessageBuilder(key).withDefault(defaultValue).build();
    }

    /**
     * Creates a new {@link MessageBuilder}, that will help to create a new {@link UnownedMessage} instance.
     * The message intance must be added to a {@link MessageTranslator} to become of use.
     *
     * @param key The message key, not including any namespace.
     * @return a new {@link MessageBuilder}
     */
    static MessageBuilder builder(@KeyPattern String key) {
        return new MessageBuilder(key);
    }

    /**
     * @return the {@link TranslationKey} instance, with the message key as key and the owning {@link MessageTranslator}s key
     * as namespace.
     */
    TranslationKey getKey();

    /**
     * Converts this message into a string value, resolving all appended resolvers.
     * @param format The format to use, use constant {@link MessageEncoding} fields.
     * @return The string value
     */
    String toString(MessageEncoding format);

    /**
     * Converts this message into a string value, resolving all appended resolvers.
     * @param format The format to use, use constant {@link MessageEncoding} fields.
     * @param locale The locale to use to retrieve the string value from the dictionary.
     * @return The string value
     */
    String toString(MessageEncoding format, Locale locale);

    /**
     * @return The translation dictionary for this message, mapping locale to translation in NanoMessage format.
     * A reference is being returned, changes affect this message directly.
     */
    Map<Locale, String> getDictionary();

    /**
     * @return The map of placeholder keys and optionally their description. A reference is being returned,
     * changes affect this message directly.
     */
    Map<String, Optional<String>> getPlaceholderTags();

    /**
     * Replaces the existing map of placeholder keys and their optional description with the given value.
     * @param placeholderTags The new map of placeholders and their description.
     */
    void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

    /**
     * @return A comment string for this message or null if no comment set.
     */
    @Nullable String getComment();

    /**
     * Sets the comment for this message. Use '\n' to create multiline comments.
     *
     * @param comment The comment string or null to remove any existing comment.
     */
    void setComment(@Nullable String comment);
}
