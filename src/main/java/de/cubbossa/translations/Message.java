package de.cubbossa.translations;

import de.cubbossa.translations.annotation.KeyPattern;
import de.cubbossa.translations.annotation.PathPattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface Message extends ComponentLike, Cloneable, Comparable<Message> {


    @KeyPattern String getKey();

    @PathPattern String getNamespacedKey();

    @Nullable Translations getTranslations();

    void setTranslations(@NotNull Translations translations);


    @Override
    @NotNull Component asComponent();

    String toString(MessageFormat format);


    @Contract(pure = true)
    Message formatted(Audience audience);

    @Contract(pure = true)
    Message formatted(TagResolver... resolver);

    @Contract(pure = true)
    default Message insertString(final @NotNull String key, String value) {
        return formatted(Placeholder.unparsed(key, value));
    }

    @Contract(pure = true)
    default Message insertStringLazy(final @NotNull String key, Supplier<String> value) {
        return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
            return Tag.preProcessParsed(value.get());
        }));
    }

    @Contract(pure = true)
    default Message insertComponent(final @NotNull String key, ComponentLike value) {
        return formatted(Placeholder.component(key, value));
    }

    @Contract(pure = true)
    default Message insertComponentLazy(final @NotNull String key, Supplier<ComponentLike> value) {
        return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
            return Tag.inserting(value.get());
        }));
    }

    @Contract(pure = true)
    default Message insertNumber(final @NotNull String key, Number value) {
        return formatted(Formatter.number(key, value));
    }

    @Contract(pure = true)
    default Message insertNumberLazy(final @NotNull String key, Supplier<Number> value) {
        return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
            return Formatter.number(key, value.get()).resolve(key, argumentQueue, context);
        }));
    }

    @Contract(pure = true)
    default Message insertNumberChoice(final @NotNull String key, Number number) {
        return formatted(Formatter.choice(key, number));
    }

    @Contract(pure = true)
    default Message insertTemporal(final @NotNull String key, TemporalAccessor value) {
        return formatted(Formatter.date(key, value));
    }

    @Contract(pure = true)
    default Message insertBool(final @NotNull String key, Boolean value) {
        return formatted(Formatter.booleanChoice(key, value));
    }

    @Contract(pure = true)
    default Message insertTag(final @NotNull String key, Tag tag) {
        return formatted(TagResolver.resolver(key, tag));
    }

    @Nullable Audience getTarget();

    Collection<TagResolver> getResolvers();


    Map<Locale, String> getDictionary();

    Map<String, Optional<String>> getPlaceholderTags();

    void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

    String getComment();

    void setComment(String comment);
}
