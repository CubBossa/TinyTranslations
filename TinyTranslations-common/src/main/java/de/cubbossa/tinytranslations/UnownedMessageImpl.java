package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.tinyobject.InsertedObject;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
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
        return new MessageImpl(TranslationKey.of(namespace, key), ref);
    }

    @Override
    public Message owner(MessageTranslator translator) {
        return new MessageImpl(TranslationKey.of(translator.getPath(), key), ref);
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
    public Map<String, InsertedObject> insertedObjects() {
        return ref.insertedObjects();
    }

    @Override
    public <T> Message insertObject(@NotNull String key, T obj) {
        return wrap(ref.insertObject(key, obj));
    }

    @Override
    public <T> Message insertObject(@NotNull String key, T obj, Collection<TinyObjectMapping> additionalResolvers) {
        ref.insertObject(key, obj, additionalResolvers);
        return this;
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
    public Map<String, Optional<String>> getPlaceholderDescriptions() {
        return ref.getPlaceholderDescriptions();
    }

    @Override
    public void setPlaceholderDescriptions(Map<String, Optional<String>> placeholderDescriptions) {
        ref.setPlaceholderDescriptions(placeholderDescriptions);
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
    public @Nullable String comment() {
        return ref.comment();
    }

    @Override
    public Message comment(@Nullable String comment) {
        return wrap(ref.comment(comment));
    }

    @Override
    public Map<Locale, String> dictionary() {
        return ref.dictionary();
    }

    @Override
    public Message dictionary(Map<Locale, String> dictionary) {
        return wrap(ref.dictionary(dictionary));
    }

    @Override
    public Collection<PlaceholderDescription> placeholderDescriptions() {
        return ref.placeholderDescriptions();
    }

    @Override
    public Message placeholderDescriptions(Collection<PlaceholderDescription> descriptions) {
        return wrap(ref.placeholderDescriptions(descriptions));
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
        msg.ref = ref;
        return msg;
    }
}
