package de.cubbossa.translations.persistent;

import de.cubbossa.translations.MessageStyle;
import de.cubbossa.translations.MessageStyleImpl;
import de.cubbossa.translations.StyleSerializer;
import de.cubbossa.translations.StyleSerializerImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropertiesStyleStorage implements StyleStorage {

    private final File file;
    private final StyleSerializer styleSerializer;

    public PropertiesStyleStorage(File file) {
        this(file, new StyleSerializerImpl());
    }

    public PropertiesStyleStorage(File file, StyleSerializer styleSerializer) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
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
        this.styleSerializer = styleSerializer;
    }

    @Override
    public void writeStyles(Map<String, MessageStyle> styles) {
        List<Line> lines = readStylesFile(file);
        Map<String, MessageStyle> present = readStylesFromLines(lines);
        Map<String, MessageStyle> toWrite = new LinkedHashMap<>();

        for (var stylePair : styles.entrySet()) {
            if (!present.containsKey(stylePair.getKey())) {
                toWrite.put(stylePair.getKey(), stylePair.getValue());
            }
        }

        toWrite.forEach((s, style) -> lines.add(new StyleLine(s, style)));
        writeStyles(lines);
    }

    private void writeStyles(List<Line> lines) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));

            for (Line line : lines) {
                writer.append(line.print()).append(System.getProperty("line.separator"));
            }

            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, MessageStyle> loadStyles() {
        return readStylesFromLines(readStylesFile(file));
    }

    private Map<String, MessageStyle> readStylesFromLines(List<Line> lines) {
        return lines.stream()
                .filter(line -> line instanceof StyleLine)
                .map(line -> (StyleLine) line)
                .collect(Collectors.toMap(StyleLine::key, StyleLine::style));
    }

    private List<Line> readStylesFile(File file) {
        Pattern keyValue = Pattern.compile("^([a-zA-Z._-]+)=((.)+)$");
        List<Line> lines = new ArrayList<>();

        int lineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineIndex++;
                if (line.isEmpty() || line.isBlank()) {
                    lines.add(new EmptyLine());
                    continue;
                }
                if (line.startsWith("# ")) {
                    lines.add(new CommentLine(line.substring(2)));
                    continue;
                }
                if (line.startsWith("#")) {
                    lines.add(new CommentLine(line.substring(1)));
                    continue;
                }
                Matcher matcher = keyValue.matcher(line);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    String stripped = matcher.group(2);
                    stripped = stripped.startsWith("\"") ? stripped.substring(1, stripped.length() - 1) : stripped;
                    stripped = stripped.replace("\\n", "\n");

                    lines.add(new StyleLine(key, new MessageStyleImpl(key, styleSerializer.deserialize(key, stripped), stripped)));
                    continue;
                }
                Logger.getLogger("Translations").log(Level.SEVERE, "Error while parsing line " + lineIndex++ + " of " + file.getName() + ".\n > '" + line + "'");
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error while parsing locale file '" + file.getAbsolutePath() + "'.", t);
        }
        return lines;
    }

    private interface Line {
        String print();
    }

    private record EmptyLine() implements Line {
        @Override
        public String print() {
            return "";
        }
    }

    private record CommentLine(String comment) implements Line {
        @Override
        public String print() {
            return "# " + comment;
        }
    }

    private record StyleLine(String key, MessageStyle style) implements Line {
        @Override
        public String print() {
            return key + "=\"" + style.getStringBackup() + "\"";
        }
    }
}
