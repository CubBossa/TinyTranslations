package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.LinkedList;
import java.util.Queue;

public class StyleSerializerImpl implements StyleSerializer {

  private final Translations translations;

  public StyleSerializerImpl(Translations translations) {
    this.translations = translations;
  }

  @Override
  public String serialize(Style style) {
    return null;
  }

  @Override
  public TagResolver deserialize(String key, String string) {
    return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (c, depth) -> {
      if (depth > 0) return Component.empty();

      // Parse the slot content
      Component slot = translations.process(string);
      return c.replaceText(TextReplacementConfig.builder()
          .matchLiteral("{slot}")
          .replacement(slot)
          .build());
    });
  }
}
