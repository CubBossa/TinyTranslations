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

public interface Message extends ComponentLike, Cloneable, Comparable<Message>, Formattable<Message>, Translatable, TranslatableComponent {

    static Message message(String key) {
        return new UnownedMessageImpl(key);
    }

    static Message message(String key, @Language("NanoMessage") String defaultValue) {
        return new MessageBuilder(key).withDefault(defaultValue).build();
    }

    static MessageBuilder builder(@KeyPattern String key) {
        return new MessageBuilder(key);
    }


    TranslationKey getKey();

    String toString(MessageEncoding format);

    String toString(MessageEncoding format, Locale locale);


    Map<Locale, String> getDictionary();

    Map<String, Optional<String>> getPlaceholderTags();

    void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

    @Nullable String getComment();

    void setComment(String comment);
}
