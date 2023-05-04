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
public class Translations implements Translator {

    private static Translations instance;

    public static Translations get() {
        return instance;
    }

    public static PluginTranslationsBuilder builder(String pluginName) {
        Translations translations = Translations.get();
        if (translations == null) {
            translations = new Translations();
        }
        return new PluginTranslationsBuilder(translations, pluginName);
    }

    public synchronized void register(String name, PluginTranslations translations) {
        Translations t = Translations.get();
        if (t == null) {
            t = new Translations();
        }

        if (t.applicationMap.containsKey(name)) {
            throw new IllegalArgumentException("Could not register new PluginTranslations, another translation with" +
                    "key '" + name + "' already exists");
        }
        t.applicationMap.put(name, translations);
    }

    private final Logger logger = Logger.getLogger("Translations");

    private final Map<String, PluginTranslations> applicationMap;
    private final Collection<TagResolver> globalResolvers;

    public Translations() {
        instance = this;

        this.applicationMap = new HashMap<>();
        this.globalResolvers = new ArrayList<>();
    }

    public TagResolver messageTags(Message forMessage, Audience audience) {
        // TODO proper loop detection
        return TagResolver.builder()
                .tag(Set.of("msg", "message", "translation"), (queue, ctx) -> {
                    String messageKey = queue.popOr("The message tag requires a message key, like <message:error.no_permission>.").value();
                    if (forMessage.getKey().equals(messageKey)) {
                        throw new MessageReferenceLoopException(forMessage);
                    }
                    return Tag.preProcessParsed(translateRaw(new Message(messageKey), audience));
                })
                .tag(Set.of("raw-msg", "raw-message"), (queue, ctx) -> {
                    String messageKey = queue.popOr("The message tag requires a message key, like <message:error.no_permission>.").value();
                    if (forMessage.getKey().equals(messageKey)) {
                        throw new MessageReferenceLoopException(forMessage);
                    }
                    return Tag.inserting(translate(new Message(messageKey), audience));
                })
                .build();
    }

    public void registerGlobalResolver(TagResolver resolver) {
        globalResolvers.add(resolver);
    }

    public void unregisterGlobalResolver(TagResolver tagResolver) {
        globalResolvers.remove(tagResolver);
    }

    private Optional<PluginTranslations> getApplicationFromKey(Message message) {
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
