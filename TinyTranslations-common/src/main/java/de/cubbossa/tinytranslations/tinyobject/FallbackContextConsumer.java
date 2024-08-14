package de.cubbossa.tinytranslations.tinyobject;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;

@FunctionalInterface public interface FallbackContextConsumer<T> {
    Object apply(T value, Context context, ArgumentQueue argumentQueue);
}
