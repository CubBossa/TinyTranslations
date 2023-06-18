package de.cubbossa.translations;

import de.cubbossa.translations.persistent.FileStorage;
import de.cubbossa.translations.persistent.LocalesStorage;
import de.cubbossa.translations.persistent.StylesStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MessageBundle extends Translator, StyleBundle {

    Locale UNDEFINED = Locale.forLanguageTag("und");

    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    class Config {
        protected Locale defaultLocale = Locale.US;
        protected Predicate<Locale> generateMissingFiles = locale -> Arrays.asList(Locale.getAvailableLocales()).contains(locale);
        protected Predicate<Locale> localePredicate = locale -> true;
        protected Function<Audience, Locale> playerLocaleFunction = audience -> defaultLocale;
        protected LocalesStorage localeBundleStorage;
        protected StylesStorage stylesStorage;
    }

    File getDataFolder();

    void clearCache();

    CompletableFuture<Void> loadStyles();

    CompletableFuture<Void> writeLocale(Locale locale);

    CompletableFuture<Void> loadLocale(Locale locale);

    void addMessage(Message message);

    void addMessages(Message...messages);

    void addMessagesClass(Class<?> fromClass);

    Message getMessage(String key);

    Config getConfig();

    Locale getLocale(@Nullable Audience audience);

    TagResolver getBundleResolvers();

    void addBundleResolver(TagResolver resolver);
}
