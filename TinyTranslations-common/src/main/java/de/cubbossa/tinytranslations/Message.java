package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.annotation.PathPattern;
import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
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

public interface Message extends ComponentLike, Cloneable, Comparable<Message>, Formattable<Message> {


	@KeyPattern
	String getKey();

	@PathPattern
	String getNamespacedKey();

	@Nullable Translator getTranslator();

	void setTranslator(@NotNull Translator translator);


	@Override
	@NotNull Component asComponent();

	String toString(MessageFormat format);


	@Nullable Audience getTarget();

	@Contract(pure = true)
	Message formatted(Audience audience);


	Map<Locale, String> getDictionary();

	Map<String, Optional<String>> getPlaceholderTags();

	void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

	@Nullable String getComment();

	void setComment(String comment);
}
