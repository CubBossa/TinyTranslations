package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.Translator;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;
import java.util.Map;

public class StyleTag {
	public static final String KEY = "style";

	public static NanoResolver resolver(Translator translator) {
		return context -> {
			Map<String, TagResolver> styles = new HashMap<>();

			Translator t = translator;
			while (t != null) {
				t.getStyleSet().forEach((key, value) -> {
					if (styles.containsKey(key)) return;
					styles.put(key, value.getResolver().apply(context));
				});
				t = t.getParent();
			}
			styles.put(KEY, TagResolver.resolver(KEY, (argumentQueue, c) -> {
				String styleKey = argumentQueue.popOr("A style tag requires a specified style").value();
				if (argumentQueue.hasNext()) {
					String namespace = styleKey;
					styleKey = argumentQueue.pop().value();
					return translator.getStyleByNamespace(namespace, styleKey).getResolver().apply(context).resolve(styleKey, argumentQueue, c);
				}
				return styles.get(styleKey).resolve(styleKey, argumentQueue, c);
			}));
			return TagResolver.resolver(styles.values());
		};
	}
}
