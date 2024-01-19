package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.function.Function;

class NanoMessageImpl implements NanoMessage {

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	private static final NanoMessageCompiler COMPILER = new NanoMessageCompiler();

	@Getter
	final ObjectTagResolverMap objectTypeResolverMap;
	TagResolver defaultResolver = TagResolver.empty();
	final Collection<Function<NanoContextImpl, TagResolver>> nanoResolvers = new HashSet<>();

	public NanoMessageImpl() {
		this.objectTypeResolverMap = new ObjectTagResolverMap();
	}

	public Component deserialize(@Language("NanoMessage") String value, NanoContextImpl context, TagResolver... resolvers) {
		String processed = COMPILER.compile(value);
		return MINI_MESSAGE.deserialize(processed, TagResolver.builder()
				.resolver(defaultResolver)
				.resolvers(resolvers)
				.resolvers(context.getResolvers().stream().map(n -> n.apply(context)).toList())
				.resolvers(nanoResolvers.stream().map(f -> f.apply(context)).toArray(TagResolver[]::new))
				.build());
	}

	@Override
	public @NotNull Component deserialize(@NotNull @Language("NanoMessage") String input) {
		return deserialize(input, new NanoContextImpl(Locale.ENGLISH));
	}

	@Override
	public @NotNull String serialize(@NotNull Component component) {
		return MINI_MESSAGE.serialize(component);
	}
}
