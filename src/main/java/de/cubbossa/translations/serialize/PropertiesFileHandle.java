package de.cubbossa.translations.serialize;

import de.cubbossa.translations.LanguageFileException;
import de.cubbossa.translations.LanguageFileHandle;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.StyleFileHandle;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PropertiesFileHandle implements StyleFileHandle, LanguageFileHandle {

    private final Logger logger;
    private final File directory;

    public PropertiesFileHandle(Logger logger, File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Language directory must not be a file.");
        }
        this.logger = logger;
        this.directory = directory;
    }

    private void mkDir() {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new LanguageFileException("Could not create language directory");
            }
        }
    }

    private File localeFile(Locale locale) {
        mkDir();
        return new File(directory, locale.getLanguage() + ".properties");
    }

    private File stylesFile() {
        mkDir();
        return new File(directory, "styles.properties");
    }

    @Override
    public Optional<String> readMessage(Message message, Locale locale, Collection<Message> scope) {
        return Optional.ofNullable(readMessages(Set.of(message), locale, scope).get(message));
    }

    @Override
    public Map<Message, String> readMessages(Collection<Message> messages, Locale locale, Collection<Message> scope) {
        try {
            File file = localeFile(locale);
            if (!file.exists()) {
                Map<Message, String> result = scope.stream().collect(Collectors.toMap(
                        Function.identity(),
                        m -> m.getDefaultTranslations().getOrDefault(locale, m.getDefaultValue()))
                );
                writeMessages(result, locale);
                return result;
            }

            Properties properties = new Properties();
            properties.load(new FileInputStream(localeFile(locale)));

            Map<Message, String> result = new HashMap<>();
            Map<Message, String> toWrite = new HashMap<>();
            for (Message message : messages) {
                if (!properties.containsKey(message)) {
                    String t = message.getDefaultTranslations().getOrDefault(locale, message.getDefaultValue());
                    result.put(message, t);
                    toWrite.put(message, t);
                    continue;
                }
                result.put(message, properties.getProperty(message.getKey()));
            }
            writeMessages(toWrite, locale);
            return result;
        } catch (IOException e) {
            throw new LanguageFileException(e);
        }
    }

    @Override
    public boolean writeMessage(Message message, Locale locale, String translation) {
        return writeMessages(Map.of(message, translation), locale).contains(message);
    }

    @Override
    public Collection<Message> writeMessages(Map<Message, String> messages, Locale locale) {
        // TODO preserve existing file !!!
        File file = localeFile(locale);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (Map.Entry<Message, String> entry : messages.entrySet()) {
                if (entry.getKey().getComment() != null) {
                    for (String commentLine : entry.getKey().getComment().split("\n")) {
                        writer.append("# ")
                                .append(commentLine)
                                .append(System.getProperty("line.separator"));
                    }
                }
                writer.append(entry.getKey().getKey())
                        .append("=")
                        .append("\"")
                        .append(entry.getValue())
                        .append("\"")
                        .append(System.getProperty("line.separator"));
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messages.keySet();
    }

    @Override
    public Collection<TagResolver> loadStyles() {
        return null;
    }
}
