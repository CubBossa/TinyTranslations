package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.TranslationsPreprocessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;

public class StyleDeserializerImpl implements StyleDeserializer {

  private static final TranslationsPreprocessor PREPROCESSOR = new TranslationsPreprocessor();

  private static final String slotPlaceholder = "<slot/>";

  public StyleDeserializerImpl() {
  }

  @Override
  public TagResolver deserialize(String key, @Language("NanoMessage") String string) {
    string = PREPROCESSOR.apply(string);
    String s = string.contains(slotPlaceholder) ? string : (string + slotPlaceholder);
    return TagResolver.resolver(key, (argumentQueue, context) -> (Modifying) (c, depth) -> {
      if (depth > 0) return Component.empty();
      return context.deserialize(s, Placeholder.component("slot", c));
    });
  }
}
