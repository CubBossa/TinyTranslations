package de.cubbossa.tinytranslations.tinyobject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface TinyObjectResolver {

    void add(TinyObjectMapping mapping);

    default @Nullable Object resolveObject(@NotNull Object object, String path) {
        return resolveObject(object, path.isEmpty()
                ? Collections.emptyList()
                : List.of(path.split(":")));
    }

    @Nullable Object resolveObject(@NotNull Object object, Iterable<String> path);
}
