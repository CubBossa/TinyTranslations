package de.cubbossa.tinytranslations.impl;

import de.cubbossa.tinytranslations.*;
import de.cubbossa.tinytranslations.annotation.AppPathPattern;
import de.cubbossa.tinytranslations.annotation.AppPattern;
import de.cubbossa.tinytranslations.nanomessage.*;
import de.cubbossa.tinytranslations.nanomessage.tag.*;
import de.cubbossa.tinytranslations.persistent.MessageStorage;
import de.cubbossa.tinytranslations.persistent.StyleStorage;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.logging.Logger;

import static de.cubbossa.tinytranslations.util.MessageUtil.getMessageTranslation;

public class AppTranslator implements Translator {

    @Getter
    private final Translator parent;
    @Getter
    private final @AppPattern String name;
    private final Map<String, Translator> children;

    private Function<@Nullable Audience, @NotNull Locale> localeProvider = null;

    @Getter
    private final Map<String, Message> messageSet;
    @Getter
    private final StyleSet styleSet;
    @Getter
    @Setter
    private @Nullable MessageStorage messageStorage;
    @Getter
    @Setter
    private @Nullable StyleStorage styleStorage;

    private ReadWriteLock lock;

    @Getter
    private final Collection<NanoResolver> resolvers = new LinkedList<>();

    public AppTranslator(Translator parent, String name) {
        this.parent = parent;
        this.name = name;

        this.children = new ConcurrentHashMap<>();

        this.messageStorage = null;
        this.styleStorage = null;

        this.messageSet = new HashMap<>() {
            @Override
            public Message put(String key, Message value) {
                value.setTranslator(AppTranslator.this);
                return super.put(key, value);
            }
        };
        this.styleSet = new StyleSet();
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
    public Translator fork(String name) {
        if (children.containsKey(name)) {
            throw new IllegalArgumentException("Another fork with name '" + name + "' already exists.");
        }

        Translator child = new AppTranslator(this, name);
        children.put(name, child);
        return child;
    }

    @Override
    public Translator forkWithStorage(String name) {
        Translator child = fork(name);
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
        return process(message, getUserLocale(null));
    }

    @Override
    public Component process(Message message, Audience target) {
        return process(message, getUserLocale(target));
    }

    @Override
    public Component process(Message message, Locale locale) {
        return process(getMessageTranslation(message, locale), new Context(locale, message.getResolvers()));
    }

    @Override
    public Component process(Message message, Context context, TagResolver... resolvers) {
        return process(getMessageTranslation(message, context.getLocale()), context, resolvers);
    }

    @Override
    public Component process(String raw, TagResolver... resolvers) {
        return process(raw, new Context(getUserLocale(null), resolvers));
    }

    @Override
    public Component process(String raw, Audience target, TagResolver... resolvers) {
        return process(raw, new Context(getUserLocale(target), resolvers));
    }

    @Override
    public Component process(String raw, Locale locale, TagResolver... resolvers) {
        return process(raw, new Context(locale, resolvers));
    }

    @Override
    public Component process(String raw, Context context, TagResolver... resolvers) {
        Collection<NanoResolver> r = new LinkedList<>(context.getResolvers());
        r.addAll(this.resolvers);

        Translator t = this;
        while (t.getParent() != null) {
            t = t.getParent();
            r.addAll(t.getResolvers());
        }
        r.add(MessageTag.resolver(this));
        r.add(StyleTag.resolver(this));
        return TinyTranslations.NM.parse(raw, new Context(context.getLocale(), r), TagResolver.resolver(resolvers));
    }

    @Override
    public @Nullable Message getMessage(String key) {
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

    private @Nullable Translator getTranslationsByNamespace(@AppPathPattern String namespace) {
        Translator translator = this;
        String[] split = namespace.split("\\.");
        Queue<String> path = new LinkedList<>(List.of(split));

        // remove global from queue
        path.poll();

        while (!path.isEmpty()) {
            String childName = path.poll();
            translator = children.get(childName);
            if (translator == null) {
                return null;
            }
        }
        return translator;
    }

    @Override
    public @Nullable Message getMessageByNamespace(@AppPathPattern String namespace, String key) {
        if (parent != null) {
            return parent.getMessageByNamespace(namespace, key);
        }
        Translator translator = getTranslationsByNamespace(namespace);
        return translator == null ? null : translator.getMessageInParentTree(key);
    }

    @Override
    public @Nullable MessageStyle getStyleByNamespace(@AppPathPattern String namespace, String key) {
        if (parent != null) {
            return parent.getStyleByNamespace(namespace, key);
        }
        Translator translator = getTranslationsByNamespace(namespace);
        return translator == null ? null : translator.getStyleInParentTree(key);
    }

    @Override
    public void addMessage(Message message) {
        if (message.getTranslator() != null) {
            message.getTranslator().getMessageSet().remove(message.getKey());
        }
        messageSet.put(message.getKey(), message);
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
            messageStorage.readMessages(locale).forEach((message, s) -> {
                message.setTranslator(this);
                messageSet.computeIfAbsent(message.getKey(), key -> message).getDictionary().put(locale, s);
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

    @Override
    public Translator formatted(TagResolver... resolver) {
        this.resolvers.addAll(Arrays.stream(resolver).map(r -> (NanoResolver) c -> r).toList());
        return this;
    }

    @Override
    public Translator formatted(NanoResolver... nanoResolver) {
        this.resolvers.addAll(List.of(nanoResolver));
        return null;
    }
}
