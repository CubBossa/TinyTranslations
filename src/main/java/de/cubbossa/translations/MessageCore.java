package de.cubbossa.translations;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public final class MessageCore implements Message {

    private static final MiniMessage S_MM = MiniMessage.builder().build();
    private static final GsonComponentSerializer S_GSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer S_LEGACY = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer S_LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();

    public enum Format {

        GSON("nbt", S_GSON::deserialize),
        MINI_MESSAGE("minimessage", (s, r) -> S_MM.deserialize(s, r)),
        LEGACY("paragraph", S_LEGACY::deserialize),
        LEGACY_AMP("ampersand", S_LEGACY_AMP::deserialize),
        PLAIN;

        private static final Pattern PREFIX = Pattern.compile("^(!!([a-z_]+): )?((.|\n)*)");

        private final String prefix;

        private final BiFunction<String, TagResolver, Component> translator;

        Format() {
            this.prefix = toString().toLowerCase();
            this.translator = (s, tagResolver) -> Component.text(s);
        }

        Format(String prefix, Function<String, Component> translator) {
            this.prefix = prefix;
            this.translator = (s, tagResolver) -> translator.apply(s);
        }

        Format(String prefix, BiFunction<String, TagResolver, Component> translator) {
            this.prefix = prefix;
            this.translator = translator;
        }

        public String toPrefix() {
            return "!!" + prefix + ": ";
        }

        public static Component translate(String message, TagResolver resolver) {
            Matcher matcher = PREFIX.matcher(message);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Message is invalid");
            }
            String prefix = matcher.group(2);
            String m = matcher.group(3);
            Format format = m == null
                    ? MINI_MESSAGE
                    : fromPrefix(prefix).orElse(MINI_MESSAGE);
            return format.translator.apply(m == null ? prefix : m, resolver);
        }

        public static Optional<Format> fromPrefix(String prefix) {
            for (Format value : Format.values()) {
                if (value.prefix.equals(prefix)) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }

    }

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
    public String getNamespacedKey() {
        return translations.getPath() + ":" + key;
    }

    @Override
    public void setTranslations(@NotNull Translations translations) {
        this.translations = translations;
    }

    @Override
    public @NotNull Component asComponent() {
        return translations.process(this);
    }

    @Override
    public @NotNull Component asComponent(Audience audience) {
        return translations.process(this, audience);
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
        return clone(translations);
    }

    @Override
    public int compareTo(@NotNull Message o) {
        return getKey().compareTo(o.getKey());
    }
}
