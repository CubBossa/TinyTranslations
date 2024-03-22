package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.tag.ObjectTag;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.internal.parser.node.TextNode;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

/**
 * Adds the option to add {@link TagResolver}s to an object. The object might use the resolvers to resolve a string.
 * Most methods are pre-implemented and turn primitives into their most fitting {@link TagResolver} representations.
 *
 * @param <ReturnT> The type of the implementing class to allow a builder-like pattern.
 */
public interface Formattable<ReturnT extends Formattable<ReturnT>> {

    /**
     * All placeholders that are being introduced by using {@link this#insertList(String, Collection)},
     * {@link this#insertList(String, Collection, ListSection)} or {@link this#insertList(String, Collection, Consumer)}
     */
    String[] LIST_PLACEHOLDERS = {
            "has-pages", "page", "pages", "next-page", "prev-page", "offset", "range"
    };
    /**
     * All placeholders that are being introduced by using {@link this#insertList(String, Function, ListSection)}
     */
    String[] DYNAMIC_LIST_PLACEHOLDERS = {
            "page", "next-page", "prev-page", "offset", "range"
    };

    /**
     * @return a copy of the set resolvers on this object.
     */
    Collection<TagResolver> getResolvers();

    /**
     * Returns an instance with the resolver set.
     * For the current implementations, {@link Message} returns a new instance while {@link MessageTranslator} modifies
     * its values.
     *
     * @param resolver An array of {@link TagResolver}s to add.
     * @return this object or a new object if the implementation is pure
     */
    ReturnT formatted(TagResolver... resolver);

    /**
     * Parses a MiniMessage/NanoMessage string and inserts it as component
     *
     * @param key         The tag key
     * @param minimessage The MiniMessage/NanoMessage value to parse
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertParsed(final @NotNull String key, String minimessage) {
        return formatted(Placeholder.parsed(key, minimessage));
    }

    /**
     * Replaces <pre><[tag]></pre> with the raw string value, even if it contains MiniMessage formatting.
     *
     * @param key   The tag key
     * @param value The string value to insert
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertString(final @NotNull String key, String value) {
        return formatted(Placeholder.unparsed(key, value));
    }

    /**
     * Replaces <pre><[tag]></pre> with the raw string value, even if it contains MiniMessage formatting.
     * This placeholder is lazy and the according supplier is only being called when the tag is actually being used.
     *
     * @param key   The tag key
     * @param value A supplier producing the string value to insert
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertString(final @NotNull String key, Supplier<String> value) {
        return formatted(new PlaceholderTag(key, () -> Component.text(value.get())));
    }

    /**
     * Replaces <pre><[tag]></pre> with a {@link ComponentLike}.
     *
     * @param key   The tag key
     * @param value The {@link ComponentLike} to insert.
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertComponent(final @NotNull String key, ComponentLike value) {
        return formatted(Placeholder.component(key, value));
    }

    /**
     * Replaces <pre><[tag]></pre> with the ComponentLike.
     * This placeholder is lazy and the according supplier is only being called when the tag is actually being used.
     *
     * @param key   The tag key
     * @param value The {@link ComponentLike} to insert.
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertComponent(final @NotNull String key, Supplier<ComponentLike> value) {
        return formatted(new PlaceholderTag(key, value));
    }

    /**
     * Inserts a Number tag.
     *
     * @param key   The tag key
     * @param value The number value to insert
     * @return this object or a new object if the implementation is pure
     * @see <a href="https://docs.advntr.dev/minimessage/dynamic-replacements.html#insert-a-number">MiniMessage Docs</a> for a full
     * guide about how to use number tags.
     */
    default ReturnT insertNumber(final @NotNull String key, Number value) {
        return formatted(Formatter.number(key, value));
    }

    /**
     * Inserts a Number tag.
     *
     * @param key   The tag key
     * @param value
     * @return this object or a new object if the implementation is pure
     * @see <a href="https://docs.advntr.dev/minimessage/dynamic-replacements.html#insert-a-number">MiniMessage Docs</a> for a full
     * guide about how to use number tags.
     * This placeholder is lazy and the according supplier is only being called when the tag is actually being used.
     */
    default ReturnT insertNumber(final @NotNull String key, Supplier<Number> value) {
        return formatted(TagResolver.resolver(key, new BiFunction<>() {

            Number cached = null;

            @Override
            public Tag apply(ArgumentQueue argumentQueue, Context context) {
                if (cached == null) {
                    cached = value.get();
                }
                return Formatter.number(key, cached).resolve(key, argumentQueue, context);
            }
        }));
    }

    /**
     * @param key   The tag key
     * @param value
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertTemporal(final @NotNull String key, Temporal value) {
        return formatted(Formatter.date(key, value));
    }

    /**
     * Inserts a boolean as a simple string.
     *
     * @param key   The tag key
     * @param value A boolean value to insert
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertBool(final @NotNull String key, Boolean value) {
        return formatted(Placeholder.parsed(key, value.toString()));
    }

    /**
     * Short form of {@link TagResolver#resolver(String, Tag)}
     *
     * @param key The tag key
     * @param tag A tag implementation to insert
     * @return this object or a new object if the implementation is pure
     */
    default ReturnT insertTag(final @NotNull String key, Tag tag) {
        return formatted(TagResolver.resolver(key, tag));
    }

    /**
     * Inserts any object as {@link TagResolver}.
     * Generally, objects will be inserted via their according methods if present (insertNumber, ...)
     * or as <pre>Component.text(object.toString())</pre>
     * <br><br>
     * To define a specific way for turning objects into components, you want to use
     * {@link TinyObjectResolver}.<br>
     * In the general TinyTranslations translation process, a series of default {@link TinyObjectResolver}s
     * is being applied. Objects contained in {@link Collection}s are rendered via {@link #insertObject(String, Object)} again.
     * <br><br>
     *
     * @param key The tag key
     * @param obj Any object to insert
     * @param <T> The object type
     * @return this object or a new object if the implementation is pure
     */
    default <T> ReturnT insertObject(final @NotNull String key, T obj) {
        return insertObject(key, obj, Collections.emptyList());
    }

    /**
     * Inserts any object as {@link TagResolver}.
     * Generally, objects will be inserted via their according methods if present (insertNumber, ...)
     * or as <pre>Component.text(object.toString())</pre>
     * <br><br>
     * To define a specific way for turning objects into components, you want to use
     * {@link TinyObjectResolver}.<br>
     * In the general TinyTranslations translation process, a series of default {@link TinyObjectResolver}s
     * is being applied. Objects contained in {@link Collection}s are rendered via {@link #insertObject(String, Object)} again.
     * <br><br>
     *
     * @param key                 The tag key
     * @param obj                 Any object to insert
     * @param additionalResolvers a collection of additional {@link TinyObjectResolver} to apply in the parsing process.
     * @param <T>                 The object type
     * @return this object or a new object if the implementation is pure
     */
    default <T> ReturnT insertObject(final @NotNull String key, T obj, Collection<TinyObjectResolver> additionalResolvers) {
        Collection<TinyObjectResolver> r = new LinkedList<>(additionalResolvers);
        r.addAll(getObjectResolversInScope());
        return formatted(ObjectTag.resolver(key, obj, r));
    }

    /**
     * @return All {@link TinyObjectResolver}s of the current context. If this interface is being implemented by a {@link MessageTranslator},
     * this might be all resolvers of this translator instance and its ancestors.
     */
    Collection<TinyObjectResolver> getObjectResolversInScope();

    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0")
    default <E> ReturnT insertList(final @NotNull String key, List<E> elements, Function<E, ComponentLike> renderer) {
        return this.insertList(key, elements, ListSection.paged(0, elements.size()), renderer);
    }

    /**
     * Inserts a list of objects as tag.
     * <br><br>
     * The tag slot represents the way of how each element should be rendered, while &#60el> represents the object
     * within slot.
     * <br><br>
     * Objects contained in the collection are resolved via {@link TinyObjectResolver}s,
     * see {@link #insertObject(String, Object)}
     * <br><br>
     * Example:
     * <pre>Java: insertList("online", Bukkit.getOnlinePlayers())</pre>
     * <pre>NanoMessage: &#online>\n- &#60el>&#60/online></pre>
     * Will render all players in new lines with a "- " prefix.
     * Players will be rendered with their display names, since {@link TinyTranslations} registers a {@link TinyObjectResolver}
     * for the org.bukkit.Player class.
     *
     * @param key      The tag key for the list
     * @param elements A list of objects to render as list
     * @param <E>      The generic type of the elements
     * @return this object or a new object if the implementation is pure
     */
    default <E> ReturnT insertList(final @NotNull String key, Collection<E> elements) {
        return insertList(key, elements, ListSection.paged(0, elements.size()));
    }

    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0")
    default <E> ReturnT insertList(final @NotNull String key, List<E> elements, ListSection section, Function<E, ComponentLike> renderer) {
        return formatted(
                Formatter.choice("has-pages", section.getMaxPages(elements.size())),
                Formatter.number("page", section.getPage() + 1),
                Formatter.number("pages", section.getMaxPages(elements.size())),
                Formatter.number("next-page", Math.min(section.getMaxPages(elements.size()), section.getPage() + 2)),
                Formatter.number("prev-page", Math.max(1, section.getPage())),
                Formatter.number("offset", section.getOffset()),
                Formatter.number("range", section.getRange()),
                TagResolver.resolver(key, (argumentQueue, context) -> {
                    String separator = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
                    Component separatorParsed = separator == null ? text(", ") : context.deserialize(separator);

                    List<E> sublist = section.apply(elements);
                    Component content = Component.join(JoinConfiguration.separator(separatorParsed), sublist.stream()
                            .map(renderer)
                            .collect(Collectors.toList())
                    );
                    return Tag.selfClosingInserting(content);
                }));
    }

    /**
     * Inserts a list of objects as tag.
     * <br><br>
     * The tag slot represents the way of how each element should be rendered, while &#60el> represents the object
     * within slot.
     * <br><br>
     * Objects contained in the collection are resolved via {@link TinyObjectResolver}s,
     * see {@link #insertObject(String, Object)}
     * <br><br>
     * Example:
     * <pre>Java: insertList("online", Bukkit.getOnlinePlayers())</pre>
     * <pre>NanoMessage: &#online>\n- &#60el>&#60/online></pre>
     * Will render all players in new lines with a "- " prefix.
     * Players will be rendered with their display names, since {@link TinyTranslations} registers a {@link TinyObjectResolver}
     * for the org.bukkit.Player class.
     *
     * @param key      The tag key for the list
     * @param elements A list of objects to render as list
     * @param section  Allows to only render a segment of the provided elements, most likely used for pagination.
     * @param <E>      The generic type of the elements
     * @return this object or a new object if the implementation is pure
     */
    default <E> ReturnT insertList(final @NotNull String key, Collection<E> elements, ListSection section) {
        return formatted(
                Formatter.choice("has-pages", section.getMaxPages(elements.size())),
                Formatter.number("page", section.getPage() + 1),
                Formatter.number("pages", section.getMaxPages(elements.size())),
                Formatter.number("next-page", Math.min(section.getMaxPages(elements.size()), section.getPage() + 2)),
                Formatter.number("prev-page", Math.max(1, section.getPage())),
                Formatter.number("offset", section.getOffset()),
                Formatter.number("range", section.getRange()),
                TagResolver.resolver(key, (argumentQueue, context) -> {
                    String separator = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
                    Component separatorParsed = separator == null ? text(", ") : context.deserialize(separator);

                    List<E> sublist = section.apply(formList(elements));
                    AtomicInteger index = new AtomicInteger(section.getOffset());
                    AtomicReference<String> format = new AtomicReference<>("");

                    return new Modifying() {
                        @Override
                        public void visit(@NotNull Node current, int depth) {
                            if (depth != 0) return;
                            if (current.children().isEmpty()) return;
                            format.set(serializeChildren(current));
                        }

                        @Override
                        public Component apply(@NotNull Component current, int depth) {
                            if (depth != 0) return Component.empty();
                            return Component.join(JoinConfiguration.separator(separatorParsed), sublist.stream()
                                    .map(e -> Message.contextual(format.get())
                                            .insertObject("element", e)
                                            .insertObject("el", e)
                                            .insertNumber("index", index.incrementAndGet())
                                    )
                                    .toList());
                        }
                    };
                }));
    }

    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0")
    default <E> ReturnT insertList(final @NotNull String key, Function<ListSection, List<E>> elementSupplier, ListSection section, Function<E, ComponentLike> renderer) {
        return formatted(
                Formatter.number("page", section.getPage() + 1),
                Formatter.number("next-page", section.getPage() + 2),
                Formatter.number("prev-page", Math.max(0, section.getPage())),
                Formatter.number("offset", section.getOffset()),
                Formatter.number("range", section.getRange()),
                TagResolver.resolver(key, (argumentQueue, context) -> {
                    String separator = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
                    Component separatorParsed = separator == null ? text(", ") : context.deserialize(separator);

                    AtomicInteger startIndex = new AtomicInteger(section.getOffset());
                    List<E> sublist = elementSupplier.apply(section);
                    Component content = Component.join(JoinConfiguration.separator(separatorParsed), sublist.stream()
                            .map(renderer)
                            .map(componentLike -> {
                                if (componentLike instanceof Message m) {
                                    return m.insertNumber("index", startIndex.incrementAndGet());
                                }
                                return componentLike;
                            })
                            .collect(Collectors.toList())
                    );
                    return Tag.selfClosingInserting(content);
                }));
    }

    /**
     * Inserts a list of objects as tag.
     * <br><br>
     * The tag slot represents the way of how each element should be rendered, while &#60el> represents the object
     * within slot.
     * <br><br>
     * Objects contained in the collection are resolved via {@link TinyObjectResolver}s,
     * see {@link #insertObject(String, Object)}
     * <br><br>
     * Example:
     * <pre>Java: insertList("online", Bukkit.getOnlinePlayers())</pre>
     * <pre>NanoMessage: &#online>\n- &#60el>&#60/online></pre>
     * Will render all players in new lines with a "- " prefix.
     * Players will be rendered with their display names, since {@link TinyTranslations} registers a {@link TinyObjectResolver}
     * for the org.bukkit.Player class.
     *
     * @param key             The tag key for the list
     * @param elementSupplier A supplier that creates a collection for a certain {@link ListSection}
     * @param <E>             The generic type of the elements
     * @return this object or a new object if the implementation is pure
     */
    default <E> ReturnT insertList(final @NotNull String key, Function<ListSection, Collection<E>> elementSupplier, ListSection section) {
        return formatted(
                Formatter.number("page", section.getPage() + 1),
                Formatter.number("next-page", section.getPage() + 2),
                Formatter.number("prev-page", Math.max(0, section.getPage())),
                Formatter.number("offset", section.getOffset()),
                Formatter.number("range", section.getRange()),
                TagResolver.resolver(key, (argumentQueue, context) -> {
                    String separator = argumentQueue.hasNext() ? argumentQueue.pop().value() : null;
                    Component separatorParsed = separator == null ? text(", ") : context.deserialize(separator);

                    AtomicInteger startIndex = new AtomicInteger(section.getOffset());
                    List<E> sublist = formList(elementSupplier.apply(section));
                    AtomicReference<String> format = new AtomicReference<>("");

                    return new Modifying() {
                        @Override
                        public void visit(@NotNull Node current, int depth) {
                            if (depth != 0) return;
                            if (current.children().isEmpty()) return;
                            format.set(serializeChildren(current));
                        }

                        @Override
                        public Component apply(@NotNull Component current, int depth) {
                            if (depth != 0) return Component.empty();
                            Formattable<?> outer = Formattable.this;
                            return Component.join(JoinConfiguration.separator(separatorParsed), sublist.stream()
                                    .map(e -> Message.contextual(format.get())
                                            .insertObject("element", e, outer.getObjectResolversInScope())
                                            .insertObject("el", e, outer.getObjectResolversInScope())
                                            .insertNumber("index", startIndex.incrementAndGet())
                                    )
                                    .toList());
                        }
                    };
                })
        );
    }

    private static <E> List<E> formList(Collection<E> collection) {
        if (collection instanceof List<E> list) {
            return list;
        }
        if (collection instanceof SortedSet<E> set) {
            return new ArrayList<>(set);
        }
        List<E> val = new ArrayList<>(collection);
        val.sort(Comparator.comparing(Object::toString));
        return val;
    }

    private static String serializeChildren(Node node) {
        return node.children().stream().map(Formattable::serialize).collect(Collectors.joining(""));
    }

    private static String serialize(Node node) {
        StringBuilder s = new StringBuilder();
        if (node instanceof TagNode t) {
            s.append("<").append(t.name());
            for (TagPart part : t.parts().subList(1, t.parts().size())) {
                s.append(":'").append(part.value()).append("'");
            }
            s.append(">");
        } else if (node instanceof TextNode t) {
            s.append(t.value());
        }
        for (Node child : node.children()) {
            s.append(serialize(child));
        }
        return s.toString();
    }

    class PlaceholderTag implements TagResolver {

        private final String key;
        private final Supplier<ComponentLike> supplier;
        private Tag tag;

        public PlaceholderTag(String key, ComponentLike value) {
            this.key = key;
            this.supplier = null;
            this.tag = Tag.inserting(value);
        }

        public PlaceholderTag(String key, Supplier<ComponentLike> value) {
            this.key = key;
            this.supplier = value;
        }

        @Override
        public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
            if (tag == null) {
                tag = Tag.inserting(supplier.get());
            }
            return tag;
        }

        @Override
        public boolean has(@NotNull String name) {
            return name.equals(key);
        }
    }
}
