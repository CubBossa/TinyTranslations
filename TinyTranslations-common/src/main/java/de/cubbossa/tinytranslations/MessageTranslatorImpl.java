package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.AppPathPattern;
import de.cubbossa.tinytranslations.annotation.AppPattern;
import de.cubbossa.tinytranslations.nanomessage.*;
import de.cubbossa.tinytranslations.nanomessage.tag.*;
import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.StyleStorage;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    @Setter
    private @Nullable MessageStorage messageStorage;
    @Getter
    @Setter
    private @Nullable StyleStorage styleStorage;
    @Getter
    @Setter
    private boolean useClientLocale = true;
    @Getter
    @Setter
    private Locale defaultLocale = Locale.ENGLISH;

    @Getter
    private final Collection<TagResolver> resolvers = new LinkedList<>();

    public MessageTranslatorImpl(MessageTranslator parent, String name) {
        this.parent = parent;
        this.name = name.toLowerCase();

        this.children = new ConcurrentHashMap<>();

        this.messageStorage = null;
        this.styleStorage = null;

        this.messageSet = new HashMap<>();
        this.styleSet = new StyleSet();

        // remove in close
        GlobalTranslator.translator().addSource(this);
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
        GlobalTranslator.translator().removeSource(this);

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
        return translate(getMessageTranslation(message, getDefaultLocale()), getDefaultLocale(), resolvers);
    }

    @Override
    public Component translate(Message message, Locale locale, TagResolver... resolvers) {
        return translate(getMessageTranslation(message, locale), locale, resolvers);
    }

    @Override
    public Component translate(String raw, TagResolver... resolvers) {
        return translate(raw, getDefaultLocale(), resolvers);
    }

    @Override
    public Component translate(String raw, Locale locale, TagResolver... resolvers) {
        if (raw == null) {
            return Component.empty();
        }
        Collection<TagResolver> r = new LinkedList<>(this.resolvers);
        r.addAll(List.of(resolvers));

        MessageTranslator t = this;
        while (t.getParent() != null) {
            t = t.getParent();
            r.addAll(t.getResolvers());
        }
        r.add(MessageTag.resolver(this));
        r.add(StyleTag.resolver(this));
        return TinyTranslations.NM.deserialize(raw, TagResolver.resolver(r));
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
    public void addMessage(Message message) {
        if (message instanceof UnownedMessage unowned && !unowned.isOwned()) {
            message = unowned.setOwner(this);
            messageSet.put(message.getKey(), message);
        } else {
            // TODO throw -> Message already owned
        }
    }

    @Override
    public void addMessages(Message... messages) {
        for (Message message : messages) {
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
    public void loadStyles() {
        try {
            if (parent != null) {
                parent.loadStyles();
            }
            if (styleStorage != null) {
                styleSet.putAll(styleStorage.loadStyles());
            }
        } catch (Throwable t) {

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
        for (Locale availableLocale : Locale.getAvailableLocales()) {
            loadLocale(availableLocale);
        }
    }

    @Override
    public void loadLocale(Locale locale) {
        if (parent != null) {
            parent.loadLocale(locale);
        }
        if (messageStorage != null) {
            messageStorage.readMessages(locale).forEach((translationKey, s) -> {
                messageSet.computeIfAbsent(translationKey, key -> message(key.key())).getDictionary().put(locale, s);
            });
        }
        MessageLoopDetector loopDetector = new MessageLoopDetector();
        messageSet.forEach((s, message) -> {
            var loop = loopDetector.detectLoops(message, locale);
            if (loop == null) {
                return;
            }
            message.getDictionary().remove(locale);
            Logger.getLogger("TinyTranslations").severe(loop.getMessage());
        });
    }

    @Override
    public void saveLocale(Locale locale) {
        if (messageStorage != null) {
            messageStorage.writeMessages(messageSet.values(), locale);
        }
    }

    @Override
    public MessageTranslator formatted(TagResolver... resolver) {
        this.resolvers.addAll(List.of(resolver));
        return this;
    }

    @Override
    public @NotNull Key name() {
        return Key.key(getPath(), getName());
    }

    @Override
    public boolean contains(@NotNull String key) {
        return messageSet.containsKey(key);
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }

    @Override
    public void defaultLocale(@NotNull Locale locale) {
        this.defaultLocale = locale;
    }

    @Override
    public void register(@NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
        messageSet.getOrDefault(key, message(key)).getDictionary().put(locale, format.toPattern());
    }

    @Override
    public void unregister(@NotNull String key) {
        messageSet.remove(key);
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        String key = component.key();
        Message message = getMessage(key);
        if (message == null) {
            return null;
        }
        locale = useClientLocale ? locale : defaultLocale;
        if (component instanceof Message formatted) {
            var tr = translate(getMessageTranslation(formatted, locale), locale, TagResolver.resolver(formatted.getResolvers()));
            for (Component child : component.children()) {
                tr = tr.append(child);
            }
            return tr;
        }
        return null;
    }
}
