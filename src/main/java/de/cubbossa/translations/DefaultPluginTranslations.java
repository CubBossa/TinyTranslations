package de.cubbossa.translations;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.File;
import java.util.*;

public class DefaultPluginTranslations implements PluginTranslations {

    private final File directory;
    @Getter
    private final Config config;
    private LanguageFileHandle languageFileHandle;
    private final Map<Locale, Map<Message, String>> translationCache;
    private final Collection<TagResolver> applicationResolvers;

    public DefaultPluginTranslations(File directory) {
        this(directory, new Config());
    }

    public DefaultPluginTranslations(File directory, Config config) {
        this.directory = directory;
        this.config = config;
        this.translationCache = new HashMap<>();
        this.applicationResolvers = new HashSet<>();
    }

    public void addMessage(Message message) {

    }

    public void addMessages(Message... messages) {

    }

    public void addMessagesClass(Class<?> messageClass) {

    }

    @Override
    public Collection<TagResolver> getGlobalResolvers() {
        return new HashSet<>(applicationResolvers);
    }

    @Override
    public void addGlobalResolver(TagResolver resolver) {
        applicationResolvers.add(resolver);
    }

    private String getTranslation(Locale locale, Message message) {
        Map<Message, String> map = translationCache.get(config.defaultLanguage);
        return map == null ? "missing" : map.getOrDefault(message, "missing");
    }

    @Override
    public String translate(Message message) {
        return getTranslation(config.defaultLanguage, message);
    }

    @Override
    public String translate(Message message, Audience audience) {
        return getTranslation(getLanguage(audience), message);
    }
}
