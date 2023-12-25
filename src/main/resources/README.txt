#=================================#
#                                 #
# Global Translations README      #
#                                 #
#=================================#

The "lang" directory within your plugin directory appeared, because one of
the installed plugins uses the Translations Framework dependency.

More information on the framework can be found here: https://github.com/CubBossa/Translations

The library comes with support for
- Plain Text Messages
- Legacy Text Messages
- MiniMessage Format
- JSON (NBT) Messages
(Read on to learn more)

Every Message can be cross-referenced with {msg:key}, so

    my_message.a = "hello"
    my_message.b = "{msg:my_message.a} world!"

is fully valid. We could use this to insert the plugins prefix.

You can also cross-reference messages from the global directory, which is why this directory exists.
A very common use case would be, that you place your server name in this directory, like so:

    brand = "<gradient:#ffa200:#fffbc9:#ffa200>My Server Name</gradient>"

Then you can use {msg:brand} in all your plugin translations that support this library.
You only have to change a translation in one place to make an effect on all translations.

Plugin translations are always preferred to global translations, so if both your plugins en.properties and
the global en.properties contain a message with "no_perm", the plugin translation will be preferred.
If you want to explicitly call a global translation, use {msg:[path]:[key]}, like {msg:global:no_perm}
This works not only for the global translations but for all registered translations instances.
If there are two plugins A and B, you can use a translation from B in A with {msg:global.B:no_perm}, even though
it is not recommended to use translations that are not from direct or indirect parent translations.

global
    A (Plugin)
        X (Sub app of A, for example an addon to A)
        Y (Sub app of B)
    B (Plugin)
    C (Plugin)

Use:
 - <reset> To reset styles

 - All kinds of colors to apply colors: <red>test</red>.
   Full list can be found here: https://docs.advntr.dev/minimessage/format.html#color

 - Decorations:
     - <underlined> (<u>)
     - <bold> (<b>)
     - <italic> (<em> or <i>)
     - <strikethrough> (<st>)
     - <obfuscated> (<obf>)

 - Click with <click:[action]:[data]>Text</click>
   Example: <click:run_command:"/gm 1">Click for Creative</click>

 - Hover texts with <hover:[action]:[data]>Text</hover>
   Example: <hover:show_text:"<green>Oh hey<green>">Hover me</hover>

 - <repeat:[times]></repeat> To repeat the given content for any amount of times.

 - <darker></darker> or <brighter></brighter> To make the content darker or brighter.
   Important: <red><darker>Hi</darker><red> does not provide a darker shade of red. Only explicit colors in the content
   are being darkened

 - <lower></lower> makes the content lower case

 - <upper></upper> makes the content upper case

 - <reverse></reverse>Will render the content backwards. Keep in mind that
   <gradient:green:red><reverse>Helloworld</reverse></gradient> is NOT
   <gradient:red:green>dlrowolleH</gradient>, but
   <gradient:green:red>dlrowolleH</gradient>
   Outer styles are not applied.

And many more (https://docs.advntr.dev/minimessage/format.html)


Other Formats
-------------

Other formats than MiniMessage are supported, but not advised.
To encode a message in a different format, use the following syntax:

example_message = <legacy>&cA legacy ampersand text</legacy>
example_message = <legacy:'&'>&cA legacy ampersand text</legacy>
example_message = <legacy:'§'>§cA legacy text</legacy>
example_message = <plain>A plain text without colors</plain>
example_message = <nbt>{"text":"A nbt text","color":"red"}</nbt>
example_message = <red>A MiniMessage text because MiniMessage is default.</red>"

Styles
------

Styles are a way to easily change the look and feel of your messages.
Instead of explicitly using styles in your language file, you can use style placeholders like
<primary> for your primary color or <bg> for background colors.

Use them for your messages like so:
buy.success = "<text>Successfully bought plot!</text>"
buy.no_perms = "<negative>No permission!</negative>"

And style them from within your style files:
text = "<light_gray>{slot}</light_gray>"
negative = "<red><bold>{}</bold></red>"

Application styles override global styles, so if you put a style "negative" in global and one with
the exact same key in your applications styles.properties, the application style will be used.

