package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectTagResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Function;

public final class BukkitTinyTranslations extends TinyTranslations {

    private static final Object mutex = new Object();
    private static BukkitAudiences audiences;
    private static Metrics metrics = null;
    private static volatile MessageTranslator server;

    private BukkitTinyTranslations() {
        TinyTranslations.getLogger().setParent(Bukkit.getLogger());
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
        audiences = BukkitAudiences.create(plugin);

        if (metrics == null && plugin instanceof JavaPlugin jp) {
            metrics = new Metrics(jp, 20979);
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
                    applyBukkitObjectResolvers(server);
                }
            }
        }
    }

    public static void disable() {
        audiences.close();
    }

    public static MessageTranslator globalTranslator(File dir) {
        var server = TinyTranslations.globalTranslator(dir);
        applyBukkitObjectResolvers(server);
        server.addMessages(messageFieldsFromClass(BukkitGlobalMessages.class));
        return server;
    }

    public static MessageTranslator application(String name) {
        var tr = TinyTranslations.application(name);
        applyBukkitObjectResolvers(tr);
        return tr;
    }

    public static MessageTranslator application(Plugin plugin) {
        if (!isEnabled()) {
            enable(plugin);
        }
        var app = server().fork(plugin.getName());
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

    private static void applyBukkitObjectResolvers(MessageTranslator tr) {
        tr.add(TinyObjectResolver.builder(NamespacedKey.class)
                .with("namespace", NamespacedKey::getNamespace)
                .with("key", NamespacedKey::getKey)
                .withFallback(k -> Component.text(k.toString()))
                .build());
        tr.add(TinyObjectResolver.builder(PluginDescriptionFile.class)
                .with("name", PluginDescriptionFile::getName)
                .with("fullName", PluginDescriptionFile::getFullName)
                .with("authors", d -> String.join(", ", d.getAuthors()))
                .with("version", PluginDescriptionFile::getVersion)
                .with("api-version", PluginDescriptionFile::getAPIVersion)
                .with("website", PluginDescriptionFile::getWebsite)
                .with("contributors", PluginDescriptionFile::getContributors)
                .withFallback(d -> Component.text(d.getName()))
                .build());
        tr.add(TinyObjectResolver.builder(Player.class)
                .with("name", Player::getName)
                .with("uuid", Entity::getUniqueId)
                .with("type", Entity::getType)
                .with("display", Player::getDisplayName)
                .with("location", Player::getLocation)
                .withFallback(p -> BukkitGlobalMessages.FORMAT_PLAYER.insertObject("player", p))
                .build());
        tr.add(TinyObjectResolver.builder(Entity.class)
                .with("name", Entity::getName)
                .with("uuid", Entity::getUniqueId)
                .with("type", Entity::getType)
                .with("location", Entity::getLocation)
                .withFallback(p -> BukkitGlobalMessages.FORMAT_ENTITY.insertObject("entity", p))
                .build());
        tr.add(TinyObjectResolver.builder(World.class)
                .with("name", WorldInfo::getName)
                .with("uuid", WorldInfo::getUID)
                .withFallback(w -> BukkitGlobalMessages.FORMAT_WORLD.insertObject("world", w))
                .build());
        tr.add(TinyObjectResolver.builder(Block.class)
                .with("type", Block::getType)
                .with("x", Block::getX)
                .with("y", Block::getY)
                .with("z", Block::getZ)
                .with("world", Block::getWorld)
                .with("biome", Block::getBiome)
                .withFallback(b -> BukkitGlobalMessages.FORMAT_BLOCK.insertObject("block", b))
                .build());
        tr.add(TinyObjectResolver.builder(Location.class)
                .with("x", Location::getX)
                .with("y", Location::getY)
                .with("z", Location::getZ)
                .with("yaw", Location::getZ)
                .with("pitch", Location::getZ)
                .with("world", Location::getWorld)
                .withFallback(l -> BukkitGlobalMessages.FORMAT_LOCATION.insertObject("loc", l))
                .build());
        tr.add(TinyObjectResolver.builder(Vector.class)
                .with("x", Vector::getX)
                .with("y", Vector::getY)
                .with("z", Vector::getZ)
                .withFallback(v -> BukkitGlobalMessages.FORMAT_VECTOR.insertObject("vector", v))
                .build());
        tr.add(TinyObjectResolver.builder(ItemStack.class)
                .with("type", ItemStack::getType)
                .with("amount", ItemStack::getAmount)
                .with("name", i -> i.hasItemMeta() ? i.getItemMeta().getDisplayName() : i.getType())
                .with("lore", i -> i.hasItemMeta() ? i.getItemMeta().getLore() : Collections.emptyList())
                .withFallback(i -> BukkitGlobalMessages.FORMAT_ITEM.insertObject("item", i))
                .build());

        tr.add(TinyObjectResolver.builder(PotionEffectType.class).withFallback(p -> Component.translatable("effect.minecraft." + p.getKey().getKey())).build());
        tr.add(TinyObjectResolver.builder(ChatColor.class).withFallback(c -> Component.translatable("color.minecraft." + c.toString())).build());
        tr.add(TinyObjectResolver.builder(Enchantment.class).withFallback(e -> Component.translatable("enchantment.minecraft." + e.getKey().getKey())).build());
        tr.add(TinyObjectResolver.builder(Material.class).withFallback(m -> Component.translatable((m.isBlock() ? "block" : "item") + ".minecraft." + m.name().toLowerCase())).build());
        tr.add(TinyObjectResolver.builder(EntityType.class).withFallback(t -> Component.translatable(t.getTranslationKey())).build());
        tr.add(TinyObjectResolver.builder(Biome.class).withFallback(b -> Component.translatable("biome." + b.getKey().getNamespace() + "." + b.getKey().getKey())).build());
    }
}
