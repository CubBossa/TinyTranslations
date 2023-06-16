package de.cubbossa.translations;

import de.cubbossa.translations.persistent.LocalesStorage;
import de.cubbossa.translations.persistent.PropertiesStorage;
import de.cubbossa.translations.persistent.PropertiesStyles;
import de.cubbossa.translations.persistent.StylesStorage;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class PluginTranslationsBuilder {

    private final GlobalMessageBundle translations;
    private final String pluginName;
    private Logger logger = Logger.getLogger("Translations");
    private Locale defaultLanguage = Locale.US;
    private final Collection<Locale> enabledLocales = new HashSet<>(Set.of(Locale.US));
    private boolean preferClientLanguage = false;
    private LocalesStorage localesStorage;
    private StylesStorage stylesStorage;

    public PluginTranslationsBuilder(GlobalMessageBundle translations, String pluginName) {
        this.translations = translations;
        this.pluginName = pluginName;
    }

    public PluginTranslationsBuilder withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public PluginTranslationsBuilder withDefaultLocale(Locale locale) {
        this.defaultLanguage = locale;
        this.enabledLocales.add(locale);
        return this;
    }

    public PluginTranslationsBuilder withEnabledLocales(Locale... locale) {
        this.enabledLocales.addAll(Arrays.stream(locale).toList());
        return this;
    }

    public PluginTranslationsBuilder withPreferClientLanguage(boolean value) {
        this.preferClientLanguage = value;
        return this;
    }
    public PluginTranslationsBuilder withPreferClientLanguage() {
        return withPreferClientLanguage(true);
    }

    public PluginTranslationsBuilder withPropertiesStorage(File directory) {
        this.localesStorage = new PropertiesStorage(logger, directory);
        return this;
    }

    public PluginTranslationsBuilder withPropertiesStyles(File styleFile) {
        this.stylesStorage = new PropertiesStyles(styleFile);
        return this;
    }

    public MessageBundle build() {
        MessageBundle.Config c = new MessageBundle.Config()
                .defaultLocale(defaultLanguage)
                .enabledLocales(enabledLocales)
                .preferClientLanguage(preferClientLanguage)
                .localeBundleStorage(localesStorage)
                .stylesStorage(stylesStorage);
        ApplicationMessageBundle translations = new ApplicationMessageBundle(this.translations, logger, c);
        this.translations.register(pluginName, translations);
        return translations;
    }
}
