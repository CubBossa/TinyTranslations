package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import org.jetbrains.annotations.Contract;

public interface UnownedMessage extends Message {

    @Contract(pure = true)
    Message owner(MessageTranslator translator);

    @Contract(pure = true)
    Message owner(@KeyPattern String namespace);
}
