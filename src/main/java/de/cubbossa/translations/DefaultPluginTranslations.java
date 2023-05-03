package de.cubbossa.translations;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultPluginTranslations implements PluginTranslations {

    private final Translations translations;
    private final File directory;
    @Getter
    private final Config config;
    private final Map<String, Message> registeredMessages;
    private final Map<Locale, Map<Message, String>> translationCache;
    private final Collection<TagResolver> applicationResolvers;
    private final Collection<TagResolver> styles;
    private final Logger logger;

    public DefaultPluginTranslations(Translations translations, File directory, Logger logger) {
        this(translations, directory, logger, new Config());
    }

    public DefaultPluginTranslations(Translations translations, File directory, Logger logger, Config config) {
        this.translations = translations;
        this.directory = directory;
        this.config = config;
        this.logger = logger;
        this.registeredMessages = new HashMap<>();
        this.translationCache = new HashMap<>();
        this.applicationResolvers = new HashSet<>();
        this.styles = new HashSet<>();
    }

    @Override
    public CompletableFuture<Void> writeLocale(Locale locale) {
        return CompletableFuture.runAsync(() -> {
            LanguageFileHandle handle = config.languageFileStorageType.languageFileHandle();
            handle.writeMessages(registeredMessages.values().stream()
                            .collect(Collectors.toMap(Function.identity(), m -> m.getDefaultTranslations().getOrDefault(locale, m.getDefaultValue()))),
                    locale
            );
        });
    }

    public CompletableFuture<Void> cacheLocale(Locale locale) {
        return CompletableFuture.runAsync(() -> {
            LanguageFileHandle handle = config.languageFileStorageType.languageFileHandle();
            translationCache.computeIfAbsent(locale, x -> new HashMap<>()).putAll(
                    handle.readMessages(registeredMessages.values(), locale, registeredMessages.values())
            );
        });
    }

    public CompletableFuture<Void> cacheMessage(Message message, Locale locale) {
        return CompletableFuture.runAsync(() -> {
            LanguageFileHandle handle = config.languageFileStorageType.languageFileHandle();
            Optional<String> translation = handle.readMessage(message, locale, registeredMessages.values());
            translation.ifPresent(s -> translationCache.computeIfAbsent(locale, x -> new HashMap<>()).put(message, s));
        });
    }

    public CompletableFuture<Void> loadStyles() {
        return CompletableFuture.runAsync(() -> {
            StyleFileHandle handle = config.styleFileStorageType.styleFileHandle();
            styles.clear();
            styles.addAll(handle.loadStyles());
        });
    }

    public void addMessage(Message message) {
        registeredMessages.put(message.getKey(), message);
    }

    public void addMessages(Message... messages) {
        for (Message message : messages) {
            addMessage(message);
        }
    }

    public void addMessagesClass(Class<?> messageClass) {
        Field[] messages = Arrays.stream(messageClass.getDeclaredFields())
                .filter(field -> field.getType().equals(Message.class))
                .toArray(Field[]::new);

        for (Field messageField : messages) {
            try {
                addMessage((Message) messageField.get(messageClass));
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Could not extract message '" + messageField.getName() + "' from class " + messageClass.getSimpleName());
                continue;
            }
        }
    }

    @Override
    public Collection<TagResolver> getGlobalResolvers() {
        return new HashSet<>(applicationResolvers);
    }

    @Override
    public void addGlobalResolver(TagResolver resolver) {
        applicationResolvers.add(resolver);
    }

    private String getTranslationRaw(Locale locale, Message message) {
        Map<Message, String> map = translationCache.get(locale);
        if (map == null || !map.containsKey(message)) {
            cacheMessage(message, locale).join();
            map = translationCache.get(locale);
            if (map == null || !map.containsKey(message)) {
                throw new IllegalStateException("Caching did not contain message against expectation.");
            }
        }
        return map.get(message);
    }

    private Component getTranslation(Locale locale, Message message, Audience audience) {
        String translation = getTranslationRaw(locale, message);
        TagResolver resolver = TagResolver.builder()
                .resolvers(translations.getGlobalResolvers())
                .resolvers(translations.messageTags(message, audience))
                .resolvers(applicationResolvers)
                .build();
        return Message.Format.translate(translation, resolver);
    }

    @Override
    public String translateRaw(Message message) {
        return getTranslationRaw(config.defaultLanguage, message);
    }

    @Override
    public String translateRaw(Message message, Audience audience) {
        return getTranslationRaw(getLanguage(audience), message);
    }

    @Override
    public String translateRaw(Message message, Locale locale) {
        return getTranslationRaw(locale, message);
    }

    /**
     * - Resolve default language
     * - Check if message is present in cache
     * - If not
     * > create language file with all existing translations
     * > Cache created translation
     * - return cache value
     *
     * @param message
     * @return
     */
    @Override
    public Component translate(Message message) {
        return getTranslation(config.defaultLanguage, message, null);
    }

    @Override
    public Component translate(Message message, Audience audience) {
        return getTranslation(getLanguage(audience), message, audience);
    }

    @Override
    public Component translate(Message message, Locale locale) {
        return getTranslation(locale, message, null);
    }
}
