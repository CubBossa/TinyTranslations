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

Every Message can be cross-referenced with <msg:{key}>, so

    my_message.a = "hello"
    my_message.b = "<msg:my_message.a> world!"

is fully valid. We mostly use this to insert the plugins prefix.

You can also cross-reference messages from the global directory, which is why this directory exists.
A very common use case would be, that you place your server name in this directory, like so:

    brand = "<gradient:#ffa200:#fffbc9:#ffa200>Hypixel</gradient>"

Then you can use <msg:brand> in all your plugin translations that support this library.
You only have to change a translation in one place to make an effect on all translations.