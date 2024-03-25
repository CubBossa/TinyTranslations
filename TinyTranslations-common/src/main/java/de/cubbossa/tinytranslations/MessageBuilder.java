package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessageBuilder {

    private final String key;
    private final Map<Locale, String> translations;
    private final List<String> comments;
    private final Collection<Message.PlaceholderDescription> placeholderDescriptions;
    private final Collection<TagResolver> defaultResolvers;

    public MessageBuilder(@KeyPattern String key) {
        this.key = key;
        this.comments = new ArrayList<>();
        this.translations = new HashMap<>();
        this.placeholderDescriptions = new LinkedList<>();
        this.defaultResolvers = new LinkedList<>();
    }

    public MessageBuilder withComment(String line) {
        this.comments.add(line);
        return this;
    }

    public MessageBuilder withDefault(@Language("NanoMessage") String defaultValue) {
        this.translations.put(TinyTranslations.FALLBACK_DEFAULT_LOCALE, defaultValue);
        return this;
    }

    public MessageBuilder withDefault(Message otherToEmbed) {
        this.translations.put(TinyTranslations.FALLBACK_DEFAULT_LOCALE, "{msg:" + otherToEmbed.key() + "}");
        return this;
    }

    public MessageBuilder withTranslation(Locale locale, @Language("NanoMessage") String translation) {
        return this.withTranslation(locale, MessageEncoding.MINI_MESSAGE, translation);
    }

    public MessageBuilder withTranslation(Locale locale, MessageEncoding format, String translation) {
        this.translations.put(locale, format.wrap(translation));
        return this;
    }

    public MessageBuilder withPlaceholders(String... placeholders) {
        for (String placeholder : placeholders) {
            this.withPlaceholder(placeholder, null);
        }
        return this;
    }

    public MessageBuilder withPlaceholder(String tag) {
        return withPlaceholder(tag, null);
    }

    public MessageBuilder withPlaceholder(String tag, String description) {
        this.placeholderDescriptions.add(new Message.PlaceholderDescription(new String[]{tag}, description, Object.class));
        return this;
    }

    public MessageBuilder withPlaceholder(String tag, String description, @NotNull TagResolver defaultResolver) {
        defaultResolvers.add(defaultResolver);
        return withPlaceholder(tag, description);
    }

    public Message build() {
        return new UnownedMessageImpl(key)
                .comment(comments.isEmpty() ? null : String.join("\n", comments))
                .dictionary(translations)
                .placeholderDescriptions(placeholderDescriptions)
                .formatted(defaultResolvers.toArray(TagResolver[]::new));
    }
}
