package de.cubbossa.translations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
public final class Message implements ComponentLike, Cloneable {

	public enum Format {
		GSON, MINI_MESSAGE, LEGACY, LEGACY_AMP, PLAIN
	}

	private final String key;
	private String defaultValue;
	private Map<Locale, String> defaultTranslations;
	private String comment;
	private Map<String, Optional<String>> placeholderTags;
	private Collection<TagResolver> placeholderResolvers;
	private final Translations translations;

	public Message(String key) {
		this(key, "No default translation present");
	}

	public Message(String key, String defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.defaultTranslations = new HashMap<>();
		this.placeholderTags = new HashMap<>();
		this.placeholderResolvers = new HashSet<>();
		this.translations = Translations.get();
	}

	public @NotNull Component asComponent() {
		return this.asComponent(translations.getAudiences().console());
	}

	public Component asComponent(TagResolver... templates) {
		return translations.translateLine(this, (Audience) null, templates);
	}

	public List<Component> asComponents(TagResolver... templates) {
		return translations.translateLines(this, (Audience) null, templates);
	}

	public Component asComponent(Player player, TagResolver... templates) {
		return translations.translateLine(this, player, templates);
	}

	public List<Component> asComponents(Player player, TagResolver... templates) {
		return translations.translateLines(this, player, templates);
	}

	public Component asComponent(CommandSender sender, TagResolver... templates) {
		return translations.translateLine(this, sender, templates);
	}

	public List<Component> asComponents(CommandSender sender, TagResolver... templates) {
		return translations.translateLines(this, sender, templates);
	}

	public Component asComponent(Audience audience, TagResolver... templates) {
		return translations.translateLine(this, audience, templates);
	}

	public List<Component> asComponents(Audience audience, TagResolver... templates) {
		return translations.translateLines(this, audience, templates);
	}

	public Message format(TagResolver resolver) {
		this.placeholderResolvers.add(resolver);
		return this;
	}

	public Message formatted(TagResolver resolver) {
		return this.clone().format(resolver);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Message message = (Message) o;
		return key.equals(message.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public Message clone() {
		Message message = new Message(key, defaultValue);
		message.setPlaceholderTags(placeholderTags);
		message.setPlaceholderResolvers(placeholderResolvers);
		message.setComment(comment);
		return message;
	}
}
