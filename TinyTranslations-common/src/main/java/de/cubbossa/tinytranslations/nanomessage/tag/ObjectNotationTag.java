package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.tinyobject.InsertedObject;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolverImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static de.cubbossa.tinytranslations.tinyobject.TinyObjectMappingImpl.TAG_PLACEHOLDER_NAME;

public class ObjectNotationTag {

    public static final String KEY = "object_notation";
    private static TinyObjectResolver RESOLVER = new TinyObjectResolverImpl();

    public static TagResolver resolver(Map<String, InsertedObject> objectTable, Collection<TinyObjectMapping> mappings) {
        return new TagResolver() {
            @Override
            public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue argumentQueue, @NotNull Context c) throws ParsingException {
                if (!name.equalsIgnoreCase(KEY) && !objectTable.containsKey(name)) {
                    return null;
                }


                if (name.equalsIgnoreCase(KEY) && !argumentQueue.hasNext()) {
                    throw c.newException("A object_notation must be used in combination with a dot notated object descriptor.");
                }
                String descriptor = name.equalsIgnoreCase(KEY) ? argumentQueue.pop().value() : name;

                Queue<String> path = new LinkedList<>(Arrays.stream(descriptor.split("\\.")).toList());

                InsertedObject boxed = objectTable.get(path.poll());
                if (boxed == null) {
                    return Tag.inserting(Component.text("null"));
                }
                var obj = boxed.object();
                // Remain order! The per object mappings are more important than the general mappings, therefore insert them last.
                var fMappings = new ArrayList<>(mappings);
                fMappings.addAll(boxed.resolvers());

                Object resolved = RESOLVER.resolveObject(obj, path, fMappings);
                try {
                    if (resolved == null) {
                        throw c.newException("Could not resolve object with path '" + descriptor + ":" + String.join(":", path) + "'.");
                    }
                    if (resolved instanceof TagResolver resolver) {
                        return resolver.resolve(TAG_PLACEHOLDER_NAME, argumentQueue, c);
                    }
                    if (resolved instanceof Tag tag) {
                        return tag;
                    }
                    if (resolved instanceof ComponentLike componentLike) {
                        return Tag.inserting(componentLike);
                    }
                    return Tag.inserting(Component.text(resolved.toString()));
                } finally {
                    argumentQueue.reset();
                }
            }

            @Override
            public boolean has(@NotNull String name) {
                return name.equalsIgnoreCase(KEY) || objectTable.containsKey(name);
            }
        };
    }
}