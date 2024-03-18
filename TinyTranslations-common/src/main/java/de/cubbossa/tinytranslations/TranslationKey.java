package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a translation key, consisting of a namespace and a key.
 * There are two different representations for a key <pre>TranslationKey{namespace="global", key="prefix"}</pre>
 * <ul>
 *     <li>Namespaced key: 'global:prefix'</li>
 *     <li>Translation key: 'global.prefix'</li>
 * </ul>
 * It is important to notice that both namespace and key can be null.
 * TranslationKeys serve the only purpose to specify the key under which a message is being
 * stored.
 * SQL storages might want to store messages of all translators with their NamespacedKey format in one schema.
 * File based files might want to have one file per translator, so the file will only contain a key and no namespace.
 */
public interface TranslationKey {

    static TranslationKey of(@KeyPattern String namespace, @KeyPattern String key) {
        return new TranslationKeyImpl(namespace, key);
    }

    static TranslationKey of(@KeyPattern String key) {
        return of(null, key);
    }

    @KeyPattern
    @Nullable String namespace();

    @KeyPattern
    @Nullable String key();

    /**
     * @return the TranslationKey in the format
     * <pre>[namespace]:[key]</pre>
     * Both namespace and key are being represented by an empty string if null.
     */
    String asNamespacedKey();


    /**
     * @return the TranslationKey in the format
     * <pre>[namespace].[key]</pre>
     * Both namespace and key are being represented by an empty string if null.
     */
    String asTranslationKey();
}
