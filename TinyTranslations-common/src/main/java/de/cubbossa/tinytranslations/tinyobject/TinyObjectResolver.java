package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface TinyObjectResolver {

    default @Nullable Object resolveObject(@NotNull Object object, String path) {
        return resolveObject(object, path, Collections.emptyList());
    }

    default @Nullable Object resolveObject(@NotNull Object object, String path, Collection<TinyObjectMapping> mappings) {
        return resolveObject(object, path.isEmpty()
                ? Collections.emptyList()
                : List.of(path.split("\\.")), mappings);
    }

    default @Nullable Object resolveObject(@NotNull Object object, Iterable<String> path) {
        return resolveObject(object, path, Collections.emptyList());
    }

    @Nullable Object resolveObject(@NotNull Object object, Iterable<String> path, Collection<TinyObjectMapping> mappings);
}
