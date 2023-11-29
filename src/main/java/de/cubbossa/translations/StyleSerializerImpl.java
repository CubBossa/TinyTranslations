package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.LinkedList;
import java.util.Queue;

public class StyleSerializerImpl implements StyleSerializer {

  private static final String slotPlaceholder = "{slot}";
  private static final MiniMessage miniMessage = MiniMessage.miniMessage();
  private final TagResolver otherStylesResolver;

  public StyleSerializerImpl() {
    this(TagResolver.empty());
  }

  public StyleSerializerImpl(TagResolver otherStylesResolver) {
    this.otherStylesResolver = otherStylesResolver;
  }

  @Override
  public TagResolver deserialize(String key, String string) {
    return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (c, depth) -> {
      if (depth > 0) return Component.empty();

      // Parse the slot content
      Component slot = miniMessage.deserialize(string, otherStylesResolver);
      return slot.replaceText(TextReplacementConfig.builder()
          .matchLiteral(slotPlaceholder)
          .replacement(c)
          .build()).compact();
    });
  }
}
