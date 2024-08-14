package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.function.Function;

public class BungeeTinyTranslations extends TinyTranslations {

    private static final Object mutex = new Object();
    private static BungeeAudiences audiences;
    private static Metrics metrics = null;
    private static volatile MessageTranslator server;

    private BungeeTinyTranslations() {
        TinyTranslations.getLogger().setParent(ProxyServer.getInstance().getLogger());
    }

    public static MessageTranslator server() {
        MessageTranslator g = server;
        if (g == null) {
            throw new IllegalStateException("Accessing global without enabling TranslationsFramework.");
        }
        return g;
    }

    public static boolean isEnabled() {
        MessageTranslator g = server;
        if (g == null) {
            synchronized (mutex) {
                return g != null;
            }
        }
        return false;
    }

    private static void enable(Plugin plugin) {
        audiences = BungeeAudiences.create(plugin);

        if (metrics == null) {
            metrics = new Metrics(plugin, 20979);
        }

        enable(new File(plugin.getDataFolder(), "/../"));
    }

    private static void enable(File pluginDirectory) {

        MessageTranslator g = server;
        if (g == null) {
            synchronized (mutex) {
                g = server;
                if (g == null) {
                    server = globalTranslator(pluginDirectory);
                    applyBungeeObjectResolvers(server);
                }
            }
        }
    }

    public static void disable() {
        audiences.close();
    }

    public static MessageTranslator globalTranslator(File dir) {
        var server = TinyTranslations.globalTranslator(dir);
        applyBungeeObjectResolvers(server);
        server.addMessages(messageFieldsFromClass(BungeeGlobalMessages.class));
        return server;
    }

    public static MessageTranslator application(String name) {
        var tr = TinyTranslations.application(name);
        applyBungeeObjectResolvers(tr);
        return tr;
    }

    public static MessageTranslator application(Plugin plugin) {
        if (!isEnabled()) {
            enable(plugin);
        }
        var app = server().fork(plugin.getDescription().getName());
        return app;
    }

    public static Function<@Nullable Audience, Locale> getPerPlayerLocaleProvider(Locale fallback) {
        return audience -> {
            if (audience == null) {
                return fallback;
            }
            return audience.getOrDefault(Identity.LOCALE, fallback);
        };
    }

    public static Locale getLocale(CommandSender sender) {
        return getLocale(sender, FALLBACK_DEFAULT_LOCALE);
    }

    public static Locale getLocale(CommandSender sender, Locale fallback) {
        return audiences.sender(sender).get(Identity.LOCALE).orElse(fallback);
    }

    public static void sendActionBar(CommandSender sender, ComponentLike message) {
        audiences.sender(sender).sendActionBar(message);
    }

    public static void sendActionBarIfNotEmpty(CommandSender sender, ComponentLike message) {
        Component c = message.asComponent();
        if (c.equals(Component.empty())) {
            return;
        }
        sendActionBar(sender, message);
    }

    public static void sendMessage(CommandSender sender, ComponentLike message) {
        audiences.sender(sender).sendMessage(message);
    }

    public static void sendMessageIfNotEmpty(CommandSender sender, ComponentLike message) {
        Component c = message.asComponent();
        if (c.equals(Component.empty())) {
            return;
        }
        sendMessage(sender, message);
    }

    private static void applyBungeeObjectResolvers(MessageTranslator tr) {
        tr.add(TinyObjectMapping.builder(ProxiedPlayer.class)
                .with("uuid", ProxiedPlayer::getUniqueId)
                .with("display_name", ProxiedPlayer::getDisplayName)
                .with("name", ProxiedPlayer::getName)
                .with("server", ProxiedPlayer::getServer)
                .with("locale", ProxiedPlayer::getLocale)
                .withFallbackConversion(p -> BungeeGlobalMessages.FORMAT_PLAYER.insertObject("player", p))
                .build());
        tr.add(TinyObjectMapping.builder(Plugin.class)
                .withFallbackConversion(Plugin::getDescription)
                .build());
        tr.add(TinyObjectMapping.builder(PluginDescription.class)
                .with("name", PluginDescription::getName)
                .with("author", PluginDescription::getAuthor)
                .with("authors", PluginDescription::getAuthor)
                .with("version", PluginDescription::getVersion)
                .withFallbackConversion(d -> Component.text(d.getName()))
                .build());
        tr.add(TinyObjectMapping.builder(ProxyServer.class)
                .with("name", ProxyServer::getName)
                .with("online_count", ProxyServer::getOnlineCount)
                .with("players", ProxyServer::getPlayers)
                .with("plugins", s -> s.getPluginManager().getPlugins())
                .withFallbackConversion(ProxyServer::getName)
                .build());
    }
}
