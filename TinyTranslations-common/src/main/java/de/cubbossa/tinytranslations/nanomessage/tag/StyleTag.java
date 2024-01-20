package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.MessageTranslator;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;
import java.util.Map;

public class StyleTag {
	public static final String KEY = "style";

	public static TagResolver resolver(MessageTranslator messageTranslator) {
		Map<String, TagResolver> styles = new HashMap<>();

		MessageTranslator t = messageTranslator;
		while (t != null) {
			t.getStyleSet().forEach((key, value) -> {
				if (styles.containsKey(key)) return;
				styles.put(key, value);
			});
			t = t.getParent();
		}
		styles.put(KEY, TagResolver.resolver(KEY, (q, c) -> {
			String styleKey = q.popOr("A style tag requires a specified style").value();
			if (q.hasNext()) {
				String namespace = styleKey;
				styleKey = q.pop().value();
				return messageTranslator.getStyleByNamespace(namespace, styleKey).resolve(styleKey, q, c);
			}
			return styles.get(styleKey).resolve(styleKey, q, c);
		}));
		return TagResolver.resolver(styles.values());
	}
}
