package de.cubbossa.translations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageFile {

	String languageString() default TranslationHandler.HEADER_VALUE_UNDEFINED;

	String author() default TranslationHandler.HEADER_VALUE_UNDEFINED;

	String version() default TranslationHandler.HEADER_VALUE_UNDEFINED;

	String[] header() default """
			MESSAGE FILE
			----------------------------------------
			Styling:
			To style your messages, you must use the MiniMessage formatting.
			It is a tag based styling method like html or xml and allows you to
			create hover messages and click actions within your language file!
			
			Example: <green>I am a green Text. <hover:show_text:"Hello there">Hover me</hover><green>
			            
			All information on how to use the formatting can be found here:
			https://docs.adventure.kyori.net/minimessage/format.html
			            
			Additionally to the default MiniMessage, you have the following possibilities:
			<prefix> will always be replaced with the prefix message. (general.prefix)
			<msg:[message-key]> or <message:[message-key]> will replaced with another already interpreted messages
			    Using <msg:color_red> and referring to a translation `color_red: <#ff0000>` will not work.
			    The color will be interpreted as component before being inserted.
			<col:[message-key]> will insert a message as raw MiniMessage.
			    So instead of <msg:color_red>, you can use
			    <col:color_red> to insert the value of color_red ("<#ff0000>") as simple string.
			    With this functionality, you can add a style table at the top of your language file and change colors for the
			    whole file easily.
			----------------------------------------
			""";
}
