package de.cubbossa.translations;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public final class Message implements ComponentLike, Cloneable, Comparable<Message> {

    private static final MiniMessage S_MM = MiniMessage.miniMessage();
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

	private String defaultValue;
	private Map<Locale, String> defaultTranslations;
	private String comment;
	private Map<String, Optional<String>> placeholderTags;
	private Collection<TagResolver> placeholderResolvers;
	private Translator translator;
    public Message(String key, Translator translator) {
        this(key, "No default translation present", translator);
    }

    public Message(String key, String defaultValue, Translator translator) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.defaultTranslations = new HashMap<>();
        this.placeholderTags = new HashMap<>();
        this.placeholderResolvers = new HashSet<>();
        this.translator = translator;
    }

    @Override
    public @NotNull Component asComponent() {
        return translator.translate(this);
    }

    public Component asComponent(Audience audience) {
        return translator.translate(this, audience);
    }

    public Message format(TagResolver... resolver) {
        this.placeholderResolvers.add(TagResolver.resolver(resolver));
        return this;
    }

    public Message formatted(TagResolver... resolver) {
        return this.clone().format(resolver);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;
        return key.equals(message.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public Message clone() {
        Message message = new Message(key, defaultValue, translator);
        message.setPlaceholderTags(new HashMap<>(placeholderTags));
        message.setPlaceholderResolvers(new HashSet<>(placeholderResolvers));
        message.setComment(comment);
        return message;
    }

	@Override
	public int compareTo(@NotNull Message o) {
		return getKey().compareTo(o.getKey());
	}
}
