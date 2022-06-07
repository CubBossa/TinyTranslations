package de.cubbossa.translations;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang.SerializationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class TranslationHandler {

    @Getter
    private static TranslationHandler instance;

    private final File languageDirectory;

    private final JavaPlugin plugin;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    private final List<TagResolver> globalReplacements;
    private static final Map<String, Map<String, String>> languageFormats = new HashMap<>();

    @Setter
    private boolean useClientLanguage = false;
    @Setter
    private String fallbackLanguage = Locale.US.toString();
    @Setter
    private @Nullable String loadDirectory;

    public TranslationHandler(JavaPlugin plugin, BukkitAudiences audiences, MiniMessage miniMessage, File directory, @Nullable String loadDirectory) {
        instance = this;

        this.plugin = plugin;
        this.audiences = audiences;
        this.miniMessage = miniMessage;
        this.languageDirectory = directory;
        this.globalReplacements = new ArrayList<>();
        this.loadDirectory = loadDirectory;
    }

    public void registerAnnotatedLanguageClass(Class<?> annotatedClass) throws IOException {
        MessageFile messageFile = annotatedClass.getAnnotation(MessageFile.class);
        if (messageFile == null) {
            throw new IllegalArgumentException("To load a class as message class, it has to be annotated with MessageFile.class.");
        }
        File file = new File(languageDirectory, messageFile.languageString() + ".yml");
        boolean fileExists = file.exists();
        if (!fileExists) {
            if (!file.createNewFile()) {
                throw new RuntimeException("An error occurred while creating file '" + file.getAbsolutePath() + "'.");
            }
        }

        Field[] messages = Arrays.stream(annotatedClass.getDeclaredFields())
                .filter(field -> field.getDeclaringClass().equals(Message.class))
                .filter(field -> field.getAnnotation(MessageMeta.class) != null)
                .toArray(Field[]::new);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        List<String> comments;
        for (MessageGroupMeta meta : annotatedClass.getAnnotationsByType(MessageGroupMeta.class)) {
            comments = Arrays.stream(meta.comment()).collect(Collectors.toCollection(ArrayList::new));
            comments.add("Valid placeholders: " + Arrays.stream(meta.placeholders())
                    .map(s -> "<" + s + ">")
                    .collect(Collectors.joining(", ")));
            cfg.set(meta.path(), comments);
        }

        for (Field messageField : messages) {
            try {
                Message message = (Message) messageField.get(annotatedClass);
                MessageMeta value = messageField.getAnnotation(MessageMeta.class);
                MessageGroupMeta[] groupMeta = messageField.getAnnotationsByType(MessageGroupMeta.class);

                if (fileExists && cfg.isSet(message.getKey())) {
                    continue;
                }

                comments = Arrays.stream(value.comment()).collect(Collectors.toCollection(ArrayList::new));
                comments.add("Valid placeholders: " + Arrays.stream(value.placeholders())
                        .map(s -> "<" + s + ">")
                        .collect(Collectors.joining(", ")));
                cfg.setComments(message.getKey(), comments);
                cfg.set(message.getKey(), value.value());

                for (MessageGroupMeta meta : groupMeta) {

                    comments = Arrays.stream(meta.comment()).collect(Collectors.toCollection(ArrayList::new));
                    comments.add("Valid placeholders: " + Arrays.stream(meta.placeholders())
                            .map(s -> "<" + s + ">")
                            .collect(Collectors.joining(", ")));
                    cfg.set(meta.path(), comments);
                }

            } catch (Exception e) {
                throw new SerializationException("Could not write message '" + messageField.getName() + "' to file. Skipping.");
            }
        }
        cfg.save(file);
    }

    private Tag insertMessage(ArgumentQueue argumentQueue, Context context, Audience audience) {
        final String messageKey = argumentQueue.popOr("The message tag requires a message key, like <message:error.no_permission>.").value();
        return Tag.inserting(new Message(messageKey).asComponent(audience));
    }

    public void loadLanguages(Locale... createItAbsent) throws IOException {

        if (!languageDirectory.exists()) {
            languageDirectory.mkdir();
        }
        File[] files = languageDirectory.listFiles();
        if (files == null) {
            throw new NullPointerException("Language directory was null: " + languageDirectory.getAbsolutePath());
        }
        List<File> filesToLoad = new ArrayList<>();
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            filesToLoad.add(file);
        }
        for (Locale l : createItAbsent) {
            filesToLoad.add(new File(plugin.getDataFolder(), (loadDirectory != null ? loadDirectory + (loadDirectory.endsWith("/") ? "" : "/") : "") + l.toString() + ".yml"));
        }
        filesToLoad.stream().distinct().forEach(file -> {
            try {
                loadLanguage(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load File: " + file.getAbsolutePath(), e);
            }
        });
    }

    public void loadLanguage(final File languageFile) throws IOException {
        File file = languageFile;
        String languageKey = file.getName().replace(".yml", "");

        String fileName = (loadDirectory != null ? loadDirectory + (loadDirectory.endsWith("/") ? "" : "/") : "") + file.getName();

        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            file = new File(plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                throw new IOException("Could not create language file: " + fileName);
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
        globalReplacements.add(resolver);
    }

    public void unregisterTagResolver(TagResolver tagResolver) {
        globalReplacements.remove(tagResolver);
    }

    private String getMiniMessageFormat(Message message, String lang) {
        return languageFormats.getOrDefault(message.getKey(), new HashMap<>()).getOrDefault(lang, "missing:" + lang + "-" + message.getKey());
    }

    public Component translateLine(Message message, Player player, TagResolver... tagResolvers) {
        return translateLine(getMiniMessageFormat(message, getLanguage(player)), player, tagResolvers);
    }

    public Component translateLine(Message message, Audience audience, TagResolver... tagResolvers) {
        return translateLine(getMiniMessageFormat(message, getLanguage(audience)), audience, tagResolvers);
    }

    public List<Component> translateLines(Message message, Player player, TagResolver... tagResolvers) {
        return translateLines(getMiniMessageFormat(message, getLanguage(player)), player, tagResolvers);
    }

    public List<Component> translateLines(Message message, Audience audience, TagResolver... tagResolvers) {
        return translateLines(getMiniMessageFormat(message, getLanguage(audience)), audience, tagResolvers);
    }


    public Component translateLine(String message, Player player, TagResolver... tagResolvers) {
        return translateLine(message, audiences.player(player), tagResolvers);
    }

    public Component translateLine(String message, Audience audience, TagResolver... tagResolvers) {

        List<TagResolver> t = Lists.newArrayList(tagResolvers);
        t.addAll(globalReplacements);
        t.add(TagResolver.builder().tag("message", (argumentQueue, context) -> insertMessage(argumentQueue, context, audience)).build());

        return miniMessage.deserialize(message, t.toArray(TagResolver[]::new));
    }

    public List<Component> translateLines(String message, Player player, TagResolver... tagResolvers) {
        return translateLines(message, audiences.player(player), tagResolvers);
    }

    public List<Component> translateLines(String message, Audience audience, TagResolver... tagResolvers) {

        List<TagResolver> t = new ArrayList<>(List.of(tagResolvers));
        t.addAll(globalReplacements);
        t.add(TagResolver.builder().tag("message", (argumentQueue, context) -> insertMessage(argumentQueue, context, audience)).build());
        TagResolver[] resolvers = t.toArray(TagResolver[]::new);

        String[] toFormat = message.split("\n");
        List<Component> result = Arrays.stream(toFormat).map(s -> miniMessage.deserialize(s, resolvers)).collect(Collectors.toList());

        // If all lines are empty, return empty list. Allows to have an empty lore.
        if (result.stream().allMatch(component -> component.equals(Component.text("")))) {
            return new ArrayList<>();
        }
        return result;
    }

    public String getLanguage(Audience audience) {
        if (useClientLanguage) {
            return audience.getOrDefault(Identity.LOCALE, Locale.US).getLanguage();
        }
        return fallbackLanguage;
    }

    public String getLanguage(Player player) {
        if (useClientLanguage) {
            return player.getLocale();
        }
        return fallbackLanguage;
    }
}
