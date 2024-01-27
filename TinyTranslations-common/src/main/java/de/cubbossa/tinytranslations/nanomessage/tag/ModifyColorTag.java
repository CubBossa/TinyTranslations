package de.cubbossa.tinytranslations.nanomessage.tag;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.awt.*;
import java.util.Collections;
import java.util.function.Function;

public abstract class ModifyColorTag {

    protected static TagResolver modifyColor(String key, Function<Color, Color> modifier) {
        return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (current, depth) -> {
            if (current.color() == null) {
                return current.children(Collections.emptyList());
            }
            Color c = modifier.apply(new Color(current.color().value()));
            return current
                    .children(Collections.emptyList())
                    .color(TextColor.color(c.getRGB()));
        });
    }
}
