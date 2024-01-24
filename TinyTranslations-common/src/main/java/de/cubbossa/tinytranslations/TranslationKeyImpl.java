package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

record TranslationKeyImpl(@KeyPattern @Nullable String namespace,
                          @KeyPattern @Nullable String key) implements TranslationKey {

    TranslationKeyImpl(@Nullable @KeyPattern String namespace, @Nullable @KeyPattern String key) {
        this.namespace = namespace;
        this.key = key;
    }

    @Override
    public String toString() {
        return asNamespacedKey();
    }

    public String asTranslationKey() {
        return asString('.');
    }

    public String asNamespacedKey() {
        return asString(':');
    }

    private String asString(char separator) {
        return (namespace() == null ? "" : (namespace() + separator)) + (key() == null ? "" : key());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof TranslationKey t) {
            if (t.asTranslationKey().equals(asTranslationKey())) {
                return true;
            }
            return (t.namespace() == null || namespace == null) && Objects.equals(t.key(), key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key().hashCode();
    }
}
