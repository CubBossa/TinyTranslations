package de.cubbossa.translations;

import de.cubbossa.translations.persistent.LocalesStorage;
import de.cubbossa.translations.persistent.StylesStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface MessageBundle extends Translator, StyleBundle {

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

    Message getMessage(String key);

    Config getConfig();

    Locale getLocale(@Nullable Audience audience);

    TagResolver getBundleResolvers();

    void addBundleResolver(TagResolver resolver);
}
