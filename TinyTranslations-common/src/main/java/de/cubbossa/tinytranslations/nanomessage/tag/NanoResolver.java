package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.nanomessage.NanoContextImpl;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.function.Function;

public interface NanoResolver extends Function<NanoContextImpl, TagResolver> {
}
