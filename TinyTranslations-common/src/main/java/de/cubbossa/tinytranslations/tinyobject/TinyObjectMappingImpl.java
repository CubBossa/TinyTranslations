package de.cubbossa.tinytranslations.tinyobject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class TinyObjectMappingImpl implements TinyObjectMapping {

    public static final String TAG_PLACEHOLDER_NAME = "avoid_name_collisions";

    protected final Class<?> match;
    protected Function<Object, Object> overrideAll = null;
    protected FallbackContextConsumer fallbackContextConsumer = (value, context, argumentQueue) -> value.toString();
    protected final Map<String, Function<Object, Object>> productions = new HashMap<>();

    public TinyObjectMappingImpl(Class<?> match) {
        this.match = match;
    }

    @Override
    public String toString() {
        return "TinyObjectMappingImpl{" +
                "match=" + match +
                '}';
    }

    @Override
    public boolean matches(Object obj) {
        return obj != null && match.isAssignableFrom(obj.getClass());
    }

    @Override
    public boolean containsKey(String key) {
        return productions.containsKey(key);
    }

    @Override
    public @Nullable Object resolve(@Nullable Object value) {
        return resolve(value, "");
    }

    @Override
    public @Nullable Object resolve(@Nullable Object value, String key) {
        if (value == null) {
            return null;
        }
        if (overrideAll != null) {
            return overrideAll.apply(value);
        }

        var fun = productions.get(key);
        if (fun != null) {
            return fun.apply(value);
        }
        if (Objects.equals(key, "") && fallbackContextConsumer != null) {
            return TagResolver.resolver(TAG_PLACEHOLDER_NAME, (argumentQueue, context) -> {
                var r = fallbackContextConsumer.apply(value, context, argumentQueue);
                if (r instanceof Tag tag) {
                    return tag;
                }
                if (r instanceof ComponentLike c) {
                    return Tag.inserting(c);
                }
                return Tag.inserting(Component.text(r.toString()));
            });
        }
        return null;
    }
}
