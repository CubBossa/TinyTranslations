package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

@Getter
@Setter
public class AppTranslations implements Translations {

    private final Translations parent;
    private final String name;
    private final Map<String, Translations> children;

    private MiniMessage miniMessage;
    private TagResolver styleResolverCache = null;

    private Function<@Nullable Audience, @NotNull Locale> localeProvider = null;

    private final Map<String, Message> messageSet;
    private final Map<String, Style> styleSet;
    private @Nullable MessageStorage messageStorage;
    private @Nullable StyleStorage styleStorage;

    private ReadWriteLock lock;

    public AppTranslations(Translations parent, String name) {
        this.parent = parent;
        this.name = name;

        this.children = new ConcurrentHashMap<>();

        this.messageStorage = null;
        this.styleStorage = null;

        this.messageSet = new HashMap<>() {
            @Override
            public Message put(String key, Message value) {
                value.setTranslations(AppTranslations.this);
                return super.put(key, value);
            }
        };
        this.styleSet = new HashMap<>() {
            @Override
            public Style put(String key, Style value) {
                styleResolverCache = null;
                return super.put(key, value);
            }

            @Override
            public boolean remove(Object key, Object value) {
                styleResolverCache = null;
                return super.remove(key, value);
            }
        };
    }

    @Override
    public String getPath() {
        if (parent == null) {
            return name;
        }
        return parent.getPath() + "." + name;
    }

    @Override
    public void close() {
        new HashMap<>(children).forEach((s, translations) -> translations.close());
        if (parent != null) {
            parent.remove(this.name);
        }
    }

    public void remove(String application) {
        children.remove(application);
    }

    @Override
    public Translations fork(String name) {
        if (children.containsKey(name)) {
            throw new IllegalArgumentException("Another fork with name '" + name + "' already exists.");
        }

        Translations child = new AppTranslations(this, name);
        children.put(name, child);
        return child;
    }

    @Override
    public Translations forkWithStorage(String name) {
        Translations child = fork(name);
        child.setMessageStorage(messageStorage);
        child.setStyleStorage(styleStorage);
        return child;
    }

    @Override
    public Message message(String key) {
        return new MessageCore(this, key);
    }

    @Override
    public MessageBuilder messageBuilder(String key) {
        return new MessageBuilder(this, key);
    }

    @Override
    public Component process(Message message) {
        return process(message, (Audience) null);
    }

    @Override
    public Component process(Message message, Audience target) {
        Locale locale = getUserLocale(target);
        return process(message, locale);
    }

    @Override
    public Component process(Message message, Locale locale) {
        String raw = message.getDictionary().get(locale);
        if (raw == null && !"".equals(locale.getVariant())) {
            raw = message.getDictionary().get(new Locale(locale.getLanguage(), locale.getCountry()));
        }
        if (raw == null && !"".equals(locale.getCountry())) {
            raw = message.getDictionary().get(new Locale(locale.getLanguage()));
        }
        if (raw == null) {
            raw = message.getDictionary().get(TranslationsFramework.DEFAULT_LOCALE);
        }
        if (raw == null) {
            raw = "<missing translation: " + message.getNamespacedKey() + ">";
        }
        return process(raw, locale);
    }

    @Override
    public Component process(String raw) {
        return process(raw, (Audience) null);
    }

    @Override
    public Component process(String raw, Audience target) {
        return process(raw, getUserLocale(target));
    }

    @Override
    public Component process(String raw, Locale locale) {
        return MessageCore.Format.translate(raw, getResolvers(locale));
    }

    @Override
    public TagResolver getResolvers(Locale locale) {
        return TagResolver.resolver(getStylesResolver(), getMessageResolver(locale));
    }

    private TagResolver getStylesResolver() {
        if (styleResolverCache != null) {
            return styleResolverCache;
        }
        Map<String, TagResolver> styles = new HashMap<>();

        Translations t = this;
        while (t != null) {
            t.getStyleSet().forEach((key, value) -> {
                if (styles.containsKey(key)) return;
                var res = TagResolver.resolver(key, Tag.styling(style -> style.merge(value)));
                styles.put(key, res);
            });
            t = t.getParent();
        }
        styleResolverCache = TagResolver.resolver(styles.values());
        return styleResolverCache;
    }

    private TagResolver getMessageResolver(Locale locale) {
        return TagResolver.resolver("msg", (queue, ctx) -> {
            String nameSpace;
            String key = queue.popOr("The message tag requires a message key, like <msg:error.no_permission>.").value();
            if (queue.hasNext()) {
                nameSpace = key;
                key = queue.pop().value();
            } else {
                Message msg = getMessageInParentTree(key);
                if (msg == null) {
                    return Tag.inserting(Component.text("<msg-not-found:" + key + ">"));
                }
                return Tag.inserting(process(msg, locale));
            }
            Message msg = getMessageByNamespace(nameSpace, key);
            if (msg == null) {
                return Tag.inserting(Component.text("<msg-not-found:" + key + ">"));
            }
            return Tag.inserting(process(msg, locale));
        });
    }

    public MiniMessage getMiniMessage() {
        return miniMessage == null ? parent.getMiniMessage() : miniMessage;
    }

    @Override
    public @Nullable Message getMessage(String key) {
        return messageSet.get(key);
    }

    @Override
    public @Nullable Message getMessageInParentTree(String key) {
        Message msg = getMessage(key);
        if (msg != null) return msg;
        if (parent == null) return null;
        return parent.getMessageInParentTree(key);
    }

    @Override
    public @Nullable Message getMessageByNamespace(String namespace, String key) {
        if (parent != null) {
            return parent.getMessageByNamespace(namespace, key);
        }
        Translations translations = this;
        String[] split = namespace.split("\\.");
        Queue<String> path = new LinkedList<>(List.of(split));

        // remove global from queue
        path.poll();

        while (!path.isEmpty()) {
            String childName = path.poll();
            translations = children.get(childName);
            if (translations == null) {
                return null;
            }
        }
        return translations.getMessageInParentTree(key);
    }

    @Override
    public void addMessage(Message message) {
        messageSet.put(message.getKey(), message);
    }

    @Override
    public void addMessages(Message... messages) {
        for (Message message : messages) {
            addMessage(message);
        }
    }

    @Override
    public void loadStyles() {
        if (parent != null) {
            parent.loadStyles();
        }
        if (styleStorage != null) {
            styleSet.putAll(styleStorage.loadStyles());
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
            messageStorage.readMessages(messageSet.values(), locale).forEach((message, s) -> {
                message.getDictionary().put(locale, s);
                addMessage(message);
            });
        }
    }

    @Override
    public void saveLocale(Locale locale) {
        if (messageStorage != null) {
            messageStorage.writeMessages(messageSet.values(), locale);
        }
    }

    @Override
    public void setLocaleProvider(Function<@Nullable Audience, @NotNull Locale> function) {
        localeProvider = function;
    }

    @Override
    public @NotNull Locale getUserLocale(@Nullable Audience user) {
        if (localeProvider != null) {
            return localeProvider.apply(user);
        }
        return parent == null ? Locale.ENGLISH : parent.getUserLocale(user);
    }
}
