package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
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
final class MessageImpl implements Message {

    private final @KeyPattern String key;

    private Style style = Style.empty();
    private List<Component> children = new ArrayList<>();
    private final List<TranslationArgument> arguments = Collections.emptyList();
    private final Collection<TagResolver> resolvers = new ArrayList<>();

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
        this.style = other.style.color(other.style.color());
        this.children = other.children().stream().map(c -> c.children(c.children())).toList();
        this.dictionary = new HashMap<>(other.dictionary);
        this.fallback = other.fallback;
        this.placeholderTags = new HashMap<>(other.placeholderTags);
        this.comment = other.comment;
        this.resolvers.addAll(other.resolvers);
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
    public String toString(MessageEncoding format) {
        return format.format(asComponent());
    }

    @Override
    public String toString(MessageEncoding format, Locale locale) {
        return toString(format);
    }

    @Override
    public Collection<TagResolver> getResolvers() {
        return new ArrayList<>(resolvers);
    }

    @Override
    public Message formatted(TagResolver... resolver) {
        MessageImpl message = new MessageImpl(key, this);
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
