package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TinyObjectResolverImpl implements TinyObjectResolver {

    protected final Class<?> match;
    protected final Map<String, Function<Object, Object>> productions = new HashMap<>();

    public TinyObjectResolverImpl(Class<?> match) {
        this.match = match;
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
    public @Nullable Object resolve(@Nullable Object value, String key) {
        if (value == null) {
            return null;
        }
        var fun = productions.get(key);
        if (fun != null) {
            return fun.apply(value);
        }
        fun = productions.get("");
        if (fun != null) {
            return fun.apply(value);
        }
        return null;
    }

    @Override
    public int compareTo(@NotNull TinyObjectResolver o) {
        if (o instanceof TinyObjectResolverImpl i) {
            if (i.match.isAssignableFrom(match)) {
                return -1;
            }
        }
        return 1;
    }
}
