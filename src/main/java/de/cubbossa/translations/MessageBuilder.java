package de.cubbossa.translations;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.stream.Collectors;

public class MessageBuilder {

    private final Translations owner;
    private final String key;
    private String defaultValue;
    private final Map<Locale, String> translations;
    private final List<String> comments;
    private final Map<String, Placeholder> placeholderMap;

    public MessageBuilder(Translations translations, String key) {
        this.key = key;
        this.owner = translations;
        this.comments = new ArrayList<>();
        this.translations = new HashMap<>();
        this.placeholderMap = new HashMap<>();
    }

    public MessageBuilder withComment(String line) {
        this.comments.add(line);
        return this;
    }

    public MessageBuilder withDefault(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public MessageBuilder withTranslation(Locale locale, String miniMessage) {
        return this.withTranslation(locale, Message.Format.MINI_MESSAGE, miniMessage);
    }

    public MessageBuilder withTranslation(Locale locale, Message.Format format, String translation) {
        this.translations.put(locale, format.toPrefix() + translation);
        return this;
    }

    public MessageBuilder withPlaceholders(String... placeholders) {
        for (String placeholder : placeholders) {
            this.placeholderMap.put(placeholder, new Placeholder(placeholder, Optional.empty(), Optional.empty()));
        }
        return this;
    }

    public MessageBuilder withPlaceholder(String tag) {
        return withPlaceholder(tag, null, null);
    }

    public MessageBuilder withPlaceholder(String tag, String description) {
        return withPlaceholder(tag, description, null);
    }

    public MessageBuilder withPlaceholder(String tag, String description, TagResolver defaultResolver) {
        this.placeholderMap.put(tag, new Placeholder(tag, Optional.ofNullable(description), Optional.ofNullable(defaultResolver)));
        return this;
    }

    public Message build() {
        Message message = new Message(owner, key);
        message.setComment(String.join("\n", comments));
        message.setDefaultValue(defaultValue);
        message.setDefaultTranslations(translations);
        message.setPlaceholderResolvers(placeholderMap.values().stream()
                .map(Placeholder::resolver)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet()));
        message.setPlaceholderTags(placeholderMap.values().stream()
                .collect(Collectors.toMap(Placeholder::tag, Placeholder::desc)));
        return message;
    }

    record Placeholder(String tag, Optional<String> desc, Optional<TagResolver> resolver) {
    }

}
