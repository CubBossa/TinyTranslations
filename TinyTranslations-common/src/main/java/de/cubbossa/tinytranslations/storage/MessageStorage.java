package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TranslationKey;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public interface MessageStorage {

    /**
     * Collects all present locales, even if custom locales like Pirate Speech.
     * @return a Collection of all present locale files. A locale must not have a translation for every key.
     */
    Collection<Locale> fetchLocales();

    /**
     * Reads and returns a whole message storage.
     * The operation does not affect the dictionary of any message instance.
     *
     * @param locale The locale to load all translations for.
     * @return a map of all values that were present in this storage. The map key is a {@link Message} instance that
     * is not yet part of any translations instance and must be assigned to be translatable.
     */
    Map<TranslationKey, String> readMessages(Locale locale);

    /**
     * Writes a collection of {@link Message} instances into the storage. If the storage already contains
     * a translation for a namespaced key, the value will not be affected.
     * The value to write will be provided by the dictionary of the message and the given {@link Locale}.
     *
     * @param messages A collection of messages to write.
     * @param locale   A locale to write the message translations for.
     * @return A collection of Messages that were successfully written into storage.
     */
    Collection<Message> writeMessages(Collection<Message> messages, Locale locale);
}
