package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.tag.ObjectTag;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public interface Formattable<ReturnT> {

	Collection<TagResolver> getResolvers();

	ReturnT formatted(TagResolver... resolver);


	default ReturnT insertString(final @NotNull String key, String value) {
		return formatted(Placeholder.unparsed(key, value));
	}

	default ReturnT insertString(final @NotNull String key, Supplier<String> value) {
		return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
			return Tag.preProcessParsed(value.get());
		}));
	}

	default ReturnT insertComponent(final @NotNull String key, ComponentLike value) {
		return formatted(Placeholder.component(key, value));
	}

	default ReturnT insertComponent(final @NotNull String key, Supplier<ComponentLike> value) {
		return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
			return Tag.inserting(value.get());
		}));
	}

	default ReturnT insertNumber(final @NotNull String key, Number value) {
		return formatted(Formatter.number(key, value));
	}

	default ReturnT insertNumber(final @NotNull String key, Supplier<Number> value) {
		return formatted(TagResolver.resolver(key, (argumentQueue, context) -> {
			return Formatter.number(key, value.get()).resolve(key, argumentQueue, context);
		}));
	}

	default ReturnT insertTemporal(final @NotNull String key, Temporal value) {
		return formatted(Formatter.date(key, value));
	}

	default ReturnT insertBool(final @NotNull String key, Boolean value) {
		return formatted(Placeholder.parsed(key, value.toString()));
	}

	default ReturnT insertTag(final @NotNull String key, Tag tag) {
		return formatted(TagResolver.resolver(key, tag));
	}

	default <T> ReturnT insertObject(final @NotNull String key, T obj) {
		return formatted(ObjectTag.resolver(key, obj));

	}

	default <E> ReturnT insertList(final @NotNull String key, List<E> elements, Function<E, ComponentLike> renderer) {
		return this.insertList(key, elements, ListSection.paged(0, elements.size()), renderer);
	}

	default <E> ReturnT insertList(final @NotNull String key, List<E> elements) {
		return insertList(key, elements, ListSection.paged(0, elements.size()));
	}

	default <E> ReturnT insertList(final @NotNull String key, List<E> elements, ListSection section) {
		Function<E, ComponentLike> fun = e -> {
			var c = TinyTranslations.NM.getObjectTypeResolverMap().resolve(e, "");
			if (c instanceof ComponentLike cl) {
				return cl;
			}
			return Component.text(c.toString());
		};
		return this.insertList(key, elements, section, fun);
	}

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
}
