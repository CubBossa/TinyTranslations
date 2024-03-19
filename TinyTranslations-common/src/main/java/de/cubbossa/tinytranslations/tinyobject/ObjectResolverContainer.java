package de.cubbossa.tinytranslations.tinyobject;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public record ObjectResolverContainer(Collection<TinyObjectResolver> resolvers) implements TagResolver {

    @Override
    public boolean has(@NotNull String name) {
        return false;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        return null;
    }
}
