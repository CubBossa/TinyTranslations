package de.cubbossa.tinytranslations.util;

import de.cubbossa.tinytranslations.Formattable;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FormattableBuilder implements Formattable<FormattableBuilder> {

    public static FormattableBuilder builder() {
        return new FormattableBuilder();
    }

    private List<TagResolver> resolvers = new ArrayList<>();

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
    public <T> FormattableBuilder insertObject(@NotNull String key, T obj) {
        throw new IllegalStateException("Cannot insert object into FormattableBuilder without providing any TinyObjectResolvers.");
    }

    @Override
    public Collection<TinyObjectResolver> getObjectResolversInScope() {
        return Collections.emptyList();
    }
}
