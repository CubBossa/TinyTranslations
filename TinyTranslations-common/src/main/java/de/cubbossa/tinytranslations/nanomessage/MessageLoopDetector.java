package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.*;
import de.cubbossa.tinytranslations.persistent.MessageStorage;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageLoopDetector {

	private static final Pattern MESSAGE_PATTERN = Pattern.compile("\\{msg(:[a-zA-Z0-9-_.]+){1,2}}");
	private static final Pattern STYLE_PATTERN = Pattern.compile("<([#a-zA-Z0-9-_.]+)>");

	public MessageLoopDetector() {
	}

	public MessageReferenceLoopException detectLoops(Message message, Locale locale) {
		try {
			buildTree(message, new Stack<>(), message, locale);
		} catch (MessageReferenceLoopException e) {
			return e;
		}
		return null;
	}
	public Collection<MessageReferenceLoopException> detectLoops(Message message) {
		Collection<MessageReferenceLoopException> exceptions = new LinkedList<>();
		message.getDictionary().forEach((locale, s) -> {
			var e = detectLoops(message, locale);
			if (e != null) {
				exceptions.add(e);
			}
		});
		return exceptions;
	}

	private Node buildTree(Message origin, Stack<String> stack, Message msg, Locale locale) {
		stack.push("(msg) " + msg.getNamespacedKey());
		String s = msg.getDictionary().get(locale);
		if (s == null) {
			return new Node(null, new HashSet<>());
		}
		return buildTree(origin, stack, locale, origin.getTranslator(), s);
	}

	private Node buildTree(Message origin, Stack<String> stack, MessageStyle style, Translator translator, Locale locale) {
		stack.push("(style) " + translator.getPath() + ":" + style.getKey());
		String s = style.getStringBackup();
		return buildTree(origin, stack, locale, translator, s);
	}

	private Node buildTree(Message origin, Stack<String> stack, Locale locale, Translator t, String msg) {

		Collection<Node> references = new HashSet<>();
		Matcher styleMatcher = STYLE_PATTERN.matcher(msg);
		while (styleMatcher.find()) {
			String key = styleMatcher.group(1);
			if (stack.contains("(style) " + key)) {
				throw new MessageReferenceLoopException(origin, stack);
			}
			MessageStyle style;
			if (key.contains(":")) {
				String[] seg = key.split(":");
				style = t.getStyleByNamespace(seg[0], seg[1]);
			} else {
				style = t.getStyleInParentTree(key);
			}
			if (style != null) {
				references.add(buildTree(origin, stack, style, t, locale));
			}
		}
		Matcher msgMatcher = MESSAGE_PATTERN.matcher(msg);
		while (msgMatcher.find()) {
			String key = msgMatcher.group(1).substring(1);
			Message ref;
			if (key.contains(":")) {
				String[] seg = key.split(":");
				ref = t.getMessageByNamespace(seg[0], seg[1]);
			} else {
				ref = t.getMessageInParentTree(key);
			}
			if (ref != null) {
				if (stack.contains("(msg) " + ref.getNamespacedKey())) {
					throw new MessageReferenceLoopException(origin, stack);
				}
				references.add(buildTree(origin, stack, ref, locale));
			}
		}
		return new Node(msg, references);
	}

	private record Node(String id, Collection<Node> references) {}

}
