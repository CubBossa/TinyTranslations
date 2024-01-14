package de.cubbossa.tinytranslations.nanomessage.tag;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.Translator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MessageTag {
	public static final String KEY = "msg";

	public static NanoResolver resolver(Translator translator) {
		return context -> TagResolver.resolver(KEY, (queue, ctx) -> {
			String nameSpace;
			String key = queue.popOr("The message tag requires a message key, like <msg:error.no_permission/>.").value();
			if (queue.hasNext()) {
				nameSpace = key;
				key = queue.pop().value();
			} else {
				Message msg = translator.getMessageInParentTree(key);
				if (msg == null) {
					return Tag.inserting(Component.text("<msg-not-found:" + key + "/>"));
				}
				return Tag.inserting(context.process(msg));
			}
			Message msg = translator.getMessageByNamespace(nameSpace, key);
			if (msg == null) {
				return Tag.inserting(Component.text("<msg-not-found:" + nameSpace + ":" + key + "/>"));
			}
			return Tag.inserting(context.process(msg));
		});
	}

}
