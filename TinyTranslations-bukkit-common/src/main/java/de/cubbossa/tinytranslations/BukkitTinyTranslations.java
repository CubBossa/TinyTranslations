package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class BukkitTinyTranslations extends TinyTranslations {

    private static final Object mutex = new Object();
    private static BukkitAudiences audiences;
    private static volatile MessageTranslator server;

    static {
    }

    private BukkitTinyTranslations() {
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
        enable(new File(plugin.getDataFolder(), "/../"));
    }

    private static void enable(File pluginDirectory) {

        MessageTranslator g = server;
        if (g == null) {
            synchronized (mutex) {
                g = server;
                if (g == null) {
                    applyBukkitObjectResolvers(NM.getObjectTypeResolverMap());
                    server = globalTranslator(pluginDirectory);
                }
            }
        }
    }

    public static void disable() {
        audiences.close();
    }

    public static MessageTranslator globalTranslator(File dir) {
        applyBukkitObjectResolvers(NM.getObjectTypeResolverMap());
        var server = TinyTranslations.globalTranslator(dir);
        server.addMessages(messageFieldsFromClass(BukkitGlobalMessages.class));
        return server;
    }

    public static MessageTranslator application(String name) {
        if (!isEnabled()) {
            applyBukkitObjectResolvers(NM.getObjectTypeResolverMap());
        }
        return TinyTranslations.application(name);
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
        return getLocale(sender, DEFAULT_LOCALE);
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

    private static void applyBukkitObjectResolvers(ObjectTagResolverMap map) {
        map.put(PluginDescriptionFile.class, Map.of(
                "name", PluginDescriptionFile::getName,
                "fullName", PluginDescriptionFile::getFullName,
                "authors", d -> String.join(", ", d.getAuthors()),
                "version", PluginDescriptionFile::getVersion,
                "api-version", PluginDescriptionFile::getAPIVersion,
                "website", PluginDescriptionFile::getWebsite,
                "contributors", PluginDescriptionFile::getContributors
        ), d -> Component.text(d.getName()));
        map.put(Player.class, Map.of(
                "name", Player::getName,
                "uuid", Entity::getUniqueId,
                "type", Entity::getType,
                "display", Player::getDisplayName,
                "location", Player::getLocation
        ), p -> BukkitGlobalMessages.FORMAT_PLAYER.insertObject("player", p));
        map.put(Entity.class, Map.of(
                "name", Entity::getName,
                "uuid", Entity::getUniqueId,
                "type", Entity::getType,
                "location", Entity::getLocation
        ), p -> BukkitGlobalMessages.FORMAT_ENTITY.insertObject("entity", p));
        map.put(World.class, Map.of(
                "name", WorldInfo::getName,
                "uuid", WorldInfo::getUID
        ), w -> BukkitGlobalMessages.FORMAT_WORLD.insertObject("world", w));
        map.put(Block.class, Map.of(
                "type", Block::getType,
                "x", Block::getX,
                "y", Block::getY,
                "z", Block::getZ,
                "world", Block::getWorld,
                "biome", Block::getBiome
        ), b -> BukkitGlobalMessages.FORMAT_BLOCK.insertObject("block", b));
        map.put(Location.class, Map.of(
                "x", Location::getX,
                "y", Location::getY,
                "z", Location::getZ,
                "yaw", Location::getZ,
                "pitch", Location::getZ,
                "world", Location::getWorld
        ), l -> BukkitGlobalMessages.FORMAT_LOCATION.insertObject("loc", l));
        map.put(Vector.class, Map.of(
                "x", Vector::getX,
                "y", Vector::getY,
                "z", Vector::getZ
        ), v -> BukkitGlobalMessages.FORMAT_VECTOR.insertObject("vector", v));
        map.put(ItemStack.class, Map.of(
                "type", ItemStack::getType,
                "amount", ItemStack::getAmount,
                "name", i -> i.hasItemMeta() ? i.getItemMeta().getDisplayName() : i.getType(),
                "lore", i -> i.hasItemMeta() ? i.getItemMeta().getLore() : Collections.emptyList()
        ), i -> BukkitGlobalMessages.FORMAT_ITEM.insertObject("item", i));

        map.put(PotionEffectType.class, Collections.emptyMap(), p -> Component.translatable("effect.minecraft." + p.getKey().getKey()));
        map.put(ChatColor.class, Collections.emptyMap(), c -> Component.translatable("color.minecraft." + c.toString()));
        map.put(Enchantment.class, Collections.emptyMap(), e -> Component.translatable("enchantment.minecraft." + e.getKey().getKey()));
        map.put(Material.class, Collections.emptyMap(), m -> Component.translatable((m.isBlock() ? "block" : "item") + ".minecraft." + m.name().toLowerCase()));
        map.put(EntityType.class, Collections.emptyMap(), t -> Component.translatable(t.getTranslationKey()));
        map.put(Biome.class, Collections.emptyMap(), b -> Component.translatable("biome." + b.getKey().getNamespace() + "." + b.getKey().getKey()));
    }
}
