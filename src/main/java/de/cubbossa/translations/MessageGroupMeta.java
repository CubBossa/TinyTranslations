package de.cubbossa.translations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface MessageGroupMeta {

    String path();

    String[] comment() default {};

    String[] placeholders() default {};
}
