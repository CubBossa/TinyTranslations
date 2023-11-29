package de.cubbossa.translations.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@UtilityClass
public class DefaultResolvers {

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
