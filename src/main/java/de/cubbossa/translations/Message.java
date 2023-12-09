package de.cubbossa.translations;

import de.cubbossa.translations.annotation.KeyPattern;
import de.cubbossa.translations.annotation.PathPattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    @Nullable Audience getTarget();

    Collection<TagResolver> getResolvers();


    Map<Locale, String> getDictionary();

    Map<String, Optional<String>> getPlaceholderTags();

    void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

    String getComment();

    void setComment(String comment);
}
