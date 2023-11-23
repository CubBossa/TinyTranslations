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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class AppTranslations implements Translations {

    private final Translations parent;
    private final String name;
    private final HashMap<String, Translations> children = new HashMap<>();

    private MiniMessage miniMessage;
    private TagResolver styleResolverCache = null;

    private final Map<String, Message> messageSet;
    private final Map<String, Style> styleSet;
    private @Nullable MessageStorage messageStorage;
    private @Nullable StyleStorage styleStorage;

    public AppTranslations(Translations parent, String name) {
        this.parent = parent;
        this.name = name;

        this.messageSet = new HashMap<>();
        this.messageStorage = null;
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
        this.styleStorage = null;
    }

    @Override
    public String getPath() {
        if (parent == null) {
            return name;
        }
        return parent.getPath() + "." + name;
    }

    @Override
    public void shutdown() {
        children.forEach((s, translations) -> translations.shutdown());
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
        return new Message(this, key);
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
        String raw = message.getDefaultTranslations().get(locale);
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
        return getMiniMessage().deserialize(raw, getResolvers(locale));
    }

    @Override
    public TagResolver getResolvers(Locale locale) {
        TagResolver x = parent == null ? TagResolver.empty() : parent.getResolvers(locale);
        return TagResolver.resolver(getStylesResolver(), getMessageResolver(locale), x);
    }

    private TagResolver getStylesResolver() {
        if (styleResolverCache != null) {
            return styleResolverCache;
        }
        styleResolverCache = TagResolver.resolver(styleSet.entrySet().stream()
            .map(e -> TagResolver.resolver(e.getKey(), Tag.styling(style -> style.merge(e.getValue()))))
            .toList());
        return styleResolverCache;
    }

    private TagResolver getMessageResolver(Locale locale) {
        return TagResolver.resolver("msg", (queue, ctx) -> {
            String messageKey = queue.popOr("The message tag requires a message key, like <msg:error.no_permission>.").value();
            boolean preventBleed = queue.hasNext() && queue.pop().isTrue();

            // TODO loop detection
            return preventBleed
                ? Tag.selfClosingInserting(process(getMessage(messageKey), locale))
                : Tag.inserting(process(getMessage(messageKey), locale));
        });
    }

    public MiniMessage getMiniMessage() {
        return miniMessage == null ? parent.getMiniMessage() : miniMessage;
    }

    private Message getMessage(String key) {
        return messageSet.get(key);
    }

    @Override
    public void loadStyles() {
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
    public void loadLocale(Locale locale) {
        if (messageStorage != null) {
            messageStorage.readMessages(messageSet.values(), locale);
        }
    }

    @Override
    public void saveLocale(Locale locale) {
        if (messageStorage != null) {
            messageStorage.writeMessages(messageSet.values(), locale);
        }
    }

    @Override
    public Locale getUserLocale(@Nullable Audience user) {
        return parent.getUserLocale(user);
    }
}
