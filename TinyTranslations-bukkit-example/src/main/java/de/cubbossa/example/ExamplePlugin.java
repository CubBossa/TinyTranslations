package de.cubbossa.example;

import de.cubbossa.tinytranslations.BukkitTinyTranslations;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;
import de.cubbossa.tinytranslations.util.ListSection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class ExamplePlugin extends JavaPlugin {

    private MessageTranslator translator = null;

    @Override
    public void onEnable() {
        super.onEnable();

        // Create a MessageTranslator for this plugin
        translator = BukkitTinyTranslations.application(this);

        // Set storages of your choice. Can for example also be .properties files or sql databases.
        translator.setStyleStorage(new PropertiesStyleStorage(new File(getDataFolder(), "/lang/styles.properties")));
        translator.setMessageStorage(new PropertiesMessageStorage(new File(getDataFolder(), "/lang/"), "messages_", ""));

        // You may want to add some global properties.
        // We can for example make "plugin" a placeholder for all processed messages.
        // It will resolve into the plugins name or with according notation into other attributes, like
        // {plugin.version} -> v5.0.0
        // {plugin.author} -> CubBossa
        translator.insertObject("plugin", getDescription());
        translator.insertString("smile", ":D");

        // We create all messages for this plugin at once
        Messages.init(translator);

        // We call the reload functionality to save all messages into required files and to create our style sheet.
        // If any of the named existed already, their values will be fetched and cached.
        reloadLocales();

        // We can now use Messages.ANY_MESSAGE freely as if they were TextComponents

        getCommand("playerlist").setExecutor(this);
    }

    /**
     * Could be called when reloading plugin
     */
    public void reloadLocales() {
        boolean useClientLocale = true; // read from config or ignore step
        Locale fallbackLocale = Locale.ENGLISH; // read from config or ignore step

        translator.setUseClientLocale(useClientLocale);
        translator.defaultLocale(fallbackLocale);

        // save all locales that you want to exist in your lang directory.
        // this will not override user changes to the file, only add missing translations in the file and create file
        // if not existing yet.
        // You will use this if you haven't created an en.properties by hand but rely on "withDefault" calls.
        translator.saveLocale(Locale.ENGLISH);

        // Run this for all files in your resources/lang/ directory that you created by hand and want to save.
        // Also run it for ENGLISH if you didn't use "withDefault" calls while creating Messages
        for (Locale locale : List.of(Locale.GERMAN)) {
            if (!new File(getDataFolder(), "/lang/messages_" + locale.toLanguageTag() + ".properties").exists()) {
                saveResource("lang/messages_" + locale.toLanguageTag() + ".properties", false);
            }
        }

        // load all locales that can possibly be loaded
        translator.loadLocales();
        // load styles
        translator.loadStyles();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length > 1) {
            BukkitTinyTranslations.sendMessageIfNotEmpty(sender, Messages.CMD_PLAYERS_SYNTAX);
            return false;
        }

        int page;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Throwable t) {
                BukkitTinyTranslations.sendMessageIfNotEmpty(sender, Messages.INTEGER_REQUIRED
                        .insertString("input", args[0]));
                return false;
            }
        } else {
            page = 0;
        }

        BukkitTinyTranslations.sendMessageIfNotEmpty(sender, Messages.PLAYER_LIST
                .insertNumber("count", Bukkit.getOnlinePlayers().size())
                .insertList("players", Bukkit.getOnlinePlayers(), ListSection.paged(page, 3)));

        // Result could look like so:
        // ---- Online Players: 14 ------------------------------
        // 4.) CubBossa
        // 5.) Aristotle
        // 6.) Steve
        // ---- ← 2/5 → -----------------------------------------

        return false;
    }
}