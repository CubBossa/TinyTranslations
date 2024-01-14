package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class TinyTranslationsBukkit extends TinyTranslations {

	private TinyTranslationsBukkit() {}
	private static BukkitAudiences audiences;

	static {
		applyBukkitObjectResolvers(NM.getObjectTypeResolverMap());
	}

	public static void enable(JavaPlugin plugin) {
		audiences = BukkitAudiences.create(plugin);
		TinyTranslations.enable(new File(plugin.getDataFolder(), "/../"));

		global().addMessages(messageFieldsFromClass(BukkitGlobalMessages.class));
		global().saveLocale(Locale.ENGLISH);
	}

	public static void disable() {
		TinyTranslations.disable();
		audiences.close();
	}

	public static Translator application(JavaPlugin plugin) {
		if (!isEnabled()) {
			enable(plugin);
		}
		var app = application(plugin.getName());
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
		));
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
				"world", Block::getWorld
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
		map.put(Material.class, Collections.emptyMap(), m -> BukkitGlobalMessages.FORMAT_MATERIAL.insertObject("material", m));
		map.put(EntityType.class, Collections.emptyMap(), m -> BukkitGlobalMessages.FORMAT_ENTITY_TYPE.insertObject("type", m));
	}
}
