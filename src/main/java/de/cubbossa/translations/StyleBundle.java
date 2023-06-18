package de.cubbossa.translations;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface StyleBundle {

  Map<String, Style> getStyles();

  TagResolver getStylesResolver();

  void addStyle(String key, Style style);

  void removeStyle(String key);
}
