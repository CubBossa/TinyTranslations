package de.cubbossa.translations.serialize;

import de.cubbossa.translations.LocalesStorage;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.StylesStorage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropertiesStorage implements StylesStorage, LocalesStorage {

    private final Logger logger;
    private final File directory;

    public PropertiesStorage(Logger logger, File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Language directory must not be a file.");
        }
        this.logger = logger;
        this.directory = directory;
    }

    private void mkDir() {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Could not create language directory");
            }
        }
    }

    private File localeFile(Locale locale) {
        mkDir();
        File file = new File(directory, locale.toLanguageTag() + ".properties");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private File stylesFile() {
        mkDir();
        return new File(directory, "styles.properties");
    }

    @Override
    public Optional<String> readMessage(Message message, Locale locale) {
        return Optional.ofNullable(readMessages(Set.of(message), locale).get(message));
    }

    @Override
    public Map<Message, String> readMessages(Collection<Message> messages, Locale locale) {
        File file = localeFile(locale);

        List<Entry> entries = readFile(file);
        Map<String, String> entryMap = new HashMap<>();
        entries.forEach(entry -> entryMap.put(entry.key(), entry.value()));

        Map<Message, String> result = new HashMap<>();
        Map<Message, String> toWrite = new HashMap<>();
        for (Message message : messages) {
            if (!entryMap.containsKey(message.getKey())) {
                String t = message.getDefaultTranslations().getOrDefault(locale, message.getDefaultValue());
                result.put(message, t);
                toWrite.put(message, t);
                continue;
            }
            result.put(message, entryMap.get(message.getKey()));
        }
        writeMessages(toWrite, locale);
        return result;
    }

    @Override
    public boolean writeMessage(Message message, Locale locale, String translation) {
        return writeMessages(Map.of(message, translation), locale).contains(message);
    }

    @Override
    public Collection<Message> writeMessages(Map<Message, String> messages, Locale locale) {
        File file = localeFile(locale);

        List<Entry> entries = readFile(file);
        for (Map.Entry<Message, String> e : messages.entrySet()) {
            entries.add(new Entry(e.getKey().getKey(), e.getValue(), e.getKey().getComment()));
        }
        entries = new ArrayList<>(new HashSet<>(entries));
        entries.sort(Comparator.comparing(o -> o.key));

        writeFile(file, entries);
        return messages.keySet();
    }

    @Override
    public Collection<TagResolver> loadStyles() {
        return null;
    }

    private void writeFile(File file, List<Entry> entries) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (Entry entry : entries) {
                if (entry.comment() != null) {
                    if (!(entry.comment().isEmpty() || entry.comment().isBlank())) {
                        for (String commentLine : entry.comment().split("\n")) {
                            writer.append("# ")
                                    .append(commentLine)
                                    .append(System.getProperty("line.separator"));
                        }
                    }
                }
                writer.append(entry.key())
                        .append("=")
                        .append("\"")
                        .append(entry.value())
                        .append("\"")
                        .append(System.getProperty("line.separator"));
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Entry> readFile(File file) {

        Pattern keyValue = Pattern.compile("([a-zA-Z.]+)=((.)+)");
        List<Entry> entries = new ArrayList<>();
        List<String> comments = new ArrayList<>();

        int lineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("# ")) {
                    comments.add(line.substring(2));
                    continue;
                }
                Matcher matcher = keyValue.matcher(line);
                if (matcher.find()) {
                    String stripped = matcher.group(2);
                    stripped = stripped.startsWith("\"") ? stripped.substring(1, stripped.length() - 1) : stripped;
                    entries.add(new Entry(matcher.group(1), stripped, String.join("\n", comments)));
                    comments.clear();
                    continue;
                }
                throw new RuntimeException("Parse error bla "); // TODO
            }
            lineIndex++;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error while parsing locale file '" + file.getAbsolutePath() + "'.", t);
            throw new RuntimeException(t);
        }
        return entries;
    }

    private record Entry(String key, String value, String comment) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(key, entry.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}
