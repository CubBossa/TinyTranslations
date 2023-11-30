package de.cubbossa.translations.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class DefaultResolvers {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://)?[^:/]+/(.+)");

    public Tag shortURL(int a, int b) {
        if (a < 0 || b < 0) {
            throw new IllegalArgumentException("a and b must be greater or equals than 0. a=" + a + ", b=" + b);
        }
        return (Modifying) (current, depth) -> {
            if (depth > 0) return Component.empty();
            String content = PLAIN.serialize(current);
            Matcher matcher = URL_PATTERN.matcher(content);
            if (!matcher.matches()) {
                return current;
            }
            String tail = matcher.group(2);
            int tailLen = tail.length();
            if (a > tailLen || b > tailLen || a + b > tailLen) {
                return current;
            }
            return current.replaceText(TextReplacementConfig.builder()
                .matchLiteral(tail)
                .replacement(tail.substring(0, a) + "..." + tail.substring(tail.length() - b))
                .build());
        };
    }

    public Tag preview(int length) {
        AtomicInteger dots = new AtomicInteger();
        return (Modifying) (current, depth) -> mapChildren(current, c -> modifyText(c, s -> {
            String result = s;
            if (s.length() + dots.get() > length - 3) {
                result = s.substring(0, Integer.min(s.length(), Integer.max(0, length - dots.get())));
            }
            dots.addAndGet(s.length());
            return result;
        }));
    }

    public Tag lower() {
        return (Modifying) (current, depth) -> mapChildren(current, c-> modifyText(current, String::toLowerCase));
    }

    public Tag upper() {
        return (Modifying) (current, depth) -> modifyText(current, String::toUpperCase);
    }

    private Component mapChildren(Component c, Function<Component, Component> consumer) {
        return Component.empty().children(c.children().stream().map(consumer).toList());
    }

    private Component modifyText(Component c, Function<String, String> modifier) {
        c = c.children(c.children().stream().map(comp -> modifyText(comp, modifier)).toList());
        return c instanceof TextComponent tc ? tc.content(modifier.apply(tc.content())) : c;
    }
}
