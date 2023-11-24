# Translations

A translation framework to translate chat messages.
This framework builds upon [Kyori Components and the MiniMessage format](https://docs.adventure.kyori.net/minimessage/format.html).

Translations are split into global server-wide translations and local application translations.
Translations exist in a treelike structure.
The global root Translations instance allows to provide translation strings and styles for all following Translations instances.
Each Translation instance can be forked into a child, which then uses all styles and messages of its parent but has some
encapsuled translations on its own. Mostly, there will be one global Translations instance and one per plugin.

Example of the Server folder structure and how translations are included:
```YML
/Server
  /plugins
    
    /lang
      global_styles.properties # <--- global styling rules
      en-US.yml # <--- global messages (like the server name)
    
    /{your plugin}
      /lang
        styles.properties # <--- application only styles
        en-US.yml # <--- application only messages
```

Styles are a simple map like the following:
```properties
# We use opening tags to define styles.
text-light="<white>"
text="<gray>"
text-dark="<dark-gray>"
```

Messages can be stored in many ways, like SQL, Yaml or properties.
In a properties file, messages would look like this:
```properties
some.example.message="<text-light>Some light text <aqua>that can also be styled directly</aqua></text-light>"
some.example.reference="An embedded message: <msg:some.example.message>"
```

As you can see in the example, messages can be embedded into each other, which
allows you to simply create own messages and use them all over the place.
Why is this useful? Think of the following example from my plugin:
```properties
# c-brand is a style for the main plugin color.
# bg and bg-dark are global styles.
prefix="<c-brand>PathFinder </c-brand><bg-dark>| </bg-dark><bg>"
other.message="<msg:prefix>Hello."
```
Prefix is not a message that is enforced by the plugin.
Users can simply create the entry, and it will be loaded by the plugin and
embedded in other messages.

## Setup

```Java
class ExamplePlugin extends JavaPlugin {

    Translations translations;

    public void onEnable() {
        TranslationsFramework.enable(new File(getDataFolder(), "/.."));
        translations = TranslationsFramework.application("MyPlugin");

        translations.setMessageStorage(new PropertiesMessageStorage(getLogger(), new File(getDataFolder(), "/lang/")));
        translations.setStyleStorage(new PropertiesStyleStorage(new File(getDataFolder(), "/lang/styles.properties")));

        translations.addMessages(TranslationsFramework.messageFieldsFromClass(Messages.class));

        // They will not overwrite pre-existing values.
        translations.saveLocale(Locale.ENGLISH);
        translations.saveLocale(Locale.GERMAN);
        
        translations.loadStyles();
        translations.loadLocales();
    }

    public void onDisable() {
        TranslationsFramework.disable();
    }
}
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
// or just:
translations.addMessages(TranslationsFramework.messageFieldsFromClass(Messages.class));
```

And now you can use the following code to get a translated component.

```Java

// Not in player specific language
Component myTranslatedComponent = Messages.ERR_NO_PLAYER.asComponent();

// pass player audience to get a translation in player language
Component myTranslatedComponent = Messages.ERR_NO_PLAYER.formatted(myPlayerAudience).asComponent();

// format the message with placeholders
Component myTranslatedComponent = Message.ERR_NO_PLAYER.formatted(
    Placeholder.component("value1", componentabc),
    Formatter.number("speed", playerSpeed)
).toComponent();

// or both
Component myTranslatedComponent = Messages.ERR_NO_PLAYER.formatted(myPlayerAudience).formatted(
    Placeholder.component("value1", componentabc),
    Formatter.number("speed", playerSpeed)
).toComponent();

```

Messages implement the ComponentLike interface, which means that you can use them in places that require components without
explicitly converting them to a component.
```Java
player.sendMessage(Messages.ERR_NO_PLAYER);
player.sendMessage(Messages.ERR_NO_PLAYER.formatted(myPlayerAudience).formatted(...));
```

Keep in mind, that the first line is still in the default language and not in the player language.

### Player Locales

Player languages are not set up by default.
You must specify for each Translation instance, how a players locale should be resolved. All sub Translations will inherit
this behaviour.

```Java
class ExamplePlugin extends JavaPlugin {

    Translations translations;

    public void onEnable() {
        // ...

        translations.setLocaleProvider(audience -> {
            Locale fallback;
            try {
                fallback = Locale.forLanguageTag(myConfig.fallbackLocale);
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Could not parse locale tag '" + fileConfig.fallbackLocale + "'. Using 'en' instead.");
                fallback = Locale.ENGLISH;
            }

            if (audience == null) {
                return fallback;
            }
            if (!myConfig.usePlayerClientLocale) {
                return fallback;
            }
            return audience.getOrDefault(Identity.LOCALE, fallback);
        });
    }
    // ...
}
```

