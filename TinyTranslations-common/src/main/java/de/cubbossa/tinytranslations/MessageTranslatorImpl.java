package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.AppPathPattern;
import de.cubbossa.tinytranslations.annotation.AppPattern;
import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.nanomessage.tag.ObjectNotationTag;
import de.cubbossa.tinytranslations.nanomessage.tag.MessageTag;
import de.cubbossa.tinytranslations.nanomessage.tag.StyleTag;
import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.StorageEntry;
import de.cubbossa.tinytranslations.storage.StyleStorage;
import de.cubbossa.tinytranslations.tinyobject.InsertedObject;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.cubbossa.tinytranslations.util.MessageUtil.getMessageTranslation;

class MessageTranslatorImpl implements MessageTranslator {

    @Getter
    private final MessageTranslator parent;
    @Getter
    private final @AppPattern String name;
    private final Map<String, MessageTranslator> children;

    @Getter
    private final Map<TranslationKey, Message> messageSet;
    @Getter
    private final StyleSet styleSet;
    @Getter
    private final Collection<TagResolver> resolvers = new LinkedList<>();
    private final Collection<TinyObjectMapping> objectResolvers = new LinkedList<>();
    @Getter
    @Setter
    private @Nullable MessageStorage messageStorage;
    @Getter
    @Setter
    private @Nullable StyleStorage styleStorage;
    @Getter
    @Setter
    private boolean useClientLocale = true;
    private @NotNull Locale defaultLocale = Locale.ENGLISH;

    private final Map<String, InsertedObject> insertedObjects = new HashMap<>();

    private Logger logger = Logger.getLogger("TinyTranslations");

    public MessageTranslatorImpl(MessageTranslator parent, String name) {
        this.parent = parent;
        this.name = name.toLowerCase();

        this.children = new ConcurrentHashMap<>();

        this.messageStorage = null;
        this.styleStorage = null;

        this.messageSet = new HashMap<>();
        this.styleSet = new StyleSet();
        this.logger = Logger.getLogger("TinyTranslations:" + getPath());

        // unregister in close
        AdventureTranslatorAdapter.instance().register(this);
    }

    @Override
    public @AppPathPattern String getPath() {
        if (parent == null) {
            return name;
        }
        return parent.getPath() + "." + name;
    }

    @Override
    public void close() {
        AdventureTranslatorAdapter.instance().unregister(this);

        new HashMap<>(children).forEach((s, translations) -> translations.close());
        if (parent != null) {
            parent.remove(this.name);
        }
    }

    public void remove(String application) {
        var c = children.remove(application);
        if (c != null) {
            c.close();
        }
    }

    @Override
    public MessageTranslator fork(String name) {
        if (children.containsKey(name)) {
            throw new IllegalArgumentException("Another fork with name '" + name + "' already exists.");
        }

        MessageTranslator child = new MessageTranslatorImpl(this, name);
        children.put(name, child);
        child.addAll(objectResolvers);
        return child;
    }

    @Override
    public Message message(String key) {
        Message message = new MessageImpl(TranslationKey.of(getPath(), key));
        messageSet.put(message.getKey(), message);
        return message;
    }

    @Override
    public MessageBuilder messageBuilder(String key) {
        return new MessageBuilder(key) {
            @Override
            public Message build() {
                var msg = super.build();
                addMessage(msg);
                return getMessage(TranslationKey.of(getPath(), key));
            }
        };
    }

    @Override
    public Component translate(Message message, TagResolver... resolvers) {
        return translate(message.formatted(resolvers), defaultLocale());
    }

    @Override
    public Component translate(Message message, Locale locale, TagResolver... resolvers) {
        return translate(message.formatted(resolvers), locale);
    }

    @Override
    public Component translate(String raw, TagResolver... resolvers) {
        return translate(raw, defaultLocale(), resolvers);
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        String key = component.key();

        TinyTranslations.getLogger().finest("Translating message with key '" + key + "'.");

        Message message = getMessageInParentTree(key);
        if (message == null) {
            if (component.key().endsWith(Message.TEMPORARY_MESSAGE_KEY) && component instanceof Message temp) {
                message = temp;
            } else {
                return null;
            }
        }
        final Locale l = useClientLocale ? locale : defaultLocale;
        TagResolver resolver;

        Map<String, InsertedObject> objectMap = new HashMap<>(insertedObjects);
        if (component instanceof Message formatted) {
            if (formatted instanceof UnownedMessage unowned) {
                formatted = unowned.owner(this);
            }
            objectMap.putAll(formatted.insertedObjects());

            resolver = TagResolver.builder()
                    .resolvers(getResolvers())
                    .resolvers(formatted.getResolvers())
                    .resolver(ObjectNotationTag.resolver(objectMap, getTinyObjectResolvers()))
                    .build();
        } else {
            resolver = TagResolver.builder()
                    .resolvers(getResolvers())
                    .resolver(ObjectNotationTag.resolver(insertedObjects, getTinyObjectResolvers()))
                    .build();
        }

        // Translate given translation string with provided resolvers
        var translation = translate(getMessageTranslation(message, l), l, resolver);

        // Cleanup result and return
        if (translation == null) {
            return null;
        }

        // render all translated children again, because they again might be translatable
        if (!translation.children().isEmpty()) {
            translation = translation.children(translation.children().stream()
                    .map(c -> {
                        if (c instanceof Message m) {
                            m.insertedObjects().putAll(objectMap);
                            return m.formatted(resolver);
                        }
                        return c;
                    })
                    .map(c -> GlobalTranslator.renderer().render(
                                c instanceof UnownedMessage
                                    ? ((UnownedMessage) c).owner(this)
                                    : c
                            , l))
                    .toList());
        }
        // add all remaining children on the actual message component
        for (Component child : component.children()) {
            translation = translation.append(child);
        }
        // render hover event - it might also be translatable
        if (component.hoverEvent() != null) {
            if (component.hoverEvent().value() instanceof Component c) {
                component = component.hoverEvent(GlobalTranslator.renderer().render(c, locale));
            }
        }
        // Make sure that every style is being transferred
        translation = Component.empty().style(component.style()).append(translation).compact();
        return translation;
    }

    @Override
    public Component translate(String raw, Locale locale, TagResolver... resolvers) {
        if (raw == null) {
            return null;
        }
        TinyTranslations.getLogger().finest("Formatting value: '" + raw + "'.");

        Collection<TagResolver> r = new LinkedList<>(this.resolvers);
        r.addAll(List.of(resolvers));

        MessageTranslator t = this;
        while (t.getParent() != null) {
            t = t.getParent();
            r.addAll(t.getResolvers());
        }
        r.add(MessageTag.resolver(this));
        r.add(StyleTag.resolver(this));
        var component = TinyTranslations.NM.deserialize(raw, TagResolver.resolver(r));
        if (component == null) {
            return null;
        }
        if (component instanceof Message msg) {
            component = translate(msg, locale);
        }
        return component;
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }

    @Override
    public @NotNull TriState hasAnyTranslations() {
        // anonymous messages always match, therefore always true
        return TriState.TRUE;
    }

    @Override
    public @Nullable Message getMessage(String key) {
        String path = getPath();
        if (key.startsWith(path)) {
            return messageSet.get(TranslationKey.of(path, key.substring(path.length() + 1)));
        }
        return messageSet.get(TranslationKey.of(path, key));
    }

    @Override
    public @Nullable Message getMessage(TranslationKey key) {
        return messageSet.get(key);
    }

    public @Nullable MessageStyle getStyle(String key) {
        return styleSet.get(key);
    }

    public @Nullable MessageStyle getStyleInParentTree(String key) {
        MessageStyle style = getStyle(key);
        if (style != null) return style;
        if (parent == null) return null;
        return parent.getStyleInParentTree(key);
    }

    @Override
    public @Nullable Message getMessageInParentTree(String key) {
        Message msg = getMessage(key);
        if (msg != null) return msg;
        if (parent == null) return null;
        return parent.getMessageInParentTree(key);
    }

    private @Nullable MessageTranslator getTranslationsByNamespace(@AppPathPattern String namespace) {
        MessageTranslator messageTranslator = this;
        String[] split = namespace.split("\\.");
        Queue<String> path = new LinkedList<>(List.of(split));

        // remove global from queue
        path.poll();

        while (!path.isEmpty()) {
            String childName = path.poll();
            messageTranslator = children.get(childName);
            if (messageTranslator == null) {
                return null;
            }
        }
        return messageTranslator;
    }

    @Override
    public @Nullable Message getMessageByNamespace(@AppPathPattern String namespace, String key) {
        if (parent != null) {
            return parent.getMessageByNamespace(namespace, key);
        }
        MessageTranslator messageTranslator = getTranslationsByNamespace(namespace);
        return messageTranslator == null ? null : messageTranslator.getMessageInParentTree(key);
    }

    @Override
    public @Nullable MessageStyle getStyleByNamespace(@AppPathPattern String namespace, String key) {
        if (parent != null) {
            return parent.getStyleByNamespace(namespace, key);
        }
        MessageTranslator messageTranslator = getTranslationsByNamespace(namespace);
        return messageTranslator == null ? null : messageTranslator.getStyleInParentTree(key);
    }

    @Override
    public void addMessage(@NotNull Message message) {
        if (message instanceof UnownedMessage unowned) {
            message = unowned.owner(this);
            messageSet.put(message.getKey(), message);
        } else {
            throw new IllegalArgumentException("The provided message already belongs to a translator. Messages can only belong to one translator.");
        }
    }

    @Override
    public void addMessages(Message... messages) {
        for (Message message : messages) {
            if (message == null) {
                continue;
            }
            addMessage(message);
        }
    }

    @Override
    public void addMessage(Iterable<Message> messages) {
        for (Message message : messages) {
            addMessage(message);
        }
    }

    @Override
    public Collection<TinyObjectMapping> getTinyObjectResolvers() {
        Collection<TinyObjectMapping> result = new LinkedList<>(objectResolvers);
        if (parent != null) {
            result.addAll(parent.getTinyObjectResolvers());
        }
        return result;
    }

    @Override
    public void addAll(Iterable<TinyObjectMapping> resolvers) {
        resolvers.forEach(objectResolvers::add);
    }

    @Override
    public void add(TinyObjectMapping resolver) {
        objectResolvers.add(resolver);
    }

    @Override
    public void remove(TinyObjectMapping resolver) {
        objectResolvers.remove(resolver);
    }

    @Override
    public void loadStyles() {
        try {
            if (parent != null) {
                parent.loadStyles();
            }
            if (styleStorage != null) {
                styleSet.putAll(styleStorage.loadStyles());
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage());
        }
    }

    @Override
    public void saveStyles() {
        if (styleStorage != null) {
            styleStorage.writeStyles(styleSet);
        }
    }

    @Override
    public void loadLocales() {
        if (parent != null) {
            parent.loadLocales();
        }
        if (messageStorage != null) {
            for (Locale availableLocale : messageStorage.fetchLocales()) {
                loadLocale(availableLocale, false);
            }
        }
    }

    @Override
    public void loadLocale(Locale locale) {
        loadLocale(locale, true);
    }

    private void loadLocale(Locale locale, boolean parentCall) {
        if (parentCall && parent != null) {
            parent.loadLocale(locale);
        }
        if (messageStorage != null) {
            Map<String, StorageEntry> keys = new HashMap<>();
            messageStorage.readMessages(locale).forEach((translationKey, s) -> {
                if (messageSet.containsKey(translationKey)) {
                    Message msg = messageSet.get(translationKey);
                    messageSet.put(msg.getKey(), msg.dictionaryEntry(locale, s.value()).comment(s.comment()));
                } else {
                    keys.put(translationKey.key(), s);
                }
            });
            keys.forEach((k, v) -> messageBuilder(k)
                    .withTranslation(locale, v.value())
                    .withComment(v.comment())
                    .build());
        }
        MessageReferenceLoopDetector loopDetector = new MessageReferenceLoopDetector();
        Collection<Message> loops = new LinkedList<>();
        messageSet.values().forEach((message) -> {
            var loop = loopDetector.detectLoops(message, locale);
            if (loop == null) {
                return;
            }
            Map<Locale, String> dict = new HashMap<>(message.dictionary());
            dict.remove(locale);
            loops.add(message.dictionary(dict));
            logger.severe(loop.getMessage());
        });
        loops.forEach(msg -> {
            messageSet.put(msg.getKey(), msg);
        });
    }

    @Override
    public void saveLocale(Locale locale) {
        if (messageStorage != null) {
            messageStorage.writeMessages(messageSet.values(), locale);
        }
    }

    @Override
    public void saveMessagesAndBackupExistingValues(Collection<Message> messages, Locale locale) {
        if (messageStorage == null) {
            return;
        }
        Map<TranslationKey, StorageEntry> loadedValues = messageStorage.readMessages(locale);
        List<Message> list = new ArrayList<>();
        for (Message message : messages) {
            Message stored = getMessage(message.getKey());
            if (stored == null) {
                continue;
            }
            String oldVal = loadedValues.get(message.getKey()).value();
            String newVal = message.dictionary().get(locale);
            if (!Objects.equals(newVal, oldVal)) {
                String comment = "Backed up value: '" + oldVal + "'";
                if (stored.comment() == null || stored.comment().isEmpty()) {
                    stored = stored.comment(comment);
                } else {
                    stored = stored.comment(stored.comment() + "\n" + comment);
                }
                list.add(stored);
            }
        }
        messageStorage.overwriteMessages(list, locale);
        loadLocales();
    }

    @Override
    public void saveMessagesIfOldValueEquals(Map<Message, String> messages, Locale locale) {
        if (messageStorage == null) {
            return;
        }
        Map<TranslationKey, StorageEntry> loadedValues = messageStorage.readMessages(locale);
        List<Message> toOverride = new LinkedList<>();
        for (Map.Entry<Message, String> e : messages.entrySet()) {
            StorageEntry present = loadedValues.get(e.getKey().getKey());
            if (present == null) {
                continue;
            }
            String presentStr = present.value();
            if (presentStr == null) {
                continue;
            }
            if (!presentStr.equals(e.getValue())) {
                continue;
            }
            toOverride.add(e.getKey());
        }
        messageStorage.overwriteMessages(toOverride, locale);
    }

    @Override
    public MessageTranslator formatted(TagResolver... resolver) {
        this.resolvers.addAll(List.of(resolver));
        return this;
    }

    @Override
    public Map<String, InsertedObject> insertedObjects() {
        return insertedObjects;
    }

    @Override
    public <T> MessageTranslator insertObject(@NotNull String key, T obj, Collection<TinyObjectMapping> resolvers) {
        insertedObjects.put(key, new InsertedObject(key, obj, resolvers));
        return this;
    }

    @Override
    public @NotNull Key name() {
        return Key.key(getPath(), getName());
    }

    @Override
    public boolean contains(@NotNull String key) {
        if (messageSet.containsKey(key)) {
            return true;
        }
        String path = getPath();
        if (key.length() < path.length() + 1) {
            return false;
        }
        key = key.substring(path.length() + 1);
        String namespace = key.substring(path.length());
        if (!namespace.equalsIgnoreCase(path)) {
            return false;
        }
        return messageSet.containsKey(key);
    }

    @Override
    public void register(@KeyPattern @NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
        var dict = Map.of(locale, format.toPattern());
        messageSet.getOrDefault(TranslationKey.of(key), message(key).dictionary(dict));
    }

    @Override
    public void unregister(@KeyPattern @NotNull String key) {
        messageSet.remove(TranslationKey.of(key));
    }

    @Override
    public String toString() {
        return "MessageTranslator[path=" + getPath() + "]";
    }

    public Locale defaultLocale() {
        return defaultLocale;
    }

    public void defaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}
