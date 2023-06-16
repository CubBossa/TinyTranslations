# Translations

A translation framework to translate chat messages.
This framework builds upon [Kyori Components and the MiniMessage format](https://docs.adventure.kyori.net/minimessage/format.html).

Translations are split into global server-wide translations and local application translations.
Translations always come with a set of styles, which must not necessarily be used.

Example of the Server folder structure and how translations are included:
```
/Server
  /plugins
    
    /Translations
      styles.properties <--- global styling rules
      en-US.yml <--- global messages (like the server name)
    
    /{your plugin}
      /lang
        styles.properties <--- application only styles
        en-US.yml <--- application only messages
```

Styles are a simple map like the following:
```Yaml
# We use opening tags to define styles.
text-light: "<white>"
text: "<gray>"
text-dark: "<dark-gray>"
```

Messages can be stored in many ways, like SQL, Yaml or properties.
In a properties file, messages would look like this:
```properties
some.example.message="<text-light>Some light text <aqua>that can also be styled directly</aqua></text-light>"
some.example.reference="An embedded message: <msg:some.example-message>"
```

As can be seen in the example, messages can be embedded into each other, which
allows you to simply create own messages and use them all over the place.
Why is this useful? Think of the following example from my plugin:
```properties
# c-brand is a style for the main plugin color.
# bg and bg-dark are global styles.
prefix="<c-brand>PathFinder </c-brand><bg-dark>| </bg-dark><bg>"
other.message="<msg:prefix>Hello."
```
Prefix is not a message that is enforced by the plugin.
Users can simply create the entry and it will be loaded by the plugin and
embeddedw in other messages.

## Setup

First, you have to initialize the TranslationHandler.

```Java
// use your plugin name and plugin directory as parameters
// it is important to use the actual plugin directory, because it will be
// used to find the general plugins directory to place the Translations folder in.
MessageBundle translations = GlobalMessageBundle.applicationTranslationsBuilder(pl.getName(), pl.getDataFolder())
        // The client locale will be read and used as player locale
        .withPreferClientLanguage()
        // The locale that will be used if all else fails
        .withDefaultLocale(Locale.ENGLISH)
        .withLogger(pl.getLogger())
        // Define a storage for locale files.
        // Here you could also provide different storages that e.g. support SQL
        .withPropertiesStorage(dir)
        // Define a storage for style files
        .withPropertiesStyles(dir)
        // All locales that are allowed. For every locale
        // listed here there may be a locale file if any player has the
        // according language as client language
        .withEnabledLocales(Locale.getAvailableLocales())
        .build();
```

### Messages

Messages can easily be set up as statics.
Locale files can be generated from a class that holds Messages
as static members.

A fully defined message:
```Java
public static final Message ERR_NO_PLAYER = new MessageBuilder("error.must_be_player")
        .withDefault("<c-negative>No player found: '<input>'.</c-negative>")
        .withTranslation(Locale.GERMAN, "<c-negative>Spieler nicht gefunden: '<input>'.</c-negative>")
        .withComment("Used to indicate if no player was found - who would have thought :P")
        .withPlaceholder("input", "The used input that was supposed to be a playername.")
        .build();
```

Or maybe just
```Java
public static final Message ERR_NO_PERM = new MessageBuilder("error.no_perm")
        .withDefault("<c-negative>No permission!</c-negative>")
        .build();
```

Don't forget to register all messages to your application!!
```Java
translations.addMessage(Messages.ERR_NO_PLAYER);
translations.addMessage(Messages.ERR_NO_PERM);
// or
translations.addMessages(Messages.ERR_NO_PLAYER, Messages.ERR_NO_PERM);
// or best:
translations.addMessagesClass(Messages.class);
```

And now you can use the following code to get a translated component.

```Java
// pass player audience to get a translation in player language
Component myTranslatedComponent = translations.translate(Messages.ERR_NO_PLAYER, myPlayerAudience);
// if not, the Translator class will use the default language.
Component myTranslatedComponent = translations.translate(Messages.ERR_NO_PLAYER);
// or format the message with placeholders
Message formatted = Message.ERR_NO_PLAYER.formatted(
    Placeholder.component("value1", componentabc),
    Formatter.number("speed", playerSpeed)
);
Component myTranslatedComponent = translations.translate(formatted);
// or inline
Component myTranslatedComponent = translations.translate(Messages.ERR_NO_PLAYER.formatted(
    Placeholder.component("value1", componentabc),
    Formatter.number("speed", playerSpeed)
));

```




