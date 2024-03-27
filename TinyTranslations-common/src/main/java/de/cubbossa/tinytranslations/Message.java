package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.storage.Commented;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translatable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The representation of one translation in a translation file.
 * Messages can be translated by {@link MessageTranslator} instances.
 * They implement the {@link Translatable} interface and are therefore also
 * translatable via MiniMessage tag <pre><translate:[message-namespaced-key]/></pre>
 */
public interface Message extends ComponentLike, Cloneable, Comparable<Message>, Formattable<Message>, Commented<Message>, Translatable, TranslatableComponent {

    String TEMPORARY_MESSAGE_KEY = "__anonymous__";

    /**
     * Creates an {@link UnownedMessage} that is being recognized as contextual and will only be rendered by the {@link MessageTranslator}
     * that renders the {@link Message} which contains this message. A contextual {@link Message} will not be rendered on its own.
     * @param content The content to render for default
     * @return The created {@link UnownedMessage} instance.
     */
    static Message contextual(@Language("NanoMessage") String content) {
        return new MessageBuilder(TEMPORARY_MESSAGE_KEY)
                .withDefault(content)
                .build();
    }

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
    @Deprecated(forRemoval = true, since = "4.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    Map<Locale, String> getDictionary();

    /**
     * @return The map of placeholder keys and optionally their description. A reference is being returned,
     * changes affect this message directly.
     */
    @Deprecated(forRemoval = true, since = "4.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    Map<String, Optional<String>> getPlaceholderDescriptions();

    /**
     * Replaces the existing map of placeholder keys and their optional description with the given value.
     * @param placeholderDescriptions The new map of placeholders and their description.
     */
    @Deprecated(forRemoval = true, since = "4.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    void setPlaceholderDescriptions(Map<String, Optional<String>> placeholderDescriptions);

    /**
     * @return A comment string for this message or null if no comment set.
     */
    @Deprecated(forRemoval = true, since = "4.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Nullable String getComment();

    /**
     * Sets the comment for this message. Use '\n' to create multiline comments.
     *
     * @param comment The comment string or null to remove any existing comment.
     */
    @Deprecated(forRemoval = true, since = "4.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    void setComment(@Nullable String comment);

    Map<Locale, String> dictionary();

    @Contract(pure = true)
    Message dictionary(Map<Locale, String> dictionary);

    @Contract(pure = true)
    default Message dictionaryEntry(Locale locale, String translation) {
        var map = new HashMap<>(dictionary());
        map.put(locale, translation);
        return dictionary(map);
    }

    Collection<PlaceholderDescription> placeholderDescriptions();

    Message placeholderDescriptions(Collection<PlaceholderDescription> descriptions);

    record PlaceholderDescription(String[] names, @Nullable String description, Class<?> type) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlaceholderDescription that = (PlaceholderDescription) o;
            return Arrays.equals(names, that.names) && Objects.equals(description, that.description) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(description, type);
            result = 31 * result + Arrays.hashCode(names);
            return result;
        }
    }
}
