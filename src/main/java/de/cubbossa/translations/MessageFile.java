package de.cubbossa.translations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageFile {

    String languageString() default "en_US";

    String author() default "unknown";

    String version() default "1.0";
}
