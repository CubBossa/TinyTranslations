# TinyTranslations

A translation framework to translate chat messages.
This framework builds upon [Kyori Components and the MiniMessage format](https://docs.adventure.kyori.net/minimessage/format.html).

## Wiki
- [Overview](https://github.com/CubBossa/Translations/wiki)
- [Setup](https://github.com/CubBossa/Translations#maven)
- Messages
  - [Overview](https://github.com/CubBossa/Translations/wiki/Messages)
  - [Using Placeholders](https://github.com/CubBossa/Translations/wiki/Placeholders)
  - [Send or Use Messages](https://github.com/CubBossa/Translations/wiki/Send-or-Use-Messages)
  - [Alternative Chat Formats](https://github.com/CubBossa/Translations/wiki/Legacy-Format-Support)
  - [Per Player Locale](https://github.com/CubBossa/Translations/wiki/Per-Player-Locale)
- [Styles](https://github.com/CubBossa/Translations/wiki/Styles)
- Storages
  - [Creating a Custom Storage](https://github.com/CubBossa/Translations/wiki/Creating-a--Custom-Storage)
  - [YAML](https://github.com/CubBossa/Translations/wiki/Yaml-Storage)
  - [Properties](https://github.com/CubBossa/Translations/wiki/Properties-Storage)

## Maven

Install the following repository and dependency in your pom.xml or your build.gradle.
Make sure to use the latest version.

```XML
<repositories>
    <repository>
        <id>Translations</id>
        <url>https://nexus.leonardbausenwein.de/repository/maven-public/</url>
    </repository>
</repositories>
```

```XML
<dependencies>
    <dependency>
        <groupId>de.cubbossa</groupId>
        <artifactId>TinyTranslations</artifactId>
        <version>[version]</version>
    </dependency>
    
    <!-- All kyori dependencies if not yet present -->
    <dependency>
        <groupId>net.kyori</groupId>
        <artifactId>adventure-api</artifactId>
        <version>4.14.0</version>
    </dependency>
    <dependency>
        <groupId>net.kyori</groupId>
        <artifactId>adventure-platform-bukkit</artifactId>
        <version>4.3.2</version>
    </dependency>
    <dependency>
        <groupId>net.kyori</groupId>
        <artifactId>adventure-text-minimessage</artifactId>
        <version>4.14.0</version>
    </dependency>
</dependencies>
```

### Shading
When shading, it is highly recommended to relocate the resource within your plugin.
This assures that no other plugin loads outdated Translations classes before your
plugin can load the latest classes. Occurring errors would potentially disable your plugin on startup.

```XML

<build>
    <plugins>
        ...
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.4</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>de.cubbossa.tinytranslationsde.cubbossa.tinytranslations</pattern>
                                <shadedPattern>[yourpluginpath].libs.translations</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

or with gradle:
```groovy
tasks.shadowJar {
    minimize()
    relocate 'de.cubbossa.tinytranslations', '[yourpluginpath].libs.translations'
}
```

### Dependencies (Spigot Libraries)

Your server must find and load the Kyori Adventure classes for Translations to work.
Either also shade them or instead register them as Spigot Libraries.
Therefore, go to your plugin.yml and add the section:
```yml
libraries:
  - net.kyori:adventure-api:4.14.0
  - net.kyori:adventure-platform-bukkit:4.3.2
  - net.kyori:adventure-text-minimessage:4.14.0
  - net.kyori:adventure-text-serializer-legacy:4.14.0
  - net.kyori:adventure-text-serializer-gson:4.14.0
  - net.kyori:adventure-text-serializer-plain:4.14.0
```

## Overview

Translations are split into global server-wide translations and local application translations.
Translations exist in a treelike structure.
The global root Translations instance allows to provide translation strings and styles for all following Translations instances.
Each Translation instance can be forked into a child, which then uses all styles and messages of its parent but has some
encapsulated translations on its own. Mostly, there will be one global Translations instance and one per plugin.

Example of the Server folder structure and how translations are included:
```YML
/Server
  /plugins
    
    /lang
      global_styles.properties # <--- global styling rules
      en-US.yml # <--- global messages (like the server name)
    
    /YourPlugin
      /lang
        styles.properties # <--- application only styles
        en-US.yml # <--- application only messages
```

Styles are a way to create new tag resolvers
```properties
# We use opening tags to define simple styles.
text-light="<white>"
text="<gray>"
text-dark="<dark-gray>"
# Or slot based styles for more complex patterns
# The list-el example will render "<list-el>abc</list-el>" as "- abc", where the "-" is gray and "abc" is white.
list-el="<gray>- </gray><white>{slot}</white>\n"
# the url tag renders only a short version but opens the whole url on click.
#     https://docs.advntr.dev/minimessage/format.html
# becomes
#     https://docs.advntr.dev/min...tml
# The first occurring '/' after the domain separates the tail, which will show its first and last three letters.
url="<blue><u><click:open_url:"{slot}"><hover:show_text:"Click to open url"><shorten_url>{slot}</shorten_url></hover></click></u></blue>"
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
import de.cubbossa.tinytranslations.MessageBuilder;

class Messages {
	public static final Message PREFIX = new MessageBuilder("prefix")
			.withDefault("<gradient:#ff0000:#ffff00:#ff0000>My Awesome Plugin</gradient>: ")
			.build();
	public static final Message NO_PERM = new MessageBuilder("no_perm")
			.withDefault("<red>No permissions!</red>")
			.build();
}


class ExamplePlugin extends JavaPlugin {

	Translations translations;

	public void onEnable() {
		// Enable Framework
		TranslationsFramework.enable(new File(getDataFolder(), "/.."));
		// create a Translations instance for your plugin 
		translations = TranslationsFramework.application("MyPlugin");

		// define the storage types for your plugins locale
		translations.setMessageStorage(new PropertiesMessageStorage(getLogger(), new File(getDataFolder(), "/lang/")));
		translations.setStyleStorage(new PropertiesStyleStorage(new File(getDataFolder(), "/lang/styles.properties")));

		// register all your messages to your Translations instance
		// a message cannot be translated without a Translations instance, which works as
		// messageTranslator.
		translations.addMessages(messageA, messageB, messageC);
		translations.addMessage(messageD);
		// just load all public static final messages declared in Messages.class
		translations.addMessages(TranslationsFramework.messageFieldsFromClass(Messages.class));

		// They will not overwrite pre-existing values.
		// You only need to save values that you assigned programmatically, like from a
		// message builder. You can also create a de.properties resource and save it as file instead.
		// Then there is no need to write the german defaults to file here.
		translations.saveLocale(Locale.ENGLISH);
		translations.saveLocale(Locale.GERMAN);

		// load all styles and locales from file. This happens for all parent translations,
		// so all changes to the global styles and translations will apply too.
		translations.loadStyles();
		translations.loadLocales();
	}

	public void onDisable() {
		// close open Translations instance
		translations.close();
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

### Add Messages to Translations

Don't forget to register all messages to your application!!
```Java
translations.addMessage(Messages.ERR_NO_PLAYER);
translations.addMessage(Messages.ERR_NO_PERM);
// or
translations.addMessages(Messages.ERR_NO_PLAYER, Messages.ERR_NO_PERM);
// or just:
translations.addMessages(TranslationsFramework.messageFieldsFromClass(Messages.class));
```

### Build Messages from Translations directly

If you use your translations instance to create a message, it will automatically be added
to your translations.
```Java
ERR_NO_PERM = translations.message("error.no_perm");
ERR_NO_PERM = translations.messageBuilder("error.no_perm")
        .withDefault("<c-negative>No permission!</c-negative>")
        .build();
```

### Message as Component

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
// PaperMC only, for other platforms you still need to convert the player into an Audience.
// See Kyori Docs for more information
// https://docs.advntr.dev/platform/bukkit.html#usage
player.sendMessage(Messages.ERR_NO_PLAYER);
player.sendMessage(Messages.ERR_NO_PLAYER.formatted(myPlayerAudience).formatted(...));
```

Keep in mind, that the first line is still in the default language and not in the player language.

### Other Formats

You can also format a Message into any other format with like so:
```Java
Message ERR_NO_PERM = new MessageBuilder("err.no_perm")
        .withDefault("<c-negative>No permissions!</c-negative>")
        .build();

String s = ERR_NO_PERM.toString(MessageFormat.LEGACY_PARAGRAPH);
// -> §cNo permissions!
String s = ERR_NO_PERM.toString(MessageFormat.LEGACY_AMPERSAND);
// -> &cNo permissions!
String s = ERR_NO_PERM.toString(MessageFormat.PLAIN);
// -> No permissions!
String s = ERR_NO_PERM.toString(MessageFormat.NBT);
// -> {"text":"No permissions!","color":"red"}
```

