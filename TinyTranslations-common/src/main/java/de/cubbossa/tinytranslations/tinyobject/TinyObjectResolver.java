package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Describes how elements should be resolved in the {@link TinyObjectTagResolver}s resolving process.
 * <br><br>
 * A mapping is represented by a collection of productions. A production is a pair of a key and a function&#60object,object>
 * <br><br>
 * Within the resolving process, a stack of Strings describes each next production for matching objects.
 * Meaning, a mapping for <pre>record Player(String name)</pre> could have production to turn the key "name" into the name
 * field of the class.
 */
public interface TinyObjectResolver extends Comparable<TinyObjectResolver> {

    /**
     * Checks if this mapping applies to a certain object. Most likely an instanceof check.
     * @param obj The object to check.
     * @return true if the object matches.
     */
    boolean matches(Object obj);

    /**
     * Checks if this mapping contains a production with the given key.
     * @param key A key to check for.
     * @return true if this mapping contains a production with the given key.
     */
    boolean containsKey(String key);

    /**
     * Finds a matching production for the provided key and applies it to the object.
     *
     * @param value A value object. A call should only be made after calling {@link #matches(Object)} first.
     * @param key The production key.
     * @return The resolved object or null if no production was found for given key.
     */
    @Nullable Object resolve(@Nullable Object value, String key);

    static <T> Builder<T> builder(Class<T> match) {
        return new Builder<>(match);
    }

    class Builder<T> {

        private final TinyObjectResolverImpl impl;

        public Builder(Class<T> match) {
            impl = new TinyObjectResolverImpl(match);
        }

        /**
         * Adds a production that is being used whenever the key is empty or non-existent.
         * @param production The production to add.
         * @return this builder instance.
         */
        public Builder<T> withFallback(Function<T, Object> production) {
            impl.productions.put("", (Function<Object, Object>) production);
            return this;
        }

        /**
         * Adds a production for a given key.
         * @param value The key to use.
         * @param production The production to add.
         * @return this builder instance.
         */
        public Builder<T> with(String value, Function<T, Object> production) {
            impl.productions.put(value, (Function<Object, Object>) production);
            return this;
        }

        public TinyObjectResolver build() {
            return impl;
        }
    }
}
