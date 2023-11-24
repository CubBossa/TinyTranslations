package de.cubbossa.translations;

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

    @Override
    @NotNull Component asComponent();

    @NotNull Component asComponent(Audience audience);

    @Contract(pure = true)
    Message formatted(Audience audience);

    @Contract(pure = true)
    Message formatted(TagResolver... resolver);

    String getKey();

    String getNamespacedKey();

    @Nullable Translations getTranslations();

    @Nullable Audience getTarget();

    Collection<TagResolver> getResolvers();

    void setTranslations(@NotNull Translations translations);

    Map<Locale, String> getDictionary();

    Map<String, Optional<String>> getPlaceholderTags();

    String getComment();

    void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

    void setComment(String comment);
}
