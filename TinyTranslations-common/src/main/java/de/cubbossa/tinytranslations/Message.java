package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import de.cubbossa.tinytranslations.annotation.PathPattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translatable;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kyori.adventure.text.Component.text;

public interface Message extends ComponentLike, Cloneable, Comparable<Message>, Formattable<Message>, Translatable, TranslatableComponent {


	@KeyPattern
	String getKey();

	@Override
	@NotNull Component asComponent();

	String toString(MessageFormat format);

	String toString(MessageFormat format, Locale locale);


	Map<Locale, String> getDictionary();

	Map<String, Optional<String>> getPlaceholderTags();

	void setPlaceholderTags(Map<String, Optional<String>> placeholderTags);

	@Nullable String getComment();

	void setComment(String comment);
}
