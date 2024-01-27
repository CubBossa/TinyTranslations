package de.cubbossa.tinytranslations.storage.properties;

import de.cubbossa.tinytranslations.MessageStyle;
import de.cubbossa.tinytranslations.storage.FileEntry;
import de.cubbossa.tinytranslations.storage.StorageEntry;
import de.cubbossa.tinytranslations.storage.StyleStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertiesStyleStorage implements StyleStorage {

    private final File file;

    public PropertiesStyleStorage(File file) {
        this.file = file;
    }

    private File file() {
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
        return file;
    }

    @Override
    public void writeStyles(Map<String, MessageStyle> styles) {
        List<StorageEntry> lines = readStylesFile(file());
        Map<String, MessageStyle> present = readStylesFromLines(lines);
        Map<String, MessageStyle> toWrite = new LinkedHashMap<>();

        for (var stylePair : styles.entrySet()) {
            if (!present.containsKey(stylePair.getKey())) {
                toWrite.put(stylePair.getKey(), stylePair.getValue());
            }
        }

        toWrite.forEach((s, style) -> lines.add(new StorageEntry(s, style.toString(), style instanceof FileEntry e ? e.getComments() : Collections.emptyList())));
        writeStyles(lines);
    }

    private void writeStyles(List<StorageEntry> lines) {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            PropertiesUtils.write(writer, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, MessageStyle> loadStyles() {
        return readStylesFromLines(readStylesFile(file()));
    }

    private Map<String, MessageStyle> readStylesFromLines(List<StorageEntry> lines) {
        return lines.stream().collect(Collectors.toMap(
                StorageEntry::key,
                e -> MessageStyle.messageStyle(e.key(), e.value()),
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private List<StorageEntry> readStylesFile(File file) {
        try (Reader r = new FileReader(file, StandardCharsets.UTF_8)) {
            return PropertiesUtils.loadProperties(r);
        } catch (Throwable t) {
            throw new RuntimeException("Error while parsing locale file '" + file.getAbsolutePath() + "'.", t);
        }
    }

    private interface Line {
        String print();
    }
}
