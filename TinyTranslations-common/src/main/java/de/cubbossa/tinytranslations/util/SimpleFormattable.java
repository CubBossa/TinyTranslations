package de.cubbossa.tinytranslations.util;

import de.cubbossa.tinytranslations.Formattable;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleFormattable implements Formattable<SimpleFormattable> {

    private List<TagResolver> resolvers = new ArrayList<>();
    @Override
    public Collection<TagResolver> getResolvers() {
        return resolvers;
    }

    @Override
    public SimpleFormattable formatted(TagResolver... resolver) {
        resolvers.addAll(List.of(resolver));
        return this;
    }
}
