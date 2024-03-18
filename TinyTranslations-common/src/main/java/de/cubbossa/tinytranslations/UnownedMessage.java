package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.annotation.KeyPattern;
import org.jetbrains.annotations.Contract;

/**
 * Extends the Message class. Unowned Messages explicitly have no {@link MessageTranslator} set as their owner.
 * Storages might want to return instances of UnownedMessages, since storages have no reference to the
 * {@link MessageTranslator}, they might not be able to set the owner.
 */
public interface UnownedMessage extends Message {

    /**
     * Creates a new {@link Message} instance that is identical but has the provided owner set.
     * @param translator the {@link MessageTranslator} to be the owner of the new message
     * @return a copy of this {@link Message} instance with the given owner set.
     */
    @Contract(pure = true)
    Message owner(MessageTranslator translator);

    /**
     * Creates a new {@link Message} instance that is identical but has the provided owner set.
     * @param namespace the key of the {@link MessageTranslator} to be the owner of the new message
     * @return a copy of this {@link Message} instance with the given owner set.
     */
    @Contract(pure = true)
    Message owner(@KeyPattern String namespace);
}
