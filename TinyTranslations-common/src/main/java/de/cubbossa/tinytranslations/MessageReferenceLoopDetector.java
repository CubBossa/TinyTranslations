package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReferenceLoopDetector {

	private static final Pattern MESSAGE_PATTERN = Pattern.compile("\\{msg(:[a-zA-Z0-9-_.]+){1,2}}");
	private static final Pattern STYLE_PATTERN = Pattern.compile("<([#a-zA-Z0-9-_.]+)>");

	public MessageReferenceLoopDetector() {
	}

	private MessageTranslator findOwner(Message message) {
		for (Translator source : GlobalTranslator.translator().sources()) {
			if (!(source instanceof MessageTranslator messageTranslator)) {
				return null;
			}
			if (messageTranslator.getMessage(message.getKey()) != null) {
				return messageTranslator;
			}
		}
		return null;
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
		stack.push("(msg) " + msg.key());
		String s = msg.getDictionary().get(locale);
		if (s == null) {
			return new Node(null, new HashSet<>());
		}
		return buildTree(origin, stack, locale, findOwner(origin), s);
	}

	private Node buildTree(Message origin, Stack<String> stack, MessageStyle style, MessageTranslator messageTranslator, Locale locale) {
		stack.push("(style) " + messageTranslator.getPath() + ":" + style.getKey());
		String s = style.toString();
		return buildTree(origin, stack, locale, messageTranslator, s);
	}

	private Node buildTree(Message origin, Stack<String> stack, Locale locale, MessageTranslator t, String msg) {

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
				if (stack.contains("(msg) " + ref.translationKey())) {
					throw new MessageReferenceLoopException(origin, stack);
				}
				references.add(buildTree(origin, stack, ref, locale));
			}
		}
		return new Node(msg, references);
	}

	private record Node(String id, Collection<Node> references) {}

}
