package de.cubbossa.tinytranslations.util;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TinyTranslations;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MessageUtil {

    public static @Nullable String getMessageTranslation(Message message, @Nullable Locale locale) {
        if (locale == null) {
            locale = TinyTranslations.DEFAULT_LOCALE;
        }
        String raw = message.getDictionary().get(locale);
        if (raw == null && !"".equals(locale.getVariant())) {
            raw = message.getDictionary().get(new Locale(locale.getLanguage(), locale.getCountry()));
        }
        if (raw == null && !"".equals(locale.getCountry())) {
            raw = message.getDictionary().get(new Locale(locale.getLanguage()));
        }
        if (raw == null) {
            raw = message.getDictionary().get(TinyTranslations.DEFAULT_LOCALE);
        }
        if (raw == null) {
            return message.getDictionary().get(TinyTranslations.DEFAULT_LOCALE);
        }
        return raw;
    }
}
