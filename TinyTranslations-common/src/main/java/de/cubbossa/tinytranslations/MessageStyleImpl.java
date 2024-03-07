package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MessageStyleImpl implements MessageStyle {

    private final String key;
    private final String representation;
    private final TagResolver resolver;

    public MessageStyleImpl(String key, String representation) {
        this.key = key;
        if (!representation.contains("{slot}")) {
            representation = representation + "{slot}";
        }
        this.representation = representation;

        resolver = TagResolver.resolver(key, (argumentQueue, ctx) -> (Modifying) (c, depth) -> {
            if (depth > 0) return Component.empty();

            Matcher matcher = Pattern.compile("\\{arg([0-9]+)}").matcher(this.representation);
            int highest = -1;
            while (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                if (x > highest) {
                    highest = x;
                }
            }
            Collection<TagResolver> r = new ArrayList<>();
            int i = 0;
            while (i <= highest && argumentQueue.hasNext()) {
                r.add(Placeholder.component("arg" + i++, ctx.deserialize(argumentQueue.pop().value())));
            }
            r.add(Placeholder.component("slot", c));

            return ctx.deserialize(this.representation, r.toArray(TagResolver[]::new));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageStyleImpl that = (MessageStyleImpl) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public @NotNull String namespace() {
        return null;
    }

    @Override
    public @NotNull String value() {
        return representation;
    }

    @Override
    public @NotNull String asString() {
        return representation;
    }

    @Override
    public String toString() {
        return representation;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        return resolver.resolve(name, arguments, ctx);
    }

    @Override
    public boolean has(@NotNull String name) {
        return key.equalsIgnoreCase(name);
    }
}
