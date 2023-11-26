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


    String getKey();

    String getNamespacedKey();

    @Nullable Translations getTranslations();

    void setTranslations(@NotNull Translations translations);


    @Override
    @NotNull Component asComponent();


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
