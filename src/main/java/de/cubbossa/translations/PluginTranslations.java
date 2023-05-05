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

public interface PluginTranslations extends Translator {

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

    CompletableFuture<Void> loadStyles();

    CompletableFuture<Void> writeLocale(Locale locale);

    CompletableFuture<Void> loadLocale(Locale locale);

    void addMessage(Message message);

    void addMessages(Message...messages);

    void addMessagesClass(Class<?> fromClass);

    Config getConfig();

    default Locale getLanguage(@Nullable Audience audience) {
        if (audience == null) {
            return getConfig().defaultLocale;
        }
        if (getConfig().preferClientLanguage) {
            return audience.getOrDefault(Identity.LOCALE, Locale.US);
        }
        return getConfig().defaultLocale;
    }

    Collection<TagResolver> getResolvers();

    void addResolver(TagResolver resolver);
}
