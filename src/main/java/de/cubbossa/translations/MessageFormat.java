package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageFormat {

    private static final MiniMessage S_MM = MiniMessage.builder().build();
    private static final GsonComponentSerializer S_GSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer S_LEGACY = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer S_LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer S_PLAIN = PlainTextComponentSerializer.plainText();
    private static final Pattern PREFIX = Pattern.compile("^(!!([a-z_]+): )?((.|\n)*)");

    public static final MessageFormat GSON = new MessageFormat("nbt", S_GSON::deserialize);
    public static final MessageFormat MINI_MESSAGE = new MessageFormat("minimessage", (s, r) -> S_MM.deserialize(s, r));
    public static final MessageFormat LEGACY = new MessageFormat("paragraph", S_LEGACY::deserialize);
    public static final MessageFormat LEGACY_AMP = new MessageFormat("ampersand", S_LEGACY_AMP::deserialize);
    public static final MessageFormat PLAIN = new MessageFormat("plain", S_PLAIN::deserialize);
    private static final MessageFormat[] VALUES = { GSON, MINI_MESSAGE, LEGACY, LEGACY_AMP, PLAIN };

    public static MessageFormat[] values() {
        return VALUES;
    }

    private final String prefix;
    private final BiFunction<String, TagResolver, Component> translator;

    MessageFormat(String prefix, Function<String, Component> translator) {
        this.prefix = prefix;
        this.translator = (s, tagResolver) -> translator.apply(s);
    }

    MessageFormat(String prefix, BiFunction<String, TagResolver, Component> translator) {
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
        MessageFormat messageFormat = m == null
                ? MINI_MESSAGE
                : fromPrefix(prefix).orElse(MINI_MESSAGE);
        return messageFormat.translator.apply(m == null ? prefix : m, resolver);
    }

    public static Optional<MessageFormat> fromPrefix(String prefix) {
        for (MessageFormat value : MessageFormat.values()) {
            if (value.prefix.equals(prefix)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

}
