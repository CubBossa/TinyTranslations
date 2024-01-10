package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.Set;

public class MessageFormat {

    private static final MiniMessage S_MM = MiniMessage.miniMessage();
    private static final GsonComponentSerializer S_GSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer S_LEGACY = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer S_LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer S_PLAIN = PlainTextComponentSerializer.plainText();

    private static final TagResolver LEGACY_RESOLVER = TagResolver.resolver("legacy", (argumentQueue, context) -> {
        if (!argumentQueue.hasNext()) {
            return Tag.preProcessParsed("");
        }
        char symbol = '&';
        String content = argumentQueue.pop().value();

        if (argumentQueue.hasNext()) {
            symbol = content.toCharArray()[0];
            content = argumentQueue.pop().value();
        }
        ComponentSerializer<Component, ? extends Component, String> translator = null;
        if (symbol == '&') {
            translator = S_LEGACY_AMP;
        } else if (symbol == '§') {
            translator = S_LEGACY;
        }
        if (translator == null) {
            return Tag.preProcessParsed("");
        }
        return Tag.inserting(translator.deserialize(content));
    });

    public static final MessageFormat MINI_MESSAGE = new MessageFormat("minimessage", S_MM) {
        @Override
        public String wrap(String content) {
            return content;
        }
    };
    public static final MessageFormat NBT = new MessageFormat(List.of("nbt", "json", "gson"), S_GSON);
    public static final MessageFormat LEGACY_PARAGRAPH = new MessageFormat("legacy", S_LEGACY, LEGACY_RESOLVER) {
        @Override
        public String wrap(String content) {
            return "<legacy:'§'>" + content + "</legacy>";
        }
    };
    public static final MessageFormat LEGACY_AMPERSAND = new MessageFormat("legacy", S_LEGACY_AMP, LEGACY_RESOLVER) {
        @Override
        public String wrap(String content) {
            return "<legacy:'&'>" + content + "</legacy>";
        }
    };
    public static final MessageFormat PLAIN = new MessageFormat(List.of("plain", "pre"), S_PLAIN);
    private static final MessageFormat[] VALUES = { MINI_MESSAGE, NBT, LEGACY_PARAGRAPH, LEGACY_AMPERSAND, PLAIN };


    public static MessageFormat[] values() {
        return VALUES;
    }

    final List<String> tag;
    final TagResolver resolver;
    final ComponentSerializer<Component, ? extends Component, String> translator;

    MessageFormat(String tag, ComponentSerializer<Component, ? extends Component, String> translator) {
        this(List.of(tag), translator);
    }

    MessageFormat(List<String> tag, ComponentSerializer<Component, ? extends Component, String> translator) {
        this(tag, translator, TagResolver.resolver(Set.copyOf(tag), (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) {
                return Tag.preProcessParsed("");
            }
            String content = argumentQueue.pop().value();
            return Tag.inserting(translator.deserialize(content));
        }));
    }

    MessageFormat(String tag, ComponentSerializer<Component, ? extends Component, String> translator, TagResolver resolver) {
        this(List.of(tag), translator, resolver);
    }

    MessageFormat(List<String> tag, ComponentSerializer<Component, ? extends Component, String> translator, TagResolver resolver) {
        this.tag = tag;
        this.translator = translator;
        this.resolver = resolver;
    }

    public String wrap(String content) {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

    public String format(Component component) {
        return translator.serialize(component);
    }

    public TagResolver getTagResolver() {
        return resolver;
    }
}
