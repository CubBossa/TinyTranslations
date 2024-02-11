package de.cubbossa.example;

import de.cubbossa.tinytranslations.BukkitTinyTranslations;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import de.cubbossa.tinytranslations.storage.yml.YamlMessageStorage;
import de.cubbossa.tinytranslations.storage.yml.YamlStyleStorage;
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
    public void onLoad() {
        translator = BukkitTinyTranslations.application(this);
        // Set storages of your choice. Can for example also be .properties files or sql databases.
        translator.setStyleStorage(new YamlStyleStorage(new File(getDataFolder(), "/lang/styles.yml")));
        translator.setMessageStorage(new YamlMessageStorage(new File(getDataFolder(), "/lang/")));

        // Since we created static fields for all messages in Messages.class, we need to add them like so
        translator.addMessages(TinyTranslations.messageFieldsFromClass(Messages.class));
        // instead, we could also create local fields. Would actually be nicer.

        reloadLocales();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        getCommand("playerlist").setExecutor(this);
    }

    /**
     * Could be called when reloading plugin
     */
    public void reloadLocales() {
        boolean useClientLocale = true; // read from config or ignore step
        Locale fallbackLocale = Locale.ENGLISH; // read from config or ignore step

        translator.setUseClientLocale(useClientLocale);
        translator.setDefaultLocale(fallbackLocale);

        // save all locales that you want to exist in your lang directory.
        // this will not override user changes to the file, only add missing translations in the file and create file
        // if not existing yet.
        // You will use this if you haven't created an en.yml by hand but rely on "withDefault" calls.
        translator.saveLocale(Locale.ENGLISH);

        // Run this for all files in yourr resources/lang/ directory that you created by hand and want to save.
        // Also run it for ENGLISH if you didn't use "withDefault" calls while creating Messages
        for (Locale locale : List.of(Locale.GERMAN)) {
            if (!new File(getDataFolder(), "/lang/" + locale.toLanguageTag() + ".yml").exists()) {
                saveResource("lang/" + locale.toLanguageTag() + ".yml", false);
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
                .insertList("players", List.of(Bukkit.getOnlinePlayers()), ListSection.paged(page, 3)));

        // Result could look like so:
        // ---- Online Players: 14 ------------------------------
        // 4.) CubBossa
        // 5.) Aristotle
        // 6.) Steve
        // ---- ← 2/5 → -----------------------------------------

        return false;
    }
}