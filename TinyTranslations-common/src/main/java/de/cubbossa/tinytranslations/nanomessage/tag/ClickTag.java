package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.nanomessage.TranslationsPreprocessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.internal.serializer.QuotingOverride;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

public class ClickTag {
	private static final TranslationsPreprocessor PREPROCESSOR = new TranslationsPreprocessor();
	private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
	private static final String NAME = "click";

	public static final TagResolver RESOLVER = SerializableResolver.claimingStyle(
			NAME,
			ClickTag::tag,
			StyleClaim.claim(NAME, Style::clickEvent, (event, emitter) -> {
				emitter.tag(NAME)
						.argument(ClickEvent.Action.NAMES.key(event.action()))
						.argument(event.value(), QuotingOverride.QUOTED);
			})
			);

	static Tag tag(final ArgumentQueue queue, final Context ctx) {
		final String actionName = queue.popOr(() -> "A click tag requires an action of one of " + ClickEvent.Action.NAMES.keys()).lowerValue();
		final ClickEvent.@Nullable Action action = ClickEvent.Action.NAMES.value(actionName);
		if (action == null) {
			throw ctx.newException("Unknown click event action '" + actionName + "'", queue);
		}

		String value = queue.popOr("Click event actions require a value").value();
		value = PREPROCESSOR.apply(value);
		Component c = ctx.deserialize(value);
		value = PLAIN.serialize(c);

		return Tag.styling(ClickEvent.clickEvent(action, value));
	}
}
