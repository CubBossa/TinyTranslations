package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class StyleDeserializerImpl implements StyleDeserializer {

  private static final String slotPlaceholder = "{slot}";
  private static final String argPlaceholder = "{arg%d}";
  private static final MiniMessage miniMessage = MiniMessage.miniMessage();
  private final TagResolver otherStylesResolver;

  public StyleDeserializerImpl() {
    this(TagResolver.empty());
  }

  public StyleDeserializerImpl(TagResolver otherStylesResolver) {
    this.otherStylesResolver = otherStylesResolver;
  }

  @Override
  public TagResolver deserialize(String key, String string) {
    String s = string.contains(slotPlaceholder) ? string : (string + slotPlaceholder);
    return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (c, depth) -> {
      if (depth > 0) return Component.empty();

      String mod = s;
      int i = 0;
      while (argumentQueue.hasNext()) {
        mod = mod.replace(String.format(argPlaceholder, i), argumentQueue.pop().value());
      }

      // Parse the slot content
      Component slot = context.deserialize(mod, otherStylesResolver);
      return slot.replaceText(TextReplacementConfig.builder()
          .matchLiteral(slotPlaceholder)
          .replacement(c)
          .build()).compact();
    });
  }
}
