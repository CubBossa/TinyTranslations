package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.AppPathPattern;
import de.cubbossa.tinytranslations.annotation.AppPattern;
import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.StyleStorage;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectTagResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.TranslationRegistry;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public interface MessageTranslator extends AutoCloseable, Formattable<MessageTranslator>, TranslationRegistry {

    @AppPathPattern
    String getPath();

    void close();

    void remove(String name);

    /**
     * Gets a reference to the parent Translations. This parent must only be null for the global Translations.
     * All other Translations refer to the global translations or to any successor of global.
     *
     * @return The parent instance or null for global Translations.
     */
    @Nullable MessageTranslator getParent();

    /**
     * Create a sub Translations instance for the current instance.
     * The new produced fork inherits all styles and all messages as tags. Meaning msg:x.y.z will be resolved from
     * this Translations message set or from the parents message set.
     * <p>
     * Storages are not inherited and must be set manually.
     *
     * @param name An individual key for this specific instance. There must not be two nest siblings with the same key.
     * @return The forked instance.
     */
    MessageTranslator fork(@AppPattern String name);


    /**
     * Creates a new {@link Message}. There is no need to add a {@link Message} created via {@link MessageTranslator}
     * to it by calling {@link #addMessage(Message)} again.
     *
     * @param key The message key.
     * @return A new owned {@link Message} instance.
     */
    Message message(@KeyPattern String key);

    /**
     * Creates a new {@link MessageBuilder} that automatically adds the created {@link Message} to this {@link MessageTranslator}
     * when building.
     * @param key The message key.
     * @return A new {@link MessageBuilder} instance.
     */
    MessageBuilder messageBuilder(@KeyPattern String key);

    Component translate(Message message, TagResolver... resolvers);

    /**
     * Turns a message into a component. All embedded messages or styles will be resolved.
     * If the message has {@link TagResolver}s specified, these will be resolved in the process.
     *
     * @param message Any message instance.
     * @param locale  A target locale that the message will be translated into.
     * @return The message translated into a component.
     */
    Component translate(Message message, Locale locale, TagResolver... resolvers);

    /**
     * Processes a raw string as if it were a translation value of a Message.
     * The default locale will be used to resolve messages.
     *
     * @param raw       A raw string that might start with a MessageEncoding prefix. Otherwise, assuming MiniMessage format.
     * @param resolvers A collection of resolvers to include into the resolving process.
     * @return The processed Component that resembles the input string.
     */
    Component translate(@Language("NanoMessage") String raw, TagResolver... resolvers);

    /**
     * Processes a raw string as if it were a translation value of a Message.
     * The given locale will be used to resolve embedded messages.
     *
     * @param raw       A raw string that might start with a MessageEncoding prefix. Otherwise, assuming MiniMessage format.
     * @param locale    A locale to use to resolve embedded messages.
     * @param resolvers A collection of resolvers to include into the resolving process.
     * @return The processed Component that resembles the input string.
     */
    Component translate(@Language("NanoMessage") String raw, Locale locale, TagResolver... resolvers);

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
     *
     * @param locale A locale instance to load from a storage.
     */
    void loadLocale(Locale locale);

    /**
     * Saves the current dictionary values for the given language and all registered messages of this Translations instance
     * to a storage if a storage instance is set.
     *
     * @param locale A locale instance to save message values for.
     */
    void saveLocale(Locale locale);

    /**
     * Backs up the existing values in translation files in the according message comments
     * and writes a new value. Don't call this regularly in a startup routine and only when your plugin
     * migrates between versions. A migration tool like Flyway might be useful.
     *
     * @param messages A list of messages whose dictionary has changed for the specified locale.
     * @param locale The locale to execute this operation for.
     */
    void saveMessagesAndBackupExistingValues(Collection<Message> messages, Locale locale);

    /**
     * Changes a message only if its value still equals a certain value, most likely the value that
     * was generated earlier. With this method, you can fix spelling mistakes in messages when migrating, but
     * only if the administrators haven't changed anything on this translation already.
     * @param messages A map of messages and their check values. If the check values are equals to the stored values,
     *                 the message will be overwritten without backup.
     * @param locale The locale to execute this operation for.
     */
    void saveMessagesIfOldValueEquals(Map<Message, String> messages, Locale locale);

    /**
     * Find a registered message by key.
     *
     * @param key The message key without namespace.
     * @return A message instance if the message existed and otherwise null.
     */
    @Nullable Message getMessage(String key);

    /**
     * Find a registered message by key.
     *
     * @param key The message key.
     * @return A message instance if the message existed and otherwise null.
     */
    @Nullable Message getMessage(TranslationKey key);

    /**
     * Find a registered MessageStyle by its key.
     * @param key The style key
     * @return The {@link MessageStyle} instance or null if no style with the given key exists.
     */
    @Nullable MessageStyle getStyle(String key);

    /**
     * Find a registered message on this Translations instance and if nothing found, search in parent tree.
     * The method returns the message from the lowest {@link MessageTranslator} in the parent tree that contains the searched message key.
     *
     * @param key The message key without namespace.
     * @return The found Message instance or null if none found.
     */
    @Nullable Message getMessageInParentTree(String key);

    /**
     * Find a {@link MessageStyle} on this Translations instance and if nothing found, search in parent tree.
     * The method returns the style of the lowest {@link MessageTranslator} in the parent tree that contains a style with the given key.
     *
     * @param key The style key.
     * @return The found style or null if none found.
     */

    @Nullable MessageStyle getStyleInParentTree(String key);

    /**
     * Retrieve a message by namespace. Return an exact match, even if child Translations overwrite the message.
     * Calling this method with the parameters 'global' and 'a', the global message by key 'a' will be returned even
     * if a child Translation also has a translation by key 'a'.
     *
     * @param namespace The exact application path (global.A.B.C)
     * @param key       The exact message key (a.b.c)
     * @return The found message instance or null if none found.
     */
    @Nullable Message getMessageByNamespace(@AppPathPattern String namespace, String key);

    @Nullable MessageStyle getStyleByNamespace(@AppPathPattern String namespace, String key);

    /**
     * Adds a message to this translations instance and sets the Translations instance of the message to this.
     * If the message had a previous Translations instance, it gets informed about the change and the message will be
     * removed from its MessageTranslator property.
     *
     * @param message Any message instance.
     */
    void addMessage(Message message);

    /**
     * {@link #addMessage(Message)} for multiple Messages at once.
     *
     * @param messages An array of Messages to add
     */
    void addMessages(Message... messages);

    /**
     * {@link #addMessage(Message)} for multiple Messages at once.
     *
     * @param messages An iterable of Messages to add
     */
    void addMessage(Iterable<Message> messages);

    Map<TranslationKey, Message> getMessageSet();

    /**
     * Returns the potentially not existent MessageStorage instance of this Translation.
     * If no storage is set, the Translation works in memory only and on restart, changes to
     * Messages and Message dictionaries are gone.
     * To persist changes if a MessageStorage is present, call {@link #saveLocale(Locale)}.
     *
     * @return the MessageStorage instance or null if not present.
     */
    @Nullable MessageStorage getMessageStorage();

    void setMessageStorage(@Nullable MessageStorage storage);

    StyleSet getStyleSet();

    @Nullable StyleStorage getStyleStorage();

    void setStyleStorage(@Nullable StyleStorage storage);

    boolean isUseClientLocale();

    void setUseClientLocale(boolean clientLocale);

    Locale defaultLocale();


    // Tiny object resolving

    Collection<TinyObjectResolver> getTinyObjectResolvers();

    void addAll(Iterable<TinyObjectResolver> resolvers);

    void add(TinyObjectResolver resolver);

    void remove(TinyObjectResolver resolver);
}
