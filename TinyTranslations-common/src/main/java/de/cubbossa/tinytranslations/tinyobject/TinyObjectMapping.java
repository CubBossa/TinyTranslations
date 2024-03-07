package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface TinyObjectMapping extends Comparable<TinyObjectMapping> {

    boolean matches(Object obj);

    boolean containsKey(String key);

    @Nullable Object resolve(@Nullable Object value, String argument);

    static <T> Builder<T> builder(Class<T> match) {
        return new Builder<>(match);
    }

    class Builder<T> {

        private final TinyObjectMappingImpl impl;

        public Builder(Class<T> match) {
            impl = new TinyObjectMappingImpl(match);
        }

        public Builder<T> withFallback(Function<T, Object> production) {
            impl.productions.put("", (Function<Object, Object>) production);
            return this;
        }

        public Builder<T> with(String value, Function<T, Object> production) {
            impl.productions.put(value, (Function<Object, Object>) production);
            return this;
        }

        public TinyObjectMapping build() {
            return impl;
        }
    }
}
