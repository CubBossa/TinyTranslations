package de.cubbossa.translations;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PacketTranslationHandler {


	@Getter
	private static PacketTranslationHandler instance;

	public static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.builder().build();
	private static final String REGEX = ".*\\{\"translate\":\"§:([^>]+):([0-9]+)\"}.*";
	private static final String REGEX_INVENTORY_TITLES = ".*\\{\"text\":\"§:([^>]+):([0-9]+)\"}.*";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	private static final Pattern PATTERN_INVENTORY_TITLES = Pattern.compile(REGEX_INVENTORY_TITLES);

	@Getter
	@Setter
	private AtomicInteger counter = new AtomicInteger(1);
	@Getter
	private final Map<Integer, TagResolver[]> resolvers = new HashMap<>();
	@Getter
	private final Collection<UUID> whitelist = new HashSet<>();


	public PacketTranslationHandler(Plugin plugin) {
		instance = this;

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
				ListenerPriority.NORMAL,
				PacketType.Play.Server.WINDOW_ITEMS) {


			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {

					if (whitelist.contains(event.getPlayer().getUniqueId())) {
						return;
					}

					PacketContainer packet = event.getPacket();

					List<ItemStack> list = packet.getItemListModifier().read(0);
					for (int i = 0; i < list.size(); i++) {
						list.set(i, translateStack(list.get(i), event.getPlayer()));
						packet.getItemListModifier().write(0, list);
					}
				}
			}
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
				ListenerPriority.NORMAL,
				PacketType.Play.Server.SET_SLOT) {

			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {

					if (whitelist.contains(event.getPlayer().getUniqueId())) {
						return;
					}

					PacketContainer packet = event.getPacket();
					packet.getItemModifier().write(0, translateStack(packet.getItemModifier().read(0), event.getPlayer()));
				}
			}
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
				ListenerPriority.NORMAL,
				PacketType.Play.Server.OPEN_WINDOW) {

			@Override
			public void onPacketSending(PacketEvent event) {
				if (whitelist.contains(event.getPlayer().getUniqueId())) {
					return;
				}

				PacketContainer packet = event.getPacket();
				String json = packet.getChatComponents().read(0).getJson();
				packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json(json, event.getPlayer(), PATTERN_INVENTORY_TITLES)));
			}
		});
	}

	public void whitelist(Player player) {
		whitelist.add(player.getUniqueId());
	}

	public void removeFromWhitelist(Player player) {
		whitelist.remove(player.getUniqueId());
	}

	public static String format(String messageKey, int placeHolder) {
		return String.format("§:%s:%d", messageKey, placeHolder);
	}

	private ItemStack translateStack(ItemStack stack, Player player) {
		if (stack.getType() == Material.AIR) {
			return stack;
		}

		NBTItem item = new NBTItem(stack);
		if (item.getCompound("display") == null) {
			return stack;
		}
		NBTCompound display = item.getCompound("display");

		if (display.hasKey("Name")) {
			String name = display.getString("Name");
			display.setString("Name", json(name, player, PATTERN));
		}

		if (display.hasKey("Lore")) {
			List<String> list = display.getStringList("Lore");

			int currentIndex = 0;
			for (String loreLine : new ArrayList<>(display.getStringList("Lore"))) {
				List<String> lines = jsonList(loreLine, player, PATTERN, "\n");
				list.set(currentIndex++, lines.size() < 1 ? "" : lines.get(0));
				for (int i = 1; i < lines.size(); i++) {
					list.add(currentIndex++, lines.get(i));
				}
			}
		}
		return item.getItem();
	}

	private String json(String string, Player player, Pattern pattern) {
		String json = string;

		Matcher matcher = pattern.matcher(json);
		while (matcher.find()) {
			String messageKey = matcher.group(1);
			String resolverIdString = matcher.group(2);
			int resolverId = Integer.parseInt(resolverIdString);

			TagResolver[] resolver = resolverIdString.equals("0") ? new TagResolver[0] : resolvers.getOrDefault(resolverId, new TagResolver[0]);

			json = matcher.replaceFirst(SERIALIZER.serialize(TranslationHandler.getInstance()
					.translateLine(new Message(messageKey), player, resolver).decoration(TextDecoration.ITALIC, false)));
			resolvers.remove(resolverId);
		}
		return json;
	}

	private List<String> jsonList(String string, Player player, Pattern pattern, String linebreak) {
		String json = string;
		List<String> parsed = new ArrayList<>();

		Matcher matcher = pattern.matcher(json);
		while (matcher.find()) {
			String messageKey = matcher.group(1);
			String resolverIdString = matcher.group(2);
			int resolverId = Integer.parseInt(resolverIdString);

			TagResolver[] resolver = resolverIdString.equals("0") ? new TagResolver[0] : resolvers.getOrDefault(resolverId, new TagResolver[0]);

			parsed.addAll(TranslationHandler.getInstance().translateLines(new Message(messageKey), player, resolver)
					.stream().map(c -> c.decoration(TextDecoration.ITALIC, false))
					.map(SERIALIZER::serialize)
					.collect(Collectors.toList()));

			resolvers.remove(resolverId);
		}
		return parsed;
	}
}
