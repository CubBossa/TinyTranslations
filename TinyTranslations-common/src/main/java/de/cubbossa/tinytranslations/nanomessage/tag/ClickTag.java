/**
 * This file is a modified file.
 * The original file is part of adventure, licensed under the MIT License.
 * <p>
 * Copyright (c) 2017-2023 KyoriPowered
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
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
	private static final NanoMessageCompiler PREPROCESSOR = new NanoMessageCompiler();
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
		value = PREPROCESSOR.compile(value);
		Component c = ctx.deserialize(value);
		value = PLAIN.serialize(c);

		return Tag.styling(ClickEvent.clickEvent(action, value));
	}
}
