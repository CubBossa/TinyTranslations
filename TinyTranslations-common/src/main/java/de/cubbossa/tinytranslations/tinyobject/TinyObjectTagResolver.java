package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface TinyObjectTagResolver {

    /**
     * Adds a {@link TinyObjectResolver} to this resolver permanently. The mapping will be used for all {@link #resolveObject(Object, String, Collection<TinyObjectResolver>)} calls.
     * @param mapping The mapping to add.
     */
    void add(TinyObjectResolver mapping);

    default @Nullable Object resolveObject(@NotNull Object object, String path) {
        return resolveObject(object, path, Collections.emptyList());
    }

    default @Nullable Object resolveObject(@NotNull Object object, String path, Collection<TinyObjectResolver> mappings) {
        return resolveObject(object, path.isEmpty()
                ? Collections.emptyList()
                : List.of(path.split(":")), mappings);
    }

    default @Nullable Object resolveObject(@NotNull Object object, Iterable<String> path) {
        return resolveObject(object, path, Collections.emptyList());
    }

    @Nullable Object resolveObject(@NotNull Object object, Iterable<String> path, Collection<TinyObjectResolver> mappings);
}
