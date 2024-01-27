package de.cubbossa.tinytranslations.nanomessage.tag;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.awt.*;

public class BrighterTag extends ModifyColorTag {
    public static final String KEY = "brighter";

    public static TagResolver RESOLVER = modifyColor(KEY, Color::brighter);
}
