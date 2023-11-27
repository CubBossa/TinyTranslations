package de.cubbossa.translations;

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

    private final String key;

    private Translations translations;
    private Map<Locale, String> dictionary;
    private Map<String, Optional<String>> placeholderTags;
    private String comment;

    public MessageCore(String key) {
        this((Translations) null, key);
    }

    public MessageCore(Translations translations, String key) {
        this(translations, key, "No default translation present");
    }

    public MessageCore(String key, String defaultValue) {
        this(null, key, defaultValue);
    }

    public MessageCore(Translations translations, String key, String defaultValue) {
        this.translations = translations;
        if (translations != null) {
            this.translations.getMessageSet().put(this.getKey(), this);
        }
        this.key = key;
        this.dictionary = new ConcurrentHashMap<>();
        this.dictionary.put(TranslationsFramework.DEFAULT_LOCALE, defaultValue);

        this.placeholderTags = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Message<" + getNamespacedKey() + ">";
    }

    @Override
    public String getNamespacedKey() {
        if (translations == null) {
            throw new IllegalStateException("Trying to access a Message before registering it to a Translations instance.");
        }
        return translations.getPath() + ":" + key;
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
        return key.equals(message.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public Message clone(Translations translations) {
        Message message = new MessageCore(translations, key);
        message.setPlaceholderTags(new HashMap<>(placeholderTags));
        message.setComment(comment);
        return message;
    }

    @Override
    public Message clone() {
        if (translations == null) {
            throw new IllegalStateException("Trying to clone a Message before registering it to a Translations instance.");
        }
        return clone(translations);
    }

    @Override
    public int compareTo(@NotNull Message o) {
        return getKey().compareTo(o.getKey());
    }
}
