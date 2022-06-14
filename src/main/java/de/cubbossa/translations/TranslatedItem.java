package de.cubbossa.translations;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
public class TranslatedItem {

	private final ItemStack stack;
	private final Message name;
	private TagResolver[] nameResolver = new TagResolver[0];
	private final Message lore;
	private TagResolver[] loreResolver = new TagResolver[0];

	public TranslatedItem(Material material, Message name, Message lore) {
		this.stack = new ItemStack(material);
		this.name = name;
		this.lore = lore;
	}

	public TranslatedItem(Material material, Message name, Message lore, TagResolver[] nameResolver, TagResolver[] loreResolver) {
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
		PacketTranslationHandler translator = PacketTranslationHandler.getInstance();

		NBTItem item = new NBTItem(stack);
		NBTCompound display = item.getCompound("display");
		if (name != null) {
			display.setString("Name", PacketTranslationHandler.SERIALIZER.serialize(TranslationHandler.getInstance().toTranslatable(name, nameResolver)));
		}

		if (lore != null) {
			List<String> loreList = display.getStringList("Lore");
			loreList.clear();
			loreList.add(PacketTranslationHandler.SERIALIZER.serialize(TranslationHandler.getInstance().toTranslatable(lore, loreResolver)));
		}

		if (translator.getCounter().get() >= Integer.MAX_VALUE - 3) {
			translator.getCounter().set(1);
		}
		return item.getItem();
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

		public TranslatedItem build() {
			return new TranslatedItem(stack, name, nameResolvers.toArray(TagResolver[]::new), lore, loreResolvers.toArray(TagResolver[]::new));
		}

		public ItemStack createItem() {
			return build().createItem();
		}
	}
}
