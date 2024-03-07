package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.TinyTranslations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.LinkedList;
import java.util.Queue;

public class ObjectTag {

    public static <T> TagResolver resolver(String key, T obj) {
        return TagResolver.resolver(key, (argumentQueue, c) -> {
            if (obj == null) {
                return Tag.inserting(Component.text("null"));
            }
            Queue<String> path = new LinkedList<>();
            while (argumentQueue.hasNext()) {
                path.add(argumentQueue.pop().value());
            }
            argumentQueue.reset();
            Object resolved = TinyTranslations.NM.getObjectResolver().resolveObject(obj, path);
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