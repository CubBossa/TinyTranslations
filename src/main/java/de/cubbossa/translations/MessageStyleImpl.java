package de.cubbossa.translations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

public class MessageStyleImpl implements MessageStyle {

    private final String key;
    private final TagResolver resolver;
    private final @Nullable String backup;

    public MessageStyleImpl(String key, TagResolver resolver) {
        this(key, resolver, null);
    }

    public MessageStyleImpl(String key, TagResolver resolver, @Nullable String backup) {
        this.key = key;
        this.resolver = resolver;
        this.backup = backup;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public TagResolver getResolver() {
        return resolver;
    }

    @Override
    public @Nullable String getStringBackup() {
        return backup;
    }
}
