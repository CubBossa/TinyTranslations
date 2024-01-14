package de.cubbossa.tinytranslations.nanomessage.tag;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.awt.*;

public class DarkerTag extends ModifyColorTag {
	private static final String KEY = "darker";

	public static final TagResolver RESOLVER = modifyColor(KEY, Color::darker);
}
