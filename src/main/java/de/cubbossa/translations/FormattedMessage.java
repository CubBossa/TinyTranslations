package de.cubbossa.translations;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FormattedMessage implements Message {

    protected final Message message;
    protected final Collection<TagResolver> resolvers;
    protected Audience audience;

    public FormattedMessage(Message message) {
        this.message = message;
        this.resolvers = ConcurrentHashMap.newKeySet();
        this.audience = null;
    }

    @Override
    public @Nullable Audience getTarget() {
        return audience == null ? message.getTarget() : audience;
    }

    @Override
    public Collection<TagResolver> getResolvers() {
        Collection<TagResolver> result = new LinkedList<>(resolvers);
        if (message != null) {
            result.addAll(resolvers);
        }
        return result;
    }

    @Override
    public void setTranslations(@NotNull Translations translations) {
        message.setTranslations(translations);
    }

    @Override
    public @NotNull Component asComponent() {
        return audience == null ? message.asComponent() : asComponent(audience);
    }

    @Override
    public @NotNull Component asComponent(Audience audience) {
        return message.asComponent(audience);
    }

    @Override
    public Message formatted(Audience audience) {
        FormattedMessage msg = new FormattedMessage(this);
        msg.audience = audience;
        return msg;
    }

    @Override
    public Message formatted(TagResolver... resolver) {
        FormattedMessage msg = new FormattedMessage(this);
        msg.resolvers.addAll(List.of(resolver));
        return msg;
    }

    @Override
    public String getKey() {
        return message.getKey();
    }

    @Override
    public String getNamespacedKey() {
        return message.getNamespacedKey();
    }

    @Override
    public Translations getTranslations() {
        return message.getTranslations();
    }

    @Override
    public Map<Locale, String> getDictionary() {
        return message.getDictionary();
    }

    @Override
    public Map<String, Optional<String>> getPlaceholderTags() {
        return message.getPlaceholderTags();
    }

    @Override
    public String getComment() {
        return message.getComment();
    }

    @Override
    public void setPlaceholderTags(Map<String, Optional<String>> placeholderTags) {
        message.setPlaceholderTags(placeholderTags);
    }

    @Override
    public void setComment(String comment) {
        message.setComment(comment);
    }

    @Override
    public int compareTo(@NotNull Message o) {
        return message.compareTo(o);
    }
}
