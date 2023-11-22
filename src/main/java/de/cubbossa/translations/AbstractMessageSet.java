package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractMessageSet implements MessageSet {


    protected boolean stylesCached = false;
    protected final Map<String, Style> styleCache;
    protected TagResolver styleResolverCache;
    protected final Collection<TagResolver> bundleResolvers;
    @Getter
    protected Config config;
    protected final Map<String, Message> registeredMessages;
    protected final Map<Locale, Map<Message, String>> translationCache;
    protected final Logger logger;

    public AbstractMessageSet(Config config, Logger logger) {
        this.styleCache = new HashMap<>();
        this.bundleResolvers = new HashSet<>();
        this.config = config;
        this.registeredMessages = new HashMap<>();
        this.translationCache = new HashMap<>();
        this.logger = logger;
    }

    @Override
    public Map<String, Style> getStyles() {
        if (!stylesCached) {
            loadStyles().join();
        }
        return new HashMap<>(styleCache);
    }

    @Override
    public TagResolver getStylesResolver() {
        if (styleResolverCache != null) {
            return styleResolverCache;
        }
        styleResolverCache = TagResolver.resolver(getStyles().entrySet().stream()
            .map(e -> TagResolver.resolver(e.getKey(), Tag.styling(style -> style.merge(e.getValue()))))
            .toList());
        return styleResolverCache;
    }

    @Override
    public void addStyle(String key, Style style) {
        styleCache.put(key, style);
    }

    @Override
    public void removeStyle(String key) {
        styleCache.remove(key);
    }

    public CompletableFuture<Void> loadStyles() {
        return CompletableFuture.runAsync(() -> {
            StyleStorage handle = config.styleStorage;
            if (handle == null) {
                return;
            }
            Map<String, Style> styleMap = handle.loadStyles();

            styleCache.putAll(styleMap);
        }).exceptionally(throwable -> {
            logger.log(Level.SEVERE, "Error while loading styles", throwable);
            return null;
        });
    }

    @Override
    public TagResolver getBundleResolvers() {
        return TagResolver.resolver(bundleResolvers);
    }

    @Override
    public void addBundleResolver(TagResolver resolver) {
        bundleResolvers.add(resolver);
    }

    @Override
    public void clearCache() {
        translationCache.clear();
        styleCache.clear();
        styleResolverCache = null;
        stylesCached = false;
    }

    @Override
    public CompletableFuture<Void> writeLocale(Locale locale) {
        if (!config.generateMissingFiles.test(locale)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Cannot generate locale '" + locale + "'."));
        }
        return loadLocale(locale).thenRun(() -> {
            MessageStorage handle = config.localeBundleStorage;
            Map<Message, String> map = new HashMap<>();
            map.putAll(registeredMessages.values().stream()
                    .collect(Collectors.toMap(Function.identity(), m -> m.getDefaultTranslations().getOrDefault(locale, m.getDefaultValue()))));
            map.putAll(translationCache.get(locale));
            handle.writeMessages(map, locale);
        });
    }

    public CompletableFuture<Void> loadLocale(Locale locale) {
        if (!config.localePredicate.test(locale)) {
            locale = Locale.US;
        }
        final Locale fLocale = locale;
        return CompletableFuture.runAsync(() -> {
            MessageStorage handle = config.localeBundleStorage;
            translationCache.computeIfAbsent(fLocale, x -> new HashMap<>()).putAll(
                    handle.readMessages(registeredMessages.values(), fLocale)
            );
        });
    }

    public CompletableFuture<Void> loadMessage(Message message, Locale locale) {
        return CompletableFuture.runAsync(() -> {
            Locale supportedLocale = supportedLocale(locale);
            MessageStorage handle = config.localeBundleStorage;
            Optional<String> translation = handle.readMessage(message, supportedLocale);
            if (translation.isPresent()) {
                translationCache.computeIfAbsent(supportedLocale, x -> new HashMap<>()).put(message, translation.get());
            } else {
                String s = message.getDefaultTranslations().get(supportedLocale);
                if (s == null) {
                    Locale reduced = Locale.forLanguageTag(supportedLocale.getLanguage());
                    if (config.localePredicate.test(reduced)) {
                        s = message.getDefaultTranslations().get(Locale.forLanguageTag(supportedLocale.getLanguage()));
                    }
                }
                if (s == null) {
                    s = message.getDefaultValue();
                }
                handle.writeMessage(message, supportedLocale, s);
                translationCache.computeIfAbsent(supportedLocale, x -> new HashMap<>()).put(message, s);
            }
        });
    }

    public void addMessage(Message message) {
        message.setTranslator(this);
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
                logger.log(Level.WARNING, "Could not extract message '" + messageField.getName() + "' from class " + messageClass.getSimpleName(), t);
            }
        }
    }

    @Override
    public Message getMessage(String key) {
        return registeredMessages.get(key);
    }

    protected String getTranslationRaw(Locale locale, Message message) {
        Map<Message, String> map = translationCache.get(locale);
        if (map == null || !map.containsKey(message)) {
            loadMessage(message, locale).join();
            map = translationCache.get(locale);
            if (map == null || !map.containsKey(message)) {
                throw new IllegalStateException("Caching did not contain message against expectation.");
            }
        }
        return map.get(message);
    }

    protected Component getTranslation(Locale locale, Message message, Audience audience) {
        String translation = getTranslationRaw(locale, message);
        TagResolver resolver = TagResolver.builder()
                .resolvers(message.getPlaceholderResolvers())
                .resolver(getStylesResolver())
                .resolver(getBundleResolvers())
                .resolver(getMessageResolver(audience))
                .build();
        return Message.Format.translate(translation, resolver);
    }

    @Override
    public String translateRaw(Message message) {
        return getTranslationRaw(config.defaultLocale(), message);
    }

    @Override
    public String translateRaw(Message message, Audience audience) {
        return getTranslationRaw(getLocale(audience), message);
    }

    @Override
    public String translateRaw(Message message, Locale locale) {
        return getTranslationRaw(locale, message);
    }

    /**
     * - Resolve default locale
     * - Check if message is present in cache
     * - If not
     * > create locale file with all existing translations
     * > Cache created translation
     * - return cache value
     *
     * @param message
     * @return
     */
    @Override
    public Component translate(Message message) {
        return getTranslation(config.defaultLocale(), message, null);
    }

    @Override
    public Component translate(Message message, Audience audience) {
        return getTranslation(getLocale(audience), message, audience);
    }

    @Override
    public Component translate(Message message, Locale locale) {
        return getTranslation(supportedLocale(locale), message, null);
    }

    protected Locale supportedLocale(Locale anyLocale) {
        if (anyLocale == null || anyLocale.toLanguageTag().equals("und")) {
            return config.defaultLocale;
        }
        // If locale supported then no issues
        if (config.localePredicate.test(anyLocale)) {
            return anyLocale;
        }
        // Have a look at locale without country
        Locale reduced = Locale.forLanguageTag(anyLocale.getLanguage());
        if (reduced == null || !config.localePredicate.test(reduced)) {
            throw new IllegalStateException("Locale '" + anyLocale + "' is not supported by Translations.");
        }
        return reduced;
    }

    @Override
    public Locale getLocale(@Nullable Audience audience) {
        // no specified audience -> default locale
        if (audience == null) {
            return config.defaultLocale;
        }
        // check actual client locale
        Locale client = config.playerLocaleFunction.apply(audience);
        if (client.equals(UNDEFINED)) {
            logger.log(Level.WARNING, "Could not read locale of player '" + audience.getOrDefault(Identity.UUID, null) + "', " + config.defaultLocale.toLanguageTag() + " used.");
            return config.defaultLocale;
        }
        try {
            return supportedLocale(client);
        } catch (IllegalStateException e) {
            // if player locale is not supported use default
            return config.defaultLocale;
        }
    }
}
