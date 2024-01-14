package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.nanomessage.Context;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.function.Function;

public interface NanoResolver extends Function<Context, TagResolver> {
}
