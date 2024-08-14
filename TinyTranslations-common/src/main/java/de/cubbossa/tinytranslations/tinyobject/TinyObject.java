package de.cubbossa.tinytranslations.tinyobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to declare an object as resolvable for {@link TinyObjectResolver}s.
 * This means, that resolvers look for {@link TinyProperty} and {@link TinyDefault} annotations
 * to produce mappings within the resolving process.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TinyObject {
}
