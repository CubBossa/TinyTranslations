package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectTagResolver;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectTagResolverImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class ObjectTag {

    private static TinyObjectTagResolver RESOLVER = new TinyObjectTagResolverImpl();

    public static <T> TagResolver resolver(String key, T obj, Collection<TinyObjectResolver> mappings) {
        return TagResolver.resolver(key, (argumentQueue, c) -> {
            if (obj == null) {
                return Tag.inserting(Component.text("null"));
            }
            Queue<String> path = new LinkedList<>();
            while (argumentQueue.hasNext()) {
                path.add(argumentQueue.pop().value());
            }
            argumentQueue.reset();
            Object resolved = RESOLVER.resolveObject(obj, path, mappings);
            if (resolved == null) {
                throw c.newException("Could not resolve object with path '" + key + ":" + String.join(":", path) + "'.");
            }
            if (resolved instanceof ComponentLike componentLike) {
                return Tag.inserting(componentLike);
            }
            return Tag.inserting(Component.text(resolved.toString()));
        });
    }
}