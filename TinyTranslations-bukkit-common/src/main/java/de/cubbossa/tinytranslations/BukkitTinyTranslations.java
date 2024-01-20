package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.impl.MessageTranslatorImpl;
import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class BukkitTinyTranslations extends TinyTranslations {

	private static BukkitAudiences audiences;
	private static volatile MessageTranslator server;

	private static final Object mutex = new Object();
	public static MessageTranslator server() {
		MessageTranslator g = server;
		if (g == null) {
			throw new IllegalStateException("Accessing global without enabling TranslationsFramework.");
		}
		return g;
	}


	static {
		applyBukkitObjectResolvers(NM.getObjectTypeResolverMap());
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

	private static void enable(JavaPlugin plugin) {
		audiences = BukkitAudiences.create(plugin);
		enable(new File(plugin.getDataFolder(), "/../"));
	}

	private static void enable(File pluginDirectory) {

		MessageTranslator g = server;
		if (g == null) {
			synchronized (mutex) {
				g = server;
				if (g == null) {
					server = new MessageTranslatorImpl(null, "global");

					if (!pluginDirectory.exists()) {
						throw new IllegalArgumentException("Global translations directory must exist.");
					}
					File globalLangDir = new File(pluginDirectory, "/lang/");

					// If lang dir exists, whatever happens in there is the choice of administrators
					boolean createStartFiles = !globalLangDir.exists();

					if (createStartFiles && !globalLangDir.mkdirs()) {
						throw new IllegalStateException("Could not create /lang/ directory for global translations.");
					}
					if (createStartFiles) {
						writeResourceIfNotExists(globalLangDir, "README.txt");
						writeResourceIfNotExists(globalLangDir, "lang/global_styles.properties", "global_styles.properties");
					}

					g.setMessageStorage(new PropertiesMessageStorage(globalLangDir));
					g.setStyleStorage(new PropertiesStyleStorage(new File(globalLangDir, "global_styles.properties")));

					g.addMessages(TinyTranslations.messageFieldsFromClass(GlobalMessages.class));
					server().addMessages(messageFieldsFromClass(BukkitGlobalMessages.class));
					g.saveLocale(Locale.ENGLISH);

					writeMissingDefaultStyles();
				}
			}
		}
	}

	public static void disable() {
		audiences.close();
	}

	public static MessageTranslator application(JavaPlugin plugin) {
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

	private static void writeResourceIfNotExists(File langDir, String name) {
		writeResourceIfNotExists(langDir, name, name);
	}

	private static void writeResourceIfNotExists(File langDir, String name, String as) {
		File file = new File(langDir, as);
		if (file.exists()) {
			return;
		}
		try {
			if (!file.createNewFile()) {
				throw new IllegalStateException("Could not create resource");
			}
			InputStream is =BukkitTinyTranslations.class.getResourceAsStream("/" + name);
			if (is == null) {
				throw new IllegalArgumentException("Could not load resource with name '" + name + "'.");
			}
			FileOutputStream os = new FileOutputStream(file);
			os.write(is.readAllBytes());
			os.close();
			is.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not load resource with name '" + name + "'.", e);
		}
	}

	private static void writeMissingDefaultStyles() {
		File tempFile;
		try {
			tempFile = File.createTempFile("stream_to_file", ".properties");
			tempFile.deleteOnExit();
			try (InputStream is = BukkitTinyTranslations.class.getResourceAsStream("/lang/global_styles.properties")) {
				try (FileOutputStream out = new FileOutputStream(tempFile)) {
					out.write(is.readAllBytes());
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Could not create temp file to append missing default styles.");
		}
		MessageTranslator server = server();
		PropertiesStyleStorage storage = new PropertiesStyleStorage(tempFile);
		storage.loadStyles().forEach((s, messageStyle) -> {
			if (!server.getStyleSet().containsKey(s)) {
				server.getStyleSet().put(s, messageStyle);
			}
		});
		if (server.getStyleStorage() != null) {
			server.getStyleStorage().writeStyles(server.getStyleSet());
		}
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
	private BukkitTinyTranslations() {}
}
