package de.cubbossa.tinytranslations.storage.properties;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TranslationKey;
import de.cubbossa.tinytranslations.storage.FileMessageStorage;
import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.StorageEntry;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PropertiesMessageStorage extends FileMessageStorage implements MessageStorage {

    private static final Charset[] CHARSETS = {StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1};

    public PropertiesMessageStorage(File directory) {
        this(directory, "", "");
    }

    public PropertiesMessageStorage(File directory, String prefix, String suffix) {
        super(directory, prefix, suffix + ".properties");
    }

    @Override
    public Map<TranslationKey, String> readMessages(Locale locale) {
        File file = localeFileIfExists(locale);
        if (file == null) {
            return new HashMap<>();
        }

        Map<String, StorageEntry> entries = readFile(file);
        Map<TranslationKey, String> result = new HashMap<>();
        entries.forEach((key, value) -> result.put(TranslationKey.of(key), value.value()));
        return result;
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
        File file = localeFile(locale);

        Collection<Message> written = new HashSet<>();
        Map<String, StorageEntry> entries = readFile(file);
        for (Message msg : messages) {
            if (msg.getDictionary().containsKey(locale)) {
                if (entries.containsKey(msg.getKey().key())) {
                    continue;
                }
                List<String> comments = msg.getComment() == null ? Collections.emptyList() : List.of(msg.getComment().split("\n"));
                entries.put(msg.key(), new StorageEntry(msg.getKey().key(), msg.getDictionary().get(locale), comments));
                written.add(msg);
            }
        }
        List<StorageEntry> sortedEntries = new ArrayList<>(entries.values());
        sortedEntries.sort(Comparator.comparing(StorageEntry::key));

        writeFile(file, sortedEntries);
        return written;
    }

    private void writeFile(File file, List<StorageEntry> entries) {
        try (Writer writer = new FileWriter(file, detectCharset(file, CHARSETS))) {
            PropertiesUtils.write(writer, entries);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, StorageEntry> readFile(File file) {
        try (Reader reader = new FileReader(file, detectCharset(file, CHARSETS))) {
            return PropertiesUtils.loadProperties(reader).stream().collect(Collectors.toMap(
                    StorageEntry::key, e -> e
            ));
        } catch (Throwable t) {
            throw new RuntimeException("Error while parsing locale file '" + file.getAbsolutePath() + "'.", t);
        }
    }
}
