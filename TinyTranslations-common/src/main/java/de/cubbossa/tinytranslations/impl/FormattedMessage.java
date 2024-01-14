package de.cubbossa.tinytranslations.impl;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageFormat;
import de.cubbossa.tinytranslations.Translator;
import de.cubbossa.tinytranslations.nanomessage.Context;
import de.cubbossa.tinytranslations.nanomessage.tag.NanoResolver;
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
    protected final Collection<NanoResolver> nanoResolvers;
    protected Audience audience;

    public FormattedMessage(Message message) {
        this.message = message;
        this.resolvers = ConcurrentHashMap.newKeySet();
        this.nanoResolvers = ConcurrentHashMap.newKeySet();
        this.audience = null;
    }

    @Override
    public @Nullable Audience getTarget() {
        return audience == null ? message.getTarget() : audience;
    }

    @Override
    public Collection<NanoResolver> getResolvers() {
        Collection<NanoResolver> result = new LinkedList<>(nanoResolvers);
        result.addAll(resolvers.stream().map(r -> (NanoResolver) c -> r).toList());
        if (message != null) {
            result.addAll(message.getResolvers());
        }
        return result;
    }

    @Override
    public void setTranslator(@NotNull Translator translator) {
        message.setTranslator(translator);
    }

    @Override
    public @NotNull Component asComponent() {
        if (message.getTranslator() == null) {
            throw new IllegalStateException("Trying to translate a Message before registering it to a Translations instance.");
        }
        return message.getTranslator().process(this, getTarget());
    }

    @Override
    public String toString(MessageFormat format) {
        return format.format(asComponent());
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
    public Message formatted(NanoResolver... nanoResolver) {
        FormattedMessage msg = new FormattedMessage(this);
        msg.nanoResolvers.addAll(List.of(nanoResolver));
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
    public Translator getTranslator() {
        return message.getTranslator();
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

    @Override
    public String toString() {
        return "Message<" + getNamespacedKey() + ">";
    }
}
