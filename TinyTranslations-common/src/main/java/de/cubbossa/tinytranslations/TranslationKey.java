package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface TranslationKey {

    @KeyPattern @Nullable String namespace();
    @KeyPattern @Nullable String key();

    String asNamespacedKey();

    String asTranslationKey();

    static TranslationKey of(@KeyPattern String namespace, @KeyPattern String key) {
        return new TranslationKeyImpl(namespace, key);
    }

    static TranslationKey of(@KeyPattern String key) {
        return of(null, key);
    }
}
