package de.cubbossa.translations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public class Message implements ComponentLike {

	@Getter
	private final String key;

	public @NotNull Component asComponent() {
		return this.asComponent(TranslationHandler.getInstance().getAudiences().console());
	}

	public Component asComponent(TagResolver... templates) {
		return TranslationHandler.getInstance().translateLine(this, (Audience) null, templates);
	}

	public List<Component> asComponents(TagResolver... templates) {
		return TranslationHandler.getInstance().translateLines(this, (Audience) null, templates);
	}

	public Component asComponent(Player player, TagResolver... templates) {
		return TranslationHandler.getInstance().translateLine(this, player, templates);
	}

	public List<Component> asComponents(Player player, TagResolver... templates) {
		return TranslationHandler.getInstance().translateLines(this, player, templates);
	}

	public Component asComponent(Audience audience, TagResolver... templates) {
		return TranslationHandler.getInstance().translateLine(this, audience, templates);
	}

	public List<Component> asComponents(Audience audience, TagResolver... templates) {
		return TranslationHandler.getInstance().translateLines(this, audience, templates);
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
}
