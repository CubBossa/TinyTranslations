package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

class UnownedMessageImpl implements UnownedMessage {

    private final String key;
    private MessageImpl ref;
    private Collection<TempObjectTag> tempTags = new ArrayList<>();

    record TempObjectTag(String key, Object obj, Collection<TinyObjectResolver> additionalResolvers) {}

    public UnownedMessageImpl(@KeyPattern String key) {
        this.key = key;
        ref = new MessageImpl(TranslationKey.of(key));
    }

    @Override
    public TranslationKey getKey() {
        return TranslationKey.of(key);
    }

    @Override
    public Message owner(String namespace) {
        Message m = new MessageImpl(TranslationKey.of(namespace, key), ref);
        for (TempObjectTag tempTag : tempTags) {
            m = m.insertObject(tempTag.key, tempTag.obj, tempTag.additionalResolvers);
        }
        return m;
    }

    @Override
    public Message owner(MessageTranslator translator) {
        Message m = new MessageImpl(TranslationKey.of(translator.getPath(), key), ref);
        for (TempObjectTag tempTag : tempTags) {
            Collection<TinyObjectResolver> merged = new LinkedList<>(translator.getObjectResolversInScope());
            merged.addAll(tempTag.additionalResolvers);
            m = m.insertObject(tempTag.key, tempTag.obj, merged);
        }
        return m;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public Collection<TagResolver> getResolvers() {
        return ref.getResolvers();
    }

    @Override
    public Message formatted(TagResolver... resolver) {
        return wrap(ref.formatted(resolver));
    }

    @Override
    public <T> Message insertObject(@NotNull String key, T obj, Collection<TinyObjectResolver> additionalResolvers) {
        tempTags.add(new TempObjectTag(key, obj, additionalResolvers));
        return this;
    }

    @Override
    public Collection<TinyObjectResolver> getObjectResolversInScope() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "UnownedMessage{key=\"" + key + "\"}";
    }

    @Override
    public String toString(MessageEncoding format) {
        return ref.toString(format);
    }

    @Override
    public String toString(MessageEncoding format, Locale locale) {
        return ref.toString(format, locale);
    }

    @Override
    public Map<Locale, String> getDictionary() {
        return ref.getDictionary();
    }

    @Override
    public Map<String, Optional<String>> getPlaceholderTags() {
        return ref.getPlaceholderTags();
    }

    @Override
    public void setPlaceholderTags(Map<String, Optional<String>> placeholderTags) {
        ref.setPlaceholderTags(placeholderTags);
    }

    @Override
    public @Nullable String getComment() {
        return ref.getComment();
    }

    @Override
    public void setComment(String comment) {
        ref.setComment(comment);
    }

    @Override
    public int compareTo(@NotNull Message o) {
        return ref.compareTo(o);
    }

    @Override
    public @NotNull String key() {
        return ref.key();
    }

    @Override
    public @NotNull TranslatableComponent key(@NotNull String key) {
        return wrap(ref.key(key));
    }

    @Override
    public @NotNull List<Component> args() {
        return ref.args();
    }

    @Override
    public @NotNull List<TranslationArgument> arguments() {
        return ref.arguments();
    }

    @Override
    public @NotNull TranslatableComponent arguments(@NotNull ComponentLike @NotNull ... args) {
        return wrap(ref.arguments(args));
    }

    @Override
    public @NotNull TranslatableComponent arguments(@NotNull List<? extends ComponentLike> args) {
        return wrap(ref.arguments(args));
    }

    @Override
    public @Nullable String fallback() {
        return ref.fallback();
    }

    @Override
    public @NotNull TranslatableComponent fallback(@Nullable String fallback) {
        return wrap(ref.fallback(fallback));
    }

    @Override
    public @NotNull Builder toBuilder() {
        return ref.toBuilder();
    }

    @Override
    public @Unmodifiable @NotNull List<Component> children() {
        return ref.children();
    }

    @Override
    public @NotNull TranslatableComponent children(@NotNull List<? extends ComponentLike> children) {
        return wrap(ref.children(children));
    }

    @Override
    public @NotNull Style style() {
        return ref.style();
    }

    @Override
    public @NotNull TranslatableComponent style(@NotNull Style style) {
        return wrap(ref.style(style));
    }

    @Override
    public @NotNull String translationKey() {
        return ref.translationKey();
    }

    private Message wrap(TranslatableComponent tr) {
        return wrap((MessageImpl) tr);
    }

    private Message wrap(MessageImpl ref) {
        var msg = new UnownedMessageImpl(key);
        msg.tempTags = new LinkedList<>(tempTags);
        msg.ref = ref;
        return msg;
    }
}
