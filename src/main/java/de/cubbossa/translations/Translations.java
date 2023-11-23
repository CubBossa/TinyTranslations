package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Translations {

    String getPath();

    void shutdown();

    void remove(String name);

    /**
     * Gets a reference to the parent Translations. This parent must only be null for the global Translations.
     * All other Translations refer to the global translations or to any successor of global.
     * @return The parent instance or null for global Translations.
     */
    @Nullable Translations getParent();

    /**
     * Create a sub Translations instance for the current instance.
     * The new produced fork inherits all styles and all messages as tags. Meaning msg:x.y.z will be resolved from
     * this Translations message set or from the parents message set.
     *
     * Storages are not inherited and must be set manually again.
     *
     * @param name An individual key for this specific instance. There must not be two nest siblings with the same key.
     * @return The forked instance.
     */
    Translations fork(String name);

    /**
     * Similar to {@link #fork(String)}, but also inherits storages.
     *
     * @param name An individual key for this specific instance. There must not be two nest siblings with the same key.
     * @return The forked instance.
     */
    Translations forkWithStorage(String name);


    Message message(String key);

    MessageBuilder messageBuilder(String key);


    Component process(Message message);

    Component process(Message message, Audience target);

    Component process(Message message, Locale locale);

    Component process(String raw);

    Component process(String raw, Audience target);

    Component process(String raw, Locale locale);

    TagResolver getResolvers(Locale locale);


    void loadStyles();

    void saveStyles();

    void loadLocale(Locale locale);

    void saveLocale(Locale locale);


    MiniMessage getMiniMessage();

    void setMiniMessage(MiniMessage miniMessage);


    Map<String, Message> getMessageSet();

    @Nullable MessageStorage getMessageStorage();

    void setMessageStorage(@Nullable MessageStorage storage);

    Map<String, Style> getStyleSet();

    @Nullable StyleStorage getStyleStorage();

    void setStyleStorage(@Nullable StyleStorage storage);


    /**
     * Provides a Locale for each user. This might be the same Locale for all Users.
     * @param user An audience individually representing each user
     * @return The locale
     */
    Locale getUserLocale(@Nullable Audience user);
}
