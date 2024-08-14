package de.cubbossa.tinytranslations.util;

import de.cubbossa.tinytranslations.Formattable;
import de.cubbossa.tinytranslations.tinyobject.InsertedObject;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FormattableBuilder implements Formattable<FormattableBuilder> {

    public static FormattableBuilder builder() {
        return new FormattableBuilder();
    }

    private List<TagResolver> resolvers = new ArrayList<>();
    private Map<String, InsertedObject> insertedObjects = new HashMap<>();

    private FormattableBuilder() {
    }

    @Override
    public Collection<TagResolver> getResolvers() {
        return resolvers;
    }

    public TagResolver toResolver() {
        return TagResolver.resolver(resolvers);
    }

    @Override
    public FormattableBuilder formatted(TagResolver... resolver) {
        resolvers.addAll(List.of(resolver));
        return this;
    }

    @Override
    public Map<String, InsertedObject> insertedObjects() {
        return insertedObjects;
    }

    @Override
    public <T> FormattableBuilder insertObject(@NotNull String key, T obj, Collection<TinyObjectMapping> resolvers) {
        insertedObjects.put(key, new InsertedObject(key, obj, resolvers));
        return this;
    }
}
