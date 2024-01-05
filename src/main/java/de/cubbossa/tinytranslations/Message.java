package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.annotation.PathPattern;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public interface Message extends ComponentLike, Cloneable, Comparable<Message> {


	@KeyPattern
	String getKey();

	@PathPattern
	String getNamespacedKey();

	@Nullable Translator getTranslator();

	void setTranslator(@NotNull Translator translator);


	@Override
	@NotNull Component asComponent();

	String toString(MessageFormat format);


	@Contract(pure = true)
	Message formatted(Audience audience);

	@Contract(pure = true)
	Message formatted(TagResolver... resolver);

	@Contract(pure = true)
	default Message insertString(final @NotNull String key, String value) {
		return formatted(Placeholder.unparsed(key, value));
	}

	@Contract(pure = true)
	default Message insertStringLazy(final @NotNull String key, Supplier<String> value) {
		return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
			return Tag.preProcessParsed(value.get());
		}));
	}

	@Contract(pure = true)
	default Message insertComponent(final @NotNull String key, ComponentLike value) {
		return formatted(Placeholder.component(key, value));
	}

	@Contract(pure = true)
	default Message insertComponentLazy(final @NotNull String key, Supplier<ComponentLike> value) {
		return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
			return Tag.inserting(value.get());
		}));
	}

	@Contract(pure = true)
	default Message insertNumber(final @NotNull String key, Number value) {
		return formatted(Formatter.number(key, value));
	}

	@Contract(pure = true)
	default Message insertNumberLazy(final @NotNull String key, Supplier<Number> value) {
		return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
			return Formatter.number(key, value.get()).resolve(key, argumentQueue, context);
		}));
	}

	@Contract(pure = true)
	default Message insertNumberChoice(final @NotNull String key, Number number) {
		return formatted(Formatter.choice(key, number));
	}

	@Contract(pure = true)
	default Message insertTemporal(final @NotNull String key, Temporal value) {
		return formatted(Formatter.date(key, value));
	}

	@Contract(pure = true)
	default Message insertBool(final @NotNull String key, Boolean value) {
		return formatted(Formatter.booleanChoice(key, value));
	}

	@Contract(pure = true)
	default Message insertTag(final @NotNull String key, Tag tag) {
		return formatted(TagResolver.resolver(key, tag));
	}

	@Contract(pure = true)
	default <E> Message insertList(final @NotNull String key, List<E> elements, Function<E, ComponentLike> renderer) {
		return this.insertList(key, elements, ListSection.paged(0, elements.size()), renderer);
	}

	@Contract(pure = true)
	default <E> Message insertList(final @NotNull String key, List<E> elements, ListSection section, Function<E, ComponentLike> renderer) {
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

	@Contract(pure = true)
	default <E> Message insertListLazy(final @NotNull String key, Function<ListSection, List<E>> elementSupplier, ListSection section, Function<E, ComponentLike> renderer) {
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

	@Nullable Audience getTarget();

	Collection<TagResolver> getResolvers();


	Map<Locale, String> getDictionary();

	Map<String, Optional<String>> getPlaceholderTags();

	void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

	@Nullable String getComment();

	void setComment(String comment);
}
