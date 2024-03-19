package de.cubbossa.tinytranslations.tinyobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a field of an object as the default mapping within the {@link TinyObjectTagResolver}s resolving process.
 * The class holding the annotated field must be annotated with {@link TinyObject} for this annotation to work.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TinyDefault {
}
