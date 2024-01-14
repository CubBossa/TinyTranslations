package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.MessageFormat;
import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
import de.cubbossa.tinytranslations.nanomessage.tag.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

public class NanoMessage {

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	private static final NanoMessageCompiler COMPILER = new NanoMessageCompiler();

	public static NanoMessage nanoMessage() {
		NanoMessage nm = new NanoMessage();
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

	@Getter
	private final ObjectTagResolverMap objectTypeResolverMap;
	private TagResolver defaultResolver = TagResolver.empty();
	private final Collection<Function<Context, TagResolver>> nanoResolvers = new HashSet<>();

	public NanoMessage() {
		this.objectTypeResolverMap = new ObjectTagResolverMap();
	}

	public Component parse(@Language("NanoMessage") String value, Context context, TagResolver... resolvers) {
		String processed = COMPILER.compile(value);
		return MINI_MESSAGE.deserialize(processed, TagResolver.builder()
				.resolver(defaultResolver)
				.resolvers(resolvers)
				.resolvers(context.getResolvers().stream().map(n -> n.apply(context)).toList())
				.resolvers(nanoResolvers.stream().map(f -> f.apply(context)).toArray(TagResolver[]::new))
				.build());
	}
}
