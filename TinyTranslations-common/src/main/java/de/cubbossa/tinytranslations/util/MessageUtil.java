package de.cubbossa.tinytranslations.util;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TinyTranslations;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class MessageUtil {

    public static @Nullable String getMessageTranslation(Message message, @Nullable Locale locale) {
        if (locale == null) {
            locale = TinyTranslations.FALLBACK_DEFAULT_LOCALE;
        }
        Map<Locale, String> dictionary = message.dictionary();
        String raw = dictionary.get(locale);
        if (raw == null && !"".equals(locale.getVariant())) {
            raw = dictionary.get(new Locale(locale.getLanguage(), locale.getCountry()));
        }
        if (raw == null && !"".equals(locale.getCountry())) {
            raw = dictionary.get(new Locale(locale.getLanguage()));
        }
        if (raw == null) {
            raw = dictionary.get(TinyTranslations.FALLBACK_DEFAULT_LOCALE);
        }
        if (raw == null) {
            return dictionary.get(TinyTranslations.FALLBACK_DEFAULT_LOCALE);
        }
        return raw;
    }
}
