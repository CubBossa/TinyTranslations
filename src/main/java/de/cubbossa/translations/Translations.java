package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Translations extends Translator {

    Locale UNDEFINED = Locale.forLanguageTag("und");

    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    class Config {
        protected Locale defaultLocale = Locale.US;
        protected Predicate<Locale> generateMissingFiles = locale -> Arrays.asList(Locale.getAvailableLocales()).contains(locale);
        protected Predicate<Locale> localePredicate = locale -> true;
        protected Function<Audience, Locale> playerLocaleFunction = audience -> defaultLocale;

        public Config defaultLocale(Locale defaultLocale) {
            if (defaultLocale == null || defaultLocale.toLanguageTag().equals("und")) {
                throw new IllegalArgumentException("Default locale must be valid: " + (defaultLocale == null
                        ? null : defaultLocale.toLanguageTag()));
            }
            this.defaultLocale = defaultLocale;
            return this;
        }
    }

    static Translations global() {

    }

    static PluginTranslationsBuilder application(String name, File directory) {
        return new PluginTranslationsBuilder(global(), name, directory);
    }

    void loadStyles();

    void saveStyles();

    void loadLocale(Locale locale);

    void saveLocale(Locale locale);


    MiniMessage getMiniMessage();

    void setMiniMessage(MiniMessage miniMessage);


    MessageSet getMessageSet();

    void setMessageSet(MessageSet set);

    MessageStorage getMessageStorage();

    void setMessageStorage(MessageStorage storage);

    StyleSet getStyleSet();

    void setStyleSet(StyleSet set);

    StyleStorage getStyleStorage();

    void setStyleStorage(StyleStorage storage);


    Locale getUserLocale(UUID user);
}
