package de.cubbossa.translations;

import net.kyori.adventure.text.format.Style;

public interface StyleSerializer {

    String serialize(Style style);

    Style deserialize(String string);
}
