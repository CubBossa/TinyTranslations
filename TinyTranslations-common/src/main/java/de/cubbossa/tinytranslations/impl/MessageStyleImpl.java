package de.cubbossa.tinytranslations.impl;

import de.cubbossa.tinytranslations.MessageStyle;
import de.cubbossa.tinytranslations.nanomessage.tag.NanoResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MessageStyleImpl implements MessageStyle {

    private final String key;
    private final NanoResolver resolver;
    private final @Nullable String backup;

    public MessageStyleImpl(String key, NanoResolver resolver) {
        this(key, resolver, null);
    }
    public MessageStyleImpl(String key, TagResolver resolver) {
        this(key, c -> resolver, null);
    }

    public MessageStyleImpl(String key, NanoResolver resolver, @Nullable String backup) {
        this.key = key;
        this.resolver = resolver;
        this.backup = backup;
    }

    public MessageStyleImpl(String key, TagResolver resolver, @Nullable String backup) {
        this(key, c -> resolver, backup);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageStyleImpl that = (MessageStyleImpl) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public NanoResolver getResolver() {
        return resolver;
    }

    @Override
    public @Nullable String getStringBackup() {
        return backup;
    }
}
