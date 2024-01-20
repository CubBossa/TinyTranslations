package de.cubbossa.tinytranslations.impl;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageFormat;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.nanomessage.tag.NanoResolver;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public final class MessageImpl implements Message {

    private final @KeyPattern String key;

    private Style style = Style.empty();
    private List<Component> children = new ArrayList<>();
    private final List<TranslationArgument> arguments = Collections.emptyList();
    private final Collection<TagResolver> resolvers = new ArrayList<>();
    private final Collection<NanoResolver> nanoResolvers = new ArrayList<>();

    private Map<Locale, String> dictionary;
    private String fallback;

    private Map<String, Optional<String>> placeholderTags;
    private String comment;

    public MessageImpl(@KeyPattern String key) {
        this(key, key);
    }

    public MessageImpl(@KeyPattern String key, String fallback) {
        this.key = key;
        this.dictionary = new ConcurrentHashMap<>();
        this.dictionary.put(TinyTranslations.DEFAULT_LOCALE, fallback);

        this.placeholderTags = new HashMap<>();
    }

    public MessageImpl(String key, MessageImpl other) {
        this.key = key;
        this.style = other.style;
        this.children = other.children().stream().map(c -> c.children(c.children())).toList();
        this.dictionary = new HashMap<>(other.dictionary);
        this.fallback = other.fallback;
        this.placeholderTags = new HashMap<>(other.placeholderTags);
        this.comment = other.comment;
    }

    @KeyPattern
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Message{key='" + getKey().toLowerCase() + "'}";
    }

	@Override
    public String toString(MessageFormat format) {
        return format.format(asComponent());
    }

    @Override
    public String toString(MessageFormat format, Locale locale) {
        return toString(format);
    }

    @Override
    public Collection<NanoResolver> getResolvers() {
        var result = new ArrayList<>(nanoResolvers);
        result.addAll(resolvers.stream().map(resolver -> (NanoResolver) c -> resolver).toList());
        return result;
    }

    @Override
    public Message formatted(TagResolver... resolver) {
        MessageImpl message = new MessageImpl(key, this);
        message.resolvers.addAll(List.of(resolver));
        return message;
    }

    @Override
    public Message formatted(NanoResolver... resolver) {
        MessageImpl message = new MessageImpl(key, this);
        message.nanoResolvers.addAll(List.of(resolver));
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

        MessageImpl message = (MessageImpl) o;
        return key.equalsIgnoreCase(message.key);
    }

    @Override
    public int hashCode() {
        return key.toLowerCase().hashCode();
    }

    @Override
    public Message clone() {
        return new MessageImpl(key, this);
    }

    @Override
    public int compareTo(@NotNull Message o) {
        return getKey().compareTo(o.getKey());
    }

    @Override
    public @NotNull String translationKey() {
        return key;
    }

    @Override
    public @NotNull String key() {
        return key;
    }

    @Override
    public @NotNull TranslatableComponent key(@NotNull String key) {
		return new MessageImpl(key, this);
    }

    @Override
    public @NotNull List<Component> args() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<TranslationArgument> arguments() {
        return arguments;
    }

    @Override
    public @NotNull TranslatableComponent arguments(@NotNull ComponentLike @NotNull ... args) {
        return new MessageImpl(key, this);
    }

    @Override
    public @NotNull TranslatableComponent arguments(@NotNull List<? extends ComponentLike> args) {
        return new MessageImpl(key, this);
    }

    @Override
    public @Nullable String fallback() {
        return fallback;
    }

    @Override
    public @NotNull TranslatableComponent fallback(@Nullable String fallback) {
        var clone = new MessageImpl(key, this);
        clone.fallback = fallback;
        return clone;
    }

    @Override
    public @NotNull Builder toBuilder() {
        return null;
    }

    @Override
    public @Unmodifiable @NotNull List<Component> children() {
        return new ArrayList<>(children);
    }

    @Override
    public @NotNull TranslatableComponent children(@NotNull List<? extends ComponentLike> children) {
        var clone = new MessageImpl(key, this);
        clone.children = children.stream().map(ComponentLike::asComponent).toList();
        return clone;
    }

    @Override
    public @NotNull Style style() {
        return style;
    }

    @Override
    public @NotNull TranslatableComponent style(@NotNull Style style) {
        var clone = new MessageImpl(key, this);
        clone.style = style;
        return clone;
    }
}
