package de.cubbossa.translations;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@AllArgsConstructor
public class MenuIcon {

	private static int counter = 1;
	public static final Map<Integer, TagResolver[]> resolvers = new HashMap<>();

	private final ItemStack stack;
	private final Message name;
	private TagResolver[] nameResolver = new TagResolver[0];
	private final Message lore;
	private TagResolver[] loreResolver = new TagResolver[0];

	public MenuIcon(Material material, Message name, Message lore) {
		this.stack = new ItemStack(material);
		this.name = name;
		this.lore = lore;
	}

	public MenuIcon(Material material, Message name, Message lore, TagResolver[] nameResolver, TagResolver[] loreResolver) {
		this.stack = new ItemStack(material);
		this.name = name;
		this.lore = lore;
		this.nameResolver = nameResolver;
		this.loreResolver = loreResolver;
	}

	public synchronized ItemStack createItem() {

		if (stack.getType() == Material.AIR) {
			return stack.clone();
		}
		ItemMeta meta = stack.getItemMeta();

		if (meta == null) {
			meta = Bukkit.getItemFactory().getItemMeta(stack.getType());
			if (meta == null) {
				throw new RuntimeException("Could not create menu icon, no meta provided.");
			}
		}
		if (name != null) {
			int nameId = 0;
			if (nameResolver != null && nameResolver.length > 0) {
				nameId = counter++;
				resolvers.put(nameId, nameResolver);
			}
			meta.setDisplayName(PacketTranslationHandler.format(name.getKey().replace(".", "$"), nameId));
		}

		if (lore != null) {
			int loreId = 0;
			if (loreResolver != null && loreResolver.length > 0) {
				loreId = counter++;
				resolvers.put(loreId, loreResolver);
			}
			meta.setLore(Lists.newArrayList(PacketTranslationHandler.format(lore.getKey().replace(".", "$"), loreId)));
		}

		if (counter >= Integer.MAX_VALUE - 3) {
			counter = 1;
		}

		ItemStack stack = this.stack.clone();
		stack.setItemMeta(meta);
		return stack;
	}

	public static class Builder {

		private final ItemStack stack;
		private Message name = null;
		private Message lore = null;
		private final List<TagResolver> nameResolvers = new ArrayList<>();
		private final List<TagResolver> loreResolvers = new ArrayList<>();

		public Builder(ItemStack stack) {
			this.stack = stack;
		}

		public Builder withName(Message message) {
			this.name = message;
			if (message instanceof FormattedMessage formatted) {
				this.nameResolvers.addAll(List.of(formatted.getResolvers()));
			}
			return this;
		}

		public Builder withLore(Message message) {
			this.lore = message;
			if (message instanceof FormattedMessage formatted) {
				this.loreResolvers.addAll(List.of(formatted.getResolvers()));
			}
			return this;
		}

		public Builder withNameResolver(TagResolver resolver) {
			this.nameResolvers.add(resolver);
			return this;
		}

		public Builder withLoreResolver(TagResolver resolver) {
			this.loreResolvers.add(resolver);
			return this;
		}

		public MenuIcon build() {
			return new MenuIcon(stack, name, nameResolvers.toArray(TagResolver[]::new), lore, loreResolvers.toArray(TagResolver[]::new));
		}

		public ItemStack createItem() {
			return build().createItem();
		}
	}
}
