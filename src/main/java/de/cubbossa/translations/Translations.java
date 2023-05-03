package de.cubbossa.translations;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Setter
public class Translations implements Translator {

	private static Translations instance;

	public static Translations get() {
		return instance;
	}

	public static PluginTranslations create(String name) {


		return new DefaultPluginTranslations();
	}

	private final AudienceProvider audiences;
	private final MiniMessage miniMessage;
	private final File directory;
	private Logger logger;

	private final Map<String, DefaultPluginTranslations> applicationMap;
	private final Collection<TagResolver> globalResolvers;

	public Translations(AudienceProvider audiences, MiniMessage miniMessage, File directory) {
		instance = this;

		this.audiences = audiences;
		this.miniMessage = miniMessage;

		this.directory = directory;
		this.directory.mkdirs(); //TODO

		this.applicationMap = new HashMap<>();
		this.globalResolvers = new ArrayList<>();
	}

	public void registerAnnotatedLanguageClass(Class<?> annotatedClass) throws IOException {
		Field[] messages = Arrays.stream(annotatedClass.getDeclaredFields())
				.filter(field -> field.getType().equals(Message.class))
				.toArray(Field[]::new);

		for (Field messageField : messages) {
			try {
				Message message = (Message) messageField.get(annotatedClass);

				if (fileExists && cfg.isSet(message.getKey())) {
					continue;
				}

				comments = Arrays.stream(value.comment()).collect(Collectors.toCollection(ArrayList::new));
				if (value.placeholders().length > 0) {
					comments.add("Valid placeholders: " + Arrays.stream(value.placeholders())
							.map(s -> "<" + s + ">")
							.collect(Collectors.joining(", ")));
				}
				cfg.set(message.getKey(), value.value());
				cfg.setComments(message.getKey(), comments);

				for (MessageGroupMeta meta : groupMeta) {

					comments = Arrays.stream(meta.comment()).collect(Collectors.toCollection(ArrayList::new));
					if (value.placeholders().length > 0) {
						comments.add("Valid placeholders: " + Arrays.stream(meta.placeholders())
								.map(s -> "<" + s + ">")
								.collect(Collectors.joining(", ")));
					}
					cfg.set(meta.path(), comments);
				}

			} catch (Exception e) {
				throw new RuntimeException("Could not write message '" + messageField.getName() + "' to file. Skipping.");
			}
		}
		cfg.save(file);
	}

	private Tag insertPreMessage(ArgumentQueue argumentQueue, Context context, Audience audience) {
		final String messageKey = argumentQueue.popOr("The message tag requires a message key, like <message:error.no_permission>.").value();
		return Tag.preProcessParsed(getMiniMessageFormat(new Message(messageKey), getLanguage(audience)));
	}

	private Tag insertMessage(ArgumentQueue argumentQueue, Context context, Audience audience, TagResolver... resolvers) {
		final String messageKey = argumentQueue.popOr("The message tag requires a message key, like <message:error.no_permission>.").value();
		return Tag.inserting(translateLine(new Message(messageKey), audience, resolvers));
	}

	public void loadStyle() {

		if (!directory.exists()) {
			directory.mkdir();
		}
		File stylesFile = new File(directory, "styles.yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(stylesFile);
		var entries = cfg.getValues(false).entrySet().stream().filter(e -> e.getValue() instanceof String).toList();
		if (cfg.getValues(true).size() != entries.size()) {
			logger.log(Level.SEVERE, "Style files can only have top-level string values, like: 'key: \"value\"'. Some values are being ignored.");
			return;
		}
		Collection<TagResolver> resolvers = new ArrayList<>();
		entries.forEach(entry -> {
			// Append any char to the tags to assure that colors are parsed properly.
			// It won't be visible anyways because we only use the style of this component.
			Component styleHolder = miniMessage.deserialize(entry.getValue() + "a");
			resolvers.add(TagResolver.resolver(entry.getKey(), (queue, context) -> Tag.styling(style -> style.merge(styleHolder.style()))));
		});
		globalResolvers.addAll(resolvers);
	}

	public void loadLanguages() throws IOException {

		if (!directory.exists()) {
			directory.mkdir();
		}
		File[] files = directory.listFiles();
		if (files == null) {
			throw new NullPointerException("Language directory was null: " + directory.getAbsolutePath());
		}
		Arrays.stream(files).filter(file -> file.getName().endsWith(".yml")).forEach(file -> {
			try {
				loadLanguage(file);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not load File: " + file.getAbsolutePath(), e);
			}
		});
	}

	public void loadLanguage(final File languageFile) throws IOException {
		File file = languageFile;
		String languageKey = file.getName().replace(".yml", "");

		if (!file.exists()) {
			plugin.saveResource(languageKey + ".yml", false);
			file = new File(directory, languageKey + ".yml");
			if (!file.exists()) {
				throw new IOException("Could not create language file: " + languageKey);
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		Map<String, Object> map = cfg.getValues(true);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() instanceof String s) {

				Map<String, String> translations = languageFormats.getOrDefault(entry.getKey(), new HashMap<>());
				translations.put(languageKey, s);
				languageFormats.put(entry.getKey(), translations);
			}
		}
	}

	public void registerTagResolver(TagResolver resolver) {
		globalResolvers.add(resolver);
	}

	public void unregisterTagResolver(TagResolver tagResolver) {
		globalResolvers.remove(tagResolver);
	}

	public Component translateLine(String message, @Nullable Audience audience, TagResolver... tagResolvers) {

		List<TagResolver> t = Lists.newArrayList(tagResolvers);
		t.addAll(globalResolvers);
		BiFunction<ArgumentQueue, Context, Tag> function = (argumentQueue, context) -> insertMessage(argumentQueue, context, audience, tagResolvers);
		t.add(TagResolver.builder().tag("message", function).build());
		t.add(TagResolver.builder().tag("msg", function).build());
		t.add(TagResolver.builder().tag("ins", (argumentQueue, context) -> insertPreMessage(argumentQueue, context, audience)).build());

		return miniMessage.deserialize(message, t.toArray(TagResolver[]::new));
	}

	private Optional<PluginTranslations> getApplicationFromKey(Message message) {
		return Optional.ofNullable(applicationMap.get(message.getKey().split("\\.")[0]));
	}

	@Override
	public String translate(Message message) {
		return getApplicationFromKey(message).orElseThrow().translate(message);
	}

	@Override
	public String translate(Message message, Audience audience) {
		return getApplicationFromKey(message).orElseThrow().translate(message, audience);
	}
}
