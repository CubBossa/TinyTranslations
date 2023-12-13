package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageCore;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesMessageStorage extends FileMessageStorage implements MessageStorage {

    private static final Charset[] CHARSETS = { StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1 };

    public PropertiesMessageStorage(File directory) {
        this(directory, "", "");
    }

    public PropertiesMessageStorage(File directory, String prefix, String suffix) {
        super(directory, prefix, suffix + ".properties");
    }

    @Override
    public Map<Message, String> readMessages(Locale locale) {
        File file = localeFileIfExists(locale);
        if (file == null) {
            return new HashMap<>();
        }

        Map<String, Entry> entries = readFile(file);
        Map<Message, String> result = new HashMap<>();
        entries.forEach((key, value) -> result.put(new MessageCore(key), value.value()));
        return result;
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
        File file = localeFile(locale);

        Collection<Message> written = new HashSet<>();
        Map<String, Entry> entries = readFile(file);
        for (Message msg : messages) {
            if (msg.getDictionary().containsKey(locale)) {
                if (entries.containsKey(msg.getKey())) {
                    continue;
                }
                entries.put(msg.getKey(), new Entry(msg.getKey(), msg.getDictionary().get(locale), msg.getComment()));
                written.add(msg);
            }
        }
        List<Entry> sortedEntries = new ArrayList<>(entries.values());
        sortedEntries.sort(Comparator.comparing(o -> o.key));

        writeFile(file, sortedEntries);
        return written;
    }

    private void writeFile(File file, List<Entry> entries) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file, detectCharset(file, CHARSETS)));
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
                        .append(entry.value().replace("\n", "\\n"))
                        .append("\"")
                        .append(System.getProperty("line.separator"));
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Entry> readFile(File file) {

        Pattern keyValue = Pattern.compile("^([a-zA-Z._-]+)=((.)+)$");
        Map<String, Entry> entries = new HashMap<>();
        List<String> comments = new ArrayList<>();

        int lineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, detectCharset(file, CHARSETS)))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineIndex++;
                if (line.isEmpty() || line.isBlank()) {
                    continue;
                }
                if (line.startsWith("# ")) {
                    comments.add(line.substring(2));
                    continue;
                }
                if (line.startsWith("#")) {
                    comments.add(line.substring(1));
                    continue;
                }
                Matcher matcher = keyValue.matcher(line);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    String stripped = matcher.group(2);
                    stripped = stripped.startsWith("\"") ? stripped.substring(1, stripped.length() - 1) : stripped;
                    stripped = stripped.replace("\\n", "\n");
                    entries.put(key, new Entry(key, stripped, String.join("\n", comments)));
                    comments.clear();
                    continue;
                }
                Logger.getLogger("Translations").log(Level.SEVERE, "Error while parsing line " + lineIndex++ + " of " + file.getName() + ".\n > '" + line + "'");
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error while parsing locale file '" + file.getAbsolutePath() + "'.", t);
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
