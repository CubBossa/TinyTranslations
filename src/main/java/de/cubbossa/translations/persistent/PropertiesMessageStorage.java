package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesMessageStorage extends FileStorage implements MessageStorage {


    public PropertiesMessageStorage(Logger logger, File directory) {
        super(logger, directory, ".properties");
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
        Set<Message> toWrite = new HashSet<>();
        for (Message message : messages) {
            if (!entryMap.containsKey(message.getKey())) {
                String t = message.getDefaultTranslations().get(locale);
                if (t == null) {
                    continue;
                }
                result.put(message, t);
                toWrite.add(message);
                continue;
            }
            result.put(message, entryMap.get(message.getKey()));
        }
        writeMessages(toWrite, locale);
        return result;
    }

    @Override
    public boolean writeMessage(Message message, Locale locale, String translation) {
        return writeMessages(Set.of(message), locale).contains(message);
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
        File file = localeFile(locale);

        List<Entry> entries = readFile(file);
        for (Message msg : messages) {
            entries.add(new Entry(msg.getNamespacedKey(), msg.getDefaultTranslations().get(locale), msg.getComment()));
        }
        entries = new ArrayList<>(new HashSet<>(entries));
        entries.sort(Comparator.comparing(o -> o.key));

        writeFile(file, entries);
        return messages;
    }

    private void writeFile(File file, List<Entry> entries) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
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

    private List<Entry> readFile(File file) {

        Pattern keyValue = Pattern.compile("^([a-zA-Z._-]+)=((.)+)$");
        List<Entry> entries = new ArrayList<>();
        List<String> comments = new ArrayList<>();

        int lineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
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
                    String stripped = matcher.group(2);
                    stripped = stripped.startsWith("\"") ? stripped.substring(1, stripped.length() - 1) : stripped;
                    stripped = stripped.replace("\\n", "\n");
                    entries.add(new Entry(matcher.group(1), stripped, String.join("\n", comments)));
                    comments.clear();
                    continue;
                }
                throw new RuntimeException("Error while parsing line " + lineIndex++ + " of " + file.getName() + ".\n > '" + line + "'");
            }
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
