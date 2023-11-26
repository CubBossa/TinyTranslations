#=================================#
#                                 #
# Global Translations README      #
#                                 #
#=================================#

The "lang" directory within your plugin directory appeared, because one of
the installed plugins uses the Translations library.

More information on the library can be found here: https://github.com/CubBossa/Translations

The library comes with support for
- Plain Text Messages
- Legacy Text Messages
- MiniMessage Format
- JSON (NBT) Messages
(Read on to learn more)

Every Message can be cross-referenced with <msg:[key]>, so

    my_message.a = "hello"
    my_message.b = "<msg:my_message.a> world!"

is fully valid. We mostly use this to insert the plugins prefix.

You can also cross-reference messages from the global directory, which is why this directory exists.
A very common use case would be, that you place your server name in this directory, like so:

    brand = "<gradient:#ffa200:#fffbc9:#ffa200>Hypixel</gradient>"

Then you can use <msg:brand> in all your plugin translations that support this library.
You only have to change a translation in one place to make an effect on all translations.

Plugin translations are always preferred to global translations, so if both your plugins en.properties and
the global en.properties contain a message with "no_perm", the plugin translation will be preferred.
If you want to explicitly call a global translation, use <msg:[path]:[key]>, like <msg:global:no_perm>
This works not only for the global translations but for all registered translations instances.
If there are two plugins A and B, you can use a translation from B in A with <msg:global.B:no_perm>, even though
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

And many more (https://docs.advntr.dev/minimessage/format.html)


Other Formats
-------------

Other formats than MiniMessage are supported, but not advised.
To encode a message in a different format, use the following prefax:

example_message = "!!<encoding>: <message>"
example_message = "!!paragraph: §cA legacy text"
example_message = "!!ampersand: &cA legacy ampersand text"
example_message = "!!plain: A plain text without colors"
example_message = "!!nbt: {"text":"A nbt text","color":"red"}"
example_message = "!!minimessage: <red>A MiniMessage text</red>"
example_message = "<red>A MiniMessage text because MiniMessage is default.</red>"

Styles
------

Styles are a way to easily change the look and feel of your messages.
Instead of explicitly using styles in your language file, you can use style placeholders like
<c_primary> for your primary color or <c_bg> for background colors.

Use them for your messages like so:
buy.success: = "<c_text>Successfully bought plot!</c_text>"
buy.no_perms = "<c_negative>No permission!</c_negative>"

And style them from within your style files:
c_text = "<light_gray>"
c_negative = "<red><bold>"

Application styles override global styles, so if you put a style "c_negative" in global and one with
the exact same key in your applications styles.properties, the application style will be used.

The style format is a combination of opening tags that render has an effect on an ongoing text.
  "<red><ul><click:...:...>"            -> valid
  "<red>hehe</red><click:...:...>"      -> invalid
  "<red></red>"                         -> invalid
  ""                                    -> valid but stupid


