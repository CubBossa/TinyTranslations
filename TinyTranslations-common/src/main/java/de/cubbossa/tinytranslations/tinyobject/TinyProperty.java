package de.cubbossa.tinytranslations.tinyobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that describes how a field of an object should be resolved in the {@link TinyObjectResolver}s resolving
 * process. The class owning the field must be annotated with {@link TinyObject}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TinyProperty {
    String name() default "$";
}
