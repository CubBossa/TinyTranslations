package de.cubbossa.tinytranslations.impl;

import de.cubbossa.tinytranslations.StyleDeserializer;
import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
import de.cubbossa.tinytranslations.nanomessage.tag.NanoResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Language;

public class StyleDeserializerImpl implements StyleDeserializer {

  private static final NanoMessageCompiler PREPROCESSOR = new NanoMessageCompiler();

  private static final String slotPlaceholder = "<slot/>";

  public StyleDeserializerImpl() {
  }

  @Override
  public NanoResolver deserialize(String key, @Language("NanoMessage") String string) {
    string = PREPROCESSOR.compile(string);
    String s = string.contains(slotPlaceholder) ? string : (string + slotPlaceholder);
    return context -> TagResolver.resolver(key, (argumentQueue, ctx) -> (Modifying) (c, depth) -> {
	  if (depth > 0) return Component.empty();
	  return context.process(s, Placeholder.component("slot", c));
	});
  }
}
