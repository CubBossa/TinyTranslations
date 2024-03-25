package de.cubbossa.tinytranslations.storage;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface Commented<T> {

    /**
     * @return A comment string for this object or null if no comment set.
     */
    @Nullable String comment();

    /**
     * Set the comment for this object. Use '\n' to create multiline comments.
     *
     * @param comment The comment string or null to remove any existing comment.
     * @return A clone of this object with the new comment set.
     */
    @Contract(pure = true)
    T comment(@Nullable String comment);
}
