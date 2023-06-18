package de.cubbossa.translations;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class ApplicationMessageBundle extends AbstractMessageBundle implements MessageBundle {

    private final GlobalMessageBundle global;
    @Getter
    private final File dataFolder;

    public ApplicationMessageBundle(GlobalMessageBundle global, File dataFolder, Logger logger) {
        this(global, dataFolder, logger, new Config());
    }

    public ApplicationMessageBundle(GlobalMessageBundle global, File dataFolder, Logger logger, Config config) {
        super(config, logger);
        this.global = global;
        this.dataFolder = dataFolder;
    }

    protected Component getTranslation(Locale locale, Message message, Audience audience) {
        String translation = getTranslationRaw(locale, message);
        TagResolver resolver = TagResolver.builder()
                .resolvers(message.getPlaceholderResolvers())
                .resolver(global.getStylesResolver())
                .resolvers(global.getBundleResolvers())
                .resolver(global.getMessageResolver(audience))
                .resolver(getStylesResolver())
                .resolver(getBundleResolvers())
                .resolver(getMessageResolver(audience))
                .build();
        return Message.Format.translate(translation, resolver);
    }

    @Override
    public TagResolver getMessageResolver(Audience audience) {
        return TagResolver.resolver("msg", (queue, ctx) -> {
            String messageKey = queue.popOr("The message tag requires a message key, like <msg:error.no_permission>.").value();
            boolean preventBleed = queue.hasNext() && queue.pop().isTrue();

            // TODO loop detection
            return preventBleed
                    ? Tag.selfClosingInserting(translate(getMessage(messageKey), audience))
                    : Tag.preProcessParsed(translateRaw(getMessage(messageKey), audience));
        });
    }

}
