# Translations

A translation framework to easily translate chat messages and GUI ItemStacks.

---

### Content

- Maven
- How to use

---

### Maven

## How to use

### Setup

First, you have to initialize the TranslationHandler.

```java
// Use your plugin main class as parameter.
TranslationHandler th=new TranslationHandler(this);
// Set fallback language
		th.setFallbackLanguage("en_US");
// Choose if player client locales should be used
		th.setUseClientLanguage(true);
// Save resources from your jar if needed
// Should not be necessary when API used with Annotations
		th.saveResources(Locale.US);
// Load all languages from [PLUGIN_DIR]/lang/
		th.loadLanguages();
```

The fallback language will be used whenever a user requests a translation of a language that does not exist.

### Messages

To send a message or customize a GUI, use the Message class.

```Java
public static final Message ERR_NO_PLAYER=new Message("error.must_be_player");
```

Now you can use

```Java
TranslationHandler.getInstance().sendMessage(Messages.ERR_NO_PLAYER, player);
```

You can also input a `FormattedMessage` object, which contains a message key but also an array of TagResolvers.
TagResolvers are the way to go if you want to use custom placeholders like
`<playername>`.

All Messages require to be formatted with the MiniMessage formatting.
Checkout [this site](https://docs.adventure.kyori.net/minimessage/format.html) for more information.

### MessageFiles

It becomes a horrible job to sync your [lang].yml and the class in which you store your Message Objects. Therefore, you
can autogenerate the lang files for one language.

First, you need to setup the class.

```Java

@MessageFile(
		author = "CubBossa",
		languageString = "en_US",
		version = "1.0",
		header = """
				This is a long header
				to demonstrate
				the functionality.
				"""
)
public class Messages {

}
```

The provided information will be displayed as header information. You can use this space to inform the administrators
about MiniMessage and how to use it.

Now, let's add Messages with default values.

```Java

@MessageFile(author = "CubBossa")
public class Messages {

	@MessageMeta(
			value = "This is just an example to demonstrate annotations.",
			comment = {"multi", "line", "comment"},
			placeholders = {"error", "player_name"})
	public static final Message HELLO_WORLD = new Message("general.hello_world");

	@MessageMeta(
			value = "Another Example",
			comment = "single line comment",
			placeholders = "only_one"
	)
	public static final Message HELLO_SPACE = new Message("general.hello_space");
}
```

You can also add group comments to sections, to prevent redundant information. E.g. if you have 10 messages for parsing,
you could add a group comment for all 10 messages.

Without Group Comment (placeholders in this case):

```Java

@MessageFile(author = "CubBossa")
public class Messages {

	@MessageMeta(value = "Error in format, please use <format>.", placeholders = "format")
	public static final Message FORMAT_STRING = new Message("general.hello_world");
	@MessageMeta(value = "Error in number format, please use <format>.", placeholders = "format")
	public static final Message FORMAT_INT = new Message("general.hello_space");
}
```

With Group Comment:

```Java

import de.cubbossa.translations.MessageGroupMeta;

@MessageFile(author = "CubBossa")
public class Messages {

	@MessageGroupMeta(path = "error.format", placeholders = "format")
	@MessageMeta(value = "Error in format, please use <format>.", placeholders = "format")
	public static final Message FORMAT_STRING = new Message("error.format.string");
	@MessageMeta(value = "Error in number format, please use <format>.", placeholders = "format")
	public static final Message FORMAT_NUMBER = new Message("error.format.number");
}
```

Result in comparison:
<table>
<tr><td>Without Group</td><td>With Group</td></tr>
<tr>
<td>

```YML
error:
  format:
    # Valid placeholders: <format>
    string: "Error in format, please use <format>."
    # Valid placeholders: <format>
    number: "Error in number format, please use <format>."
```

</td>
<td>

```YML
error:
  # Valid placeholders: <format>
  format:
    string: "Error in format, please use <format>."
    number: "Error in number format, please use <format>."
```

</td>
</tr>
</table>

Finally, you have to generate your class. Use the following code to create your yml file. Call it before loading the
languages if you want the language entries to be loaded instantly.

```Java
TranslationHandler.getInstance().registerAnnotatedLanguageClass(Messages.class);
```





