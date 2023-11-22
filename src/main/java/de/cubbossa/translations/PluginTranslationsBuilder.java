package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.PropertiesStorage;
import de.cubbossa.translations.persistent.PropertiesStyleStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class PluginTranslationsBuilder {

    private final GlobalTranslations translations;
    private final String pluginName;
    private final File dataFolder;
    private Logger logger = Logger.getLogger("Translations");
    private Locale defaultLanguage = Locale.US;
    private final Collection<Locale> enabledLocales = new HashSet<>(Set.of(Locale.US));
    private final Collection<Locale> generationLocales = new HashSet<>(List.of(Locale.getAvailableLocales()));
    private Predicate<Locale> localeFilter = enabledLocales::contains;
    private Predicate<Locale> fileGenerationFilter = generationLocales::contains;
    private Function<Audience, Locale> localeSupplier = audience -> defaultLanguage;
    private MessageStorage messageStorage;
    private StyleStorage styleStorage;

    public PluginTranslationsBuilder(GlobalTranslations translations, String pluginName, File dataFolder) {
        this.translations = translations;
        this.pluginName = pluginName;
        this.dataFolder = dataFolder;
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

    public PluginTranslationsBuilder withLocaleFilter(Predicate<Locale> filter) {
        this.localeFilter = filter;
        return this;
    }

    public PluginTranslationsBuilder withEnabledLocales(Locale... locale) {
        this.enabledLocales.addAll(Arrays.stream(locale).toList());
        return this;
    }

    public PluginTranslationsBuilder withPreferClientLanguage(boolean value) {
        this.localeSupplier = value
                ? audience -> audience.getOrDefault(Identity.LOCALE, defaultLanguage)
                : audience -> defaultLanguage;
        return this;
    }

    public PluginTranslationsBuilder withLocaleSupplier(Function<Audience, Locale> function) {
        this.localeSupplier = function;
        return this;
    }

    public PluginTranslationsBuilder withGenerateMissingFilesFor(Iterable<Locale> locale) {
        locale.forEach(this.generationLocales::add);
        return this;
    }

    public PluginTranslationsBuilder withGenerateMissingFilesFor(Locale... locale) {
        this.generationLocales.addAll(Set.of(locale));
        return this;
    }

    public PluginTranslationsBuilder withGenerateMissingFilesForEnabledLocales() {
        this.generationLocales.addAll(enabledLocales);
        return this;
    }

    public PluginTranslationsBuilder withPreferClientLanguage() {
        return withPreferClientLanguage(true);
    }

    public PluginTranslationsBuilder withPropertiesStorage(File directory) {
        this.messageStorage = new PropertiesStorage(logger, directory);
        return this;
    }

    public PluginTranslationsBuilder withPropertiesStyles(File styleFile) {
        this.styleStorage = new PropertiesStyleStorage(styleFile);
        return this;
    }

    public MessageSet build() {

        MessageSet.Config c = new MessageSet.Config()
                .defaultLocale(defaultLanguage)
                .localePredicate(localeFilter)
                .generateMissingFiles(fileGenerationFilter)
                .playerLocaleFunction(localeSupplier)
                .localeBundleStorage(messageStorage)
                .styleStorage(styleStorage);
        ApplicationTranslations translations = new ApplicationTranslations(this.translations, dataFolder, logger, c);
        this.translations.register(pluginName, translations);
        return translations;
    }
}
