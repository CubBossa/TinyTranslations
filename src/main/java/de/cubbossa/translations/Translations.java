package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public interface Translations extends AutoCloseable {

    String getPath();

    void close();

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

    Component process(String raw, TagResolver... resolvers);

    Component process(String raw, Audience target, TagResolver... resolvers);

    Component process(String raw, Locale locale, TagResolver... resolvers);

    TagResolver getResolvers(Locale locale);


    /**
     * Loads all styles from this application from file. Also propagates to global, so all parenting Translation
     * instances reload their styles.
     */
    void loadStyles();

    /**
     * Saves the styles to the styles storage if present. Does not propagate, style changes on parent Translations must
     * be saved manually.
     */
    void saveStyles();

    /**
     * Calls {@link #loadLocale(Locale)} for every existing locale. Locales that are not included in {@link Locale#getAvailableLocales()}
     * must be loaded manually.
     * Propagates to all parenting Translations.
     */
    void loadLocales();

    /**
     * Loads a locale from storage, if a storage instance is set. Propagates to all parenting Translations.
     * It saves the results in the dictionary of all registered messages. So clones of a message will only be
     * updated if the clone again is part of this Translations instance.
     * @param locale A locale instance to load from a storage.
     */
    void loadLocale(Locale locale);

    /**
     * Saves the current dictionary values for the given language and all registered messages of this Translations instance
     * to a storage if a storage instance is set.
     * @param locale A locale instance to save message values for.
     */
    void saveLocale(Locale locale);


    MiniMessage getMiniMessage();

    void setMiniMessage(MiniMessage miniMessage);


    @Nullable Message getMessage(String key);

    @Nullable Message getMessageInParentTree(String key);

    @Nullable Message getMessageByNamespace(String namespace, String key);

    void addMessage(Message message);

    void addMessages(Message... messages);

    Map<String, Message> getMessageSet();

    @Nullable MessageStorage getMessageStorage();

    void setMessageStorage(@Nullable MessageStorage storage);

    Map<String, Style> getStyleSet();

    @Nullable StyleStorage getStyleStorage();

    void setStyleStorage(@Nullable StyleStorage storage);


    void setLocaleProvider(Function<@Nullable Audience, @NotNull Locale> function);

    /**
     * Provides a Locale for each user. This might be the same Locale for all Users.
     * @param user An audience individually representing each user
     * @return The locale
     */
    @NotNull Locale getUserLocale(@Nullable Audience user);
}
