package de.cubbossa.tinytranslations;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

@ApiStatus.Internal
public class AdventureTranslatorAdapter implements Translator {

    private static final AdventureTranslatorAdapter INSTANCE = new AdventureTranslatorAdapter();

    public static AdventureTranslatorAdapter instance() {
        return INSTANCE;
    }

    private static final Key KEY = Key.key("tinytranslations");

    @Getter
    private Collection<MessageTranslator> translators;

    public AdventureTranslatorAdapter() {
        translators = new HashSet<>();
    }

    public void register(MessageTranslator translator) {
        this.translators.add(translator);

        if (translators.size() == 1) {
            GlobalTranslator.translator().addSource(this);
        }
    }

    public void unregister(MessageTranslator translator) {
        this.translators.remove(translator);

        if (translators.isEmpty()) {
            GlobalTranslator.translator().removeSource(this);
        }
    }

    @Override
    public @NotNull Key name() {
        return KEY;
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        if (component instanceof Message m && m.getKey().key().equals(Message.TEMPORARY_MESSAGE_KEY)) {
            for (MessageTranslator translator : translators) {
                if (translator.getPath() == null || translator.getPath().equals(m.getKey().namespace())) {
                    return translator.translate(component, locale);
                }
            }
        }

        Map<MessageTranslator, Message> matched = null;
        for (MessageTranslator translator : translators) {
            if (translator.hasAnyTranslations().equals(TriState.FALSE)) {
                continue;
            }
            Message m = translator.getMessage(component.key());
            if (m == null) {
                continue;
            }
            if (!m.dictionary().containsKey(locale)) {
                continue;
            }
            // every remaining translator has a matching message.
            // we need to find the one that is most likely the owner if the ownership is not set explicitly.
            if (matched == null) {
                matched = new HashMap<>();
            }
            matched.put(translator, m);
        }
        if (matched == null || matched.isEmpty()) {
            return null;
        }
        if (matched.size() == 1) {
            return matched.entrySet().iterator().next().getKey().translate(component, locale);
        }

        // lets score remaining translators on their equality in tag descriptions
        if (!(component instanceof Message message)) {
            // not a message, so we cannot find the most likely owner. Just let any one translate it.
            return matched.entrySet().iterator().next().getKey().translate(component, locale);
        }
        SortedSet<ScoredTranslator> scores = new TreeSet<>();
        Collection<Message.PlaceholderDescription> descs = new HashSet<>(message.placeholderDescriptions());
        matched.forEach((translator, m) -> {
            int score = 0;
            for (Message.PlaceholderDescription d : m.placeholderDescriptions()) {
                if (!descs.contains(d)) {
                    score--;
                } else {
                    score++;
                }
            }
            scores.add(new ScoredTranslator(translator, m, score));
        });
        if (!scores.isEmpty()) {
            return scores.last().translator().translate(component, locale);
        }
        return null;
    }

    private record ScoredTranslator(MessageTranslator translator, Message message, int score) implements Comparable<ScoredTranslator> {

        @Override
        public int compareTo(@NotNull AdventureTranslatorAdapter.ScoredTranslator o) {
            return Integer.compare(score, o.score);
        }
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }
}
