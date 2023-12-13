package de.cubbossa.translations.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class DefaultResolvers {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://)?[^:/]+/(.+)");

    public TagResolver darken(String key) {
        return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (current, depth) -> {
            if (current.color() == null) return current;
            return current.color(TextColor.color(new Color(current.color().value()).darker().getRGB()));
        });
    }

    public TagResolver lighten(String key) {
        return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (current, depth) -> {
            if (current.color() == null) return current;
            return current.color(TextColor.color(new Color(current.color().value()).brighter().getRGB()));
        });
    }

    public TagResolver repeat(String key) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            int count = argumentQueue.hasNext() ? argumentQueue.pop().asInt().orElse(2) : 2;
            return (Modifying) (current, depth) -> {
                if (depth > 0) return Component.empty();
                Component c = Component.empty();
                for (int i = 0; i < count; i++) {
                    c = c.append(current);
                }
                return c;
            };
        });
    }

    public TagResolver reverse(String key) {
        return TagResolver.resolver(key, (Modifying) (current, depth) -> {
            if (depth > 0) return Component.empty();
            return reverse(current);
        });
    }

    private Component reverse(Component c) {
        if (c instanceof TextComponent tc) {
            c = tc.content(new StringBuilder(tc.content()).reverse().toString());
        }
        if (c.children().isEmpty()) {
            return c;
        }

        Component root = Component.empty().style(c.style());

        int size = c.children().size();
        for (int i = 0; i < size; i++) {
            root = root.append(reverse(c.children().get(size - i - 1)));
        }
        return root.append(c.children(new ArrayList<>()));
    }

    public TagResolver shortUrl(String key) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            int a = argumentQueue.hasNext() ? argumentQueue.pop().asInt().orElse(3) : 3;
            int b = argumentQueue.hasNext() ? argumentQueue.pop().asInt().orElse(3) : 3;
            return shortURLTag(a, b);
        });
    }
    public Tag shortURLTag(int a, int b) {
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

    public TagResolver preview(String key) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            int length = argumentQueue.hasNext() ? argumentQueue.pop().asInt().orElse(20) : 20;
            return previewTag(length);
        });
    }

    public Tag previewTag(int length) {
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

    public TagResolver lower(String key) {
        return TagResolver.resolver(key, lowerTag());
    }

    public TagResolver upper(String key) {
        return TagResolver.resolver(key, upperTag());
    }

    public Tag lowerTag() {
        return (Modifying) (current, depth) -> mapChildren(current, c-> modifyText(current, String::toLowerCase));
    }

    public Tag upperTag() {
        return (Modifying) (current, depth) -> mapChildren(current, c-> modifyText(current, String::toUpperCase));
    }

    private Component mapChildren(Component c, Function<Component, Component> consumer) {
        return Component.empty().children(c.children().stream().map(consumer).toList());
    }

    private Component modifyText(Component c, Function<String, String> modifier) {
        c = c.children(c.children().stream().map(comp -> modifyText(comp, modifier)).toList());
        return c instanceof TextComponent tc ? tc.content(modifier.apply(tc.content())) : c;
    }
}
