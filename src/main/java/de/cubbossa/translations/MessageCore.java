package de.cubbossa.translations;

import de.cubbossa.translations.annotation.KeyPattern;
import de.cubbossa.translations.annotation.PathPattern;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public final class MessageCore implements Message {

    private final @KeyPattern String key;

    private Translations translations;
    private Map<Locale, String> dictionary;
    private Map<String, Optional<String>> placeholderTags;
    private String comment;

    public MessageCore(@KeyPattern String key) {
        this((Translations) null, key);
    }

    public MessageCore(Translations translations, @KeyPattern String key) {
        this(translations, key, "No default translation present");
    }

    public MessageCore(@KeyPattern String key, String defaultValue) {
        this(null, key, defaultValue);
    }

    public MessageCore(Translations translations, @KeyPattern String key, String defaultValue) {
        this.translations = translations;
        this.key = key;
        this.dictionary = new ConcurrentHashMap<>();
        this.dictionary.put(TranslationsFramework.DEFAULT_LOCALE, defaultValue);

        this.placeholderTags = new HashMap<>();

        if (translations != null) {
            this.translations.getMessageSet().put(key, this);
        }
    }

    @Override
    public String toString() {
        return "Message<" + getNamespacedKey().toLowerCase() + ">";
    }

    @KeyPattern
    public String getKey() {
        return key;
    }

    @PathPattern
    @Override
    public String getNamespacedKey() {
        return (translations == null ? "" : translations.getPath()) + ":" + key;
    }

    @Override
    public void setTranslations(@NotNull Translations translations) {
        this.translations = translations;
    }

    @Override
    public @NotNull Component asComponent() {
        if (translations == null) {
            throw new IllegalStateException("Trying to translate a Message before registering it to a Translations instance.");
        }
        return translations.process(this, getTarget());
    }

    @Override
    public String toString(MessageFormat format) {
        return format.format(asComponent());
    }

    @Override
    public @Nullable Audience getTarget() {
        return null;
    }

    @Override
    public Collection<TagResolver> getResolvers() {
        return Collections.emptyList();
    }

    @Override
    public Message formatted(Audience audience) {
        if (audience == null) {
            return this;
        }
        FormattedMessage message = new FormattedMessage(this);
        message.audience = audience;
        return message;
    }

    @Override
    public Message formatted(TagResolver... resolver) {
        if (resolver.length == 0) {
            return this;
        }
        FormattedMessage message = new FormattedMessage(this);
        message.resolvers.addAll(List.of(resolver));
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageCore message = (MessageCore) o;
        return key.equalsIgnoreCase(message.key);
    }

    @Override
    public int hashCode() {
        return key.toLowerCase().hashCode();
    }

    public Message clone(Translations translations) {
        Message message = new MessageCore(translations, key);
        message.setPlaceholderTags(new HashMap<>(placeholderTags));
        message.setComment(comment);
        return message;
    }

    @Override
    public Message clone() {
        return clone(translations);
    }

    @Override
    public int compareTo(@NotNull Message o) {
        return getKey().compareTo(o.getKey());
    }
}
