package de.cubbossa.translations;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.logging.Logger;

@Getter
@Setter
public class GlobalTranslations implements Translator {

    private static GlobalTranslations instance;

    public static GlobalTranslations get() {
        return instance;
    }

    public static PluginTranslationsBuilder builder(String pluginName) {
        GlobalTranslations translations = GlobalTranslations.get();
        if (translations == null) {
            translations = new GlobalTranslations();
        }
        return new PluginTranslationsBuilder(translations, pluginName);
    }

    public synchronized void register(String name, MessageBundle translations) {
        GlobalTranslations t = GlobalTranslations.get();
        if (t == null) {
            t = new GlobalTranslations();
        }

        if (t.applicationMap.containsKey(name)) {
            throw new IllegalArgumentException("Could not register new PluginTranslations, another translation with" +
                    "key '" + name + "' already exists");
        }
        t.applicationMap.put(name, translations);
    }

    private final Logger logger = Logger.getLogger("Translations");

    private final Map<String, MessageBundle> applicationMap;
    private final Collection<TagResolver> globalResolvers;

    public GlobalTranslations() {
        instance = this;

        this.applicationMap = new HashMap<>();
        this.globalResolvers = new ArrayList<>();
    }

    public TagResolver messageTags(MessageBundle bundle, Message forMessage, Audience audience) {
        // TODO proper loop detection
        return TagResolver.resolver(Set.of("msg", "message", "translation"), (queue, ctx) -> {
            String messageKey = queue.popOr("The message tag requires a message key, like <message:error.no_permission>.").value();
            boolean preventBleed = queue.hasNext() && queue.pop().isTrue();
            Translator app = queue.hasNext()
                    ? applicationMap.get(queue.pop().lowerValue())
                    : forMessage.getTranslator();

            if (forMessage.getKey().equals(messageKey)) {
                throw new MessageReferenceLoopException(forMessage);
            }
            return preventBleed
                ? Tag.selfClosingInserting(app.translate(bundle.getMessage(messageKey), audience))
                : Tag.preProcessParsed(app.translateRaw(bundle.getMessage(messageKey), audience));
        });
    }

    public void registerGlobalResolver(TagResolver resolver) {
        globalResolvers.add(resolver);
    }

    public void unregisterGlobalResolver(TagResolver tagResolver) {
        globalResolvers.remove(tagResolver);
    }

    private Optional<MessageBundle> getApplicationFromKey(Message message) {
        return Optional.ofNullable(applicationMap.get(message.getKey().split("\\.")[0]));
    }

    @Override
    public String translateRaw(Message message) {
        return getApplicationFromKey(message).orElseThrow().translateRaw(message);
    }

    @Override
    public String translateRaw(Message message, Audience audience) {
        return getApplicationFromKey(message).orElseThrow().translateRaw(message, audience);
    }

    @Override
    public String translateRaw(Message message, Locale locale) {
        return getApplicationFromKey(message).orElseThrow().translateRaw(message, locale);
    }

    @Override
    public Component translate(Message message) {
        return getApplicationFromKey(message).orElseThrow().translate(message);
    }

    @Override
    public Component translate(Message message, Audience audience) {
        return getApplicationFromKey(message).orElseThrow().translate(message, audience);
    }

    @Override
    public Component translate(Message message, Locale locale) {
        return getApplicationFromKey(message).orElseThrow().translate(message, locale);
    }
}
