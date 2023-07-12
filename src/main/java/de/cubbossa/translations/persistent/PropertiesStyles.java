package de.cubbossa.translations.persistent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropertiesStyles implements StylesStorage {

    private final File file;

    public PropertiesStyles(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Throwable t) {
                throw new IllegalStateException("Could not create properties file.", t);
            }
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("PropertiesStyles requires a properties file as argument");
        }
        if (!file.getName().endsWith(".properties")) {
            throw new IllegalArgumentException("PropertiesStyles requires a properties file as argument");
        }
        this.file = file;
    }

    @Override
    public void writeStyles(Map<String, Style> styles) {

    }

    @Override
    public Map<String, Style> loadStyles() {
        if (!file.exists()) {
            try {
                file.mkdirs();
                file.createNewFile();
                if (!file.exists()) {
                    throw new IllegalStateException("Could not create styles properties file.");
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        Pattern keyValue = Pattern.compile("([a-zA-Z._-]+)=((.)+)");

        Map<String, String> props = new LinkedHashMap<>();

        int lineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineIndex++;
                if (line.isEmpty() || line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                Matcher matcher = keyValue.matcher(line);
                if (matcher.find()) {
                    String stripped = matcher.group(2);
                    stripped = stripped.startsWith("\"") ? stripped.substring(1, stripped.length() - 1) : stripped;
                    stripped = stripped.replace("\\n", "\n");
                    props.put(matcher.group(1), stripped);
                    continue;
                }
                throw new RuntimeException("Error while parsing line " + lineIndex++ + " of " + file.getName() + ".\n > '" + line + "'");
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Map<String, Style> styles = new LinkedHashMap<>();
        Collection<TagResolver> resolvers = new ArrayList<>();

        for (Map.Entry<String, String> e : props.entrySet()) {
            Component styleHolder = miniMessage.deserialize(e.getValue(), resolvers.toArray(TagResolver[]::new));
            resolvers.add(TagResolver.resolver(e.getKey(), Tag.styling(style -> style.merge(styleHolder.style()))));
            styles.put(e.getKey(), styleHolder.style());
        }
        return styles;
    }
}
