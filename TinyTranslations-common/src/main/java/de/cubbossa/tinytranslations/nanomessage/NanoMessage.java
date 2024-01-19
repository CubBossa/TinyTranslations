package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.MessageFormat;
import de.cubbossa.tinytranslations.nanomessage.tag.BrighterTag;
import de.cubbossa.tinytranslations.nanomessage.tag.ClickTag;
import de.cubbossa.tinytranslations.nanomessage.tag.DarkerTag;
import de.cubbossa.tinytranslations.nanomessage.tag.HoverTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.intellij.lang.annotations.Language;

public interface NanoMessage extends ComponentSerializer<Component, Component, String> {

	static NanoMessage nanoMessage() {
		NanoMessageImpl nm = new NanoMessageImpl();
		nm.defaultResolver = TagResolver.resolver(
				DefaultResolvers.choice("choice"),
				DarkerTag.RESOLVER,
				BrighterTag.RESOLVER,
				DefaultResolvers.repeat("repeat"),
				DefaultResolvers.reverse("reverse"),
				DefaultResolvers.upper("upper"),
				DefaultResolvers.lower("lower"),
				DefaultResolvers.shortUrl("shorturl"),
				DefaultResolvers.preview("shorten"),
				MessageFormat.NBT.getTagResolver(),
				MessageFormat.LEGACY_PARAGRAPH.getTagResolver(),
				MessageFormat.LEGACY_AMPERSAND.getTagResolver(),
				MessageFormat.PLAIN.getTagResolver(),
				ClickTag.RESOLVER,
				HoverTag.RESOLVER
		);
		return nm;
	}

	ObjectTagResolverMap getObjectTypeResolverMap();

	Component deserialize(@Language("NanoMessage") String value, NanoContextImpl context, TagResolver... resolvers);
}
