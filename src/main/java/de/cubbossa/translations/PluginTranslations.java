package de.cubbossa.translations;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface PluginTranslations extends Translator, TranslationStyle {

    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    class Config {
        protected Locale defaultLocale = Locale.US;
        protected Collection<Locale> enabledLocales = new HashSet<>(Set.of(Locale.US));
        protected boolean preferClientLanguage = false;
        protected LocalesStorage localeBundleStorage;
        protected StylesStorage stylesStorage;
    }

    void clearCache();

    CompletableFuture<Void> loadStyles();

    CompletableFuture<Void> writeLocale(Locale locale);

    CompletableFuture<Void> loadLocale(Locale locale);

    void addMessage(Message message);

    void addMessages(Message...messages);

    void addMessagesClass(Class<?> fromClass);

    Config getConfig();

    default Locale getLanguage(@Nullable Audience audience) {
        // no specified audience -> default locale
        if (audience == null) {
            return getConfig().defaultLocale;
        }
        // all audiences will receive the same locale -> default locale
        if (!getConfig().preferClientLanguage) {
            return getConfig().defaultLocale;
        }
        // check actual client locale
        Locale client = audience.getOrDefault(Identity.LOCALE, Locale.US);
        // if client locale is supported, return client locale
        if (getConfig().enabledLocales().contains(client)) {
            return client;
        }
        // retrieve language of client locale, to e.g. reduce de-AT to de. Maybe, de is supported while de-AT is not
        Locale clientLang = Locale.forLanguageTag(client.getLanguage());
        if (getConfig().enabledLocales().contains(clientLang)) {
            return clientLang;
        }
        // could not find any supported locale for audience, go with default
        return getConfig().defaultLocale;
    }

    Collection<TagResolver> getResolvers();

    void addResolver(TagResolver resolver);
}
