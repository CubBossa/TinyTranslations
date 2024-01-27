package de.cubbossa.tinytranslations.storage;

import de.cubbossa.tinytranslations.MessageStyle;
import net.kyori.adventure.text.format.Style;

import java.util.Map;

/**
 * Stores {@link Style}s in a Map format, where the key represents a MiniMessage tag and the value the according style
 * that the tag will render.
 */
public interface StyleStorage {

    /**
     * Writes a map of styles into the storage.
     * Values that are already part of the storage must not be removed or modified.
     * Only non-existing values of the input map will therefore be added to this storage.
     *
     * @param styles A map of styles with their tag as key.
     */
    void writeStyles(Map<String, MessageStyle> styles);

    /**
     * Loads a map of styles with their MiniMessage tag as key.
     *
     * @return The loaded map.
     */
    Map<String, MessageStyle> loadStyles();
}
