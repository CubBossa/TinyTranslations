package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolverImpl;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

class NanoMessageImpl implements NanoMessage {

    private static final NanoMessageCompiler COMPILER = new NanoMessageCompiler();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .preProcessor(COMPILER::compile)
            .strict(false)
            .build();

    @Getter
    final TinyObjectResolver objectResolver;
    TagResolver defaultResolver = TagResolver.empty();

    public NanoMessageImpl() {
        this.objectResolver = new TinyObjectResolverImpl();
    }

    public Component deserialize(@Language("NanoMessage") String value, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(value, TagResolver.builder()
                .resolver(defaultResolver)
                .resolvers(resolvers)
                .build());
    }

    @Override
    public @NotNull Component deserialize(@NotNull @Language("NanoMessage") String input) {
        return deserialize(input, new TagResolver[0]);
    }

    @Override
    public @NotNull String serialize(@NotNull Component component) {
        return MINI_MESSAGE.serialize(component);
    }
}
