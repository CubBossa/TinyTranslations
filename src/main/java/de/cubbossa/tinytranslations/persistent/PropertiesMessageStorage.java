package de.cubbossa.tinytranslations.persistent;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageCore;
import de.cubbossa.tinytranslations.util.Entry;
import de.cubbossa.tinytranslations.util.PropertiesUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
                entries.put(msg.getKey(), new Entry(msg.getKey(), msg.getDictionary().get(locale), List.of(msg.getComment().split("\n"))));
                written.add(msg);
            }
        }
        List<Entry> sortedEntries = new ArrayList<>(entries.values());
        sortedEntries.sort(Comparator.comparing(Entry::key));

        writeFile(file, sortedEntries);
        return written;
    }

    private void writeFile(File file, List<Entry> entries) {
        try (Writer writer = new FileWriter(file, detectCharset(file, CHARSETS))) {
            PropertiesUtils.write(writer, entries);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Entry> readFile(File file) {
        try (Reader reader = new FileReader(file, detectCharset(file, CHARSETS))) {
            return PropertiesUtils.loadProperties(reader).stream().collect(Collectors.toMap(
                    Entry::key, e -> e
            ));
        } catch (Throwable t) {
            throw new RuntimeException("Error while parsing locale file '" + file.getAbsolutePath() + "'.", t);
        }
    }
}
