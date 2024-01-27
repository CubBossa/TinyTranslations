package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.NanoMessage;
import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TinyTranslations {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public static final NanoMessage NM = NanoMessage.nanoMessage();

    static {
        applyDefaultObjectResolvers(NM.getObjectTypeResolverMap());
    }

    protected TinyTranslations() {
    }

    public static MessageTranslator application(String name) {
        return new MessageTranslatorImpl(null, name);
    }

    public static Message[] messageFieldsFromClass(Class<?> messageClass) {
        try {
            TinyTranslations.class.getClassLoader().loadClass(messageClass.getName());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Field[] fields = Arrays.stream(messageClass.getDeclaredFields())
                .filter(field -> Message.class.isAssignableFrom(field.getType()))
                .toArray(Field[]::new);

        Message[] messages = new Message[fields.length];
        int i = 0;
        for (Field messageField : fields) {
            try {
                messages[i++] = (Message) messageField.get(messageClass);
            } catch (Throwable t) {
                Logger.getLogger("Translations").log(Level.WARNING, "Could not extract message '" + messageField.getName() + "' from class " + messageClass.getSimpleName(), t);
            }
        }
        return messages;
    }

    public static MessageTranslator globalTranslator(File directory) {
        MessageTranslator global = new MessageTranslatorImpl(null, "global");

        if (!directory.exists()) {
            throw new IllegalArgumentException("Global translations directory must exist.");
        }
        File globalLangDir = new File(directory, "/lang/");

        // If lang dir exists, whatever happens in there is the choice of administrators
        boolean createStartFiles = !globalLangDir.exists();

        if (createStartFiles && !globalLangDir.mkdirs()) {
            throw new IllegalStateException("Could not create /lang/ directory for global translations.");
        }
        if (createStartFiles) {
            writeResourceIfNotExists(globalLangDir, "README.txt");
            writeResourceIfNotExists(globalLangDir, "lang/global_styles.properties", "global_styles.properties");
        }

        global.setMessageStorage(new PropertiesMessageStorage(globalLangDir));
        global.setStyleStorage(new PropertiesStyleStorage(new File(globalLangDir, "global_styles.properties")));

        global.addMessages(messageFieldsFromClass(GlobalMessages.class));
        global.saveLocale(Locale.ENGLISH);
        global.loadLocale(Locale.ENGLISH);

        writeMissingDefaultStyles(global);
        return global;
    }

    private static void writeResourceIfNotExists(File langDir, String name) {
        writeResourceIfNotExists(langDir, name, name);
    }

    private static void writeResourceIfNotExists(File langDir, String name, String as) {
        File file = new File(langDir, as);
        if (file.exists()) {
            return;
        }
        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Could not create resource");
            }
            InputStream is = TinyTranslations.class.getResourceAsStream("/" + name);
            if (is == null) {
                throw new IllegalArgumentException("Could not load resource with name '" + name + "'.");
            }
            FileOutputStream os = new FileOutputStream(file);
            os.write(is.readAllBytes());
            os.close();
            is.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not load resource with name '" + name + "'.", e);
        }
    }

    private static void writeMissingDefaultStyles(MessageTranslator translator) {
        File tempFile;
        try {
            tempFile = File.createTempFile("stream_to_file", ".properties");
            tempFile.deleteOnExit();
            try (InputStream is = TinyTranslations.class.getResourceAsStream("/lang/global_styles.properties")) {
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    out.write(is.readAllBytes());
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Could not create temp file to append missing default styles.");
        }
        PropertiesStyleStorage storage = new PropertiesStyleStorage(tempFile);
        storage.loadStyles().forEach((s, messageStyle) -> {
            if (!translator.getStyleSet().containsKey(s)) {
                translator.getStyleSet().put(s, messageStyle);
            }
        });
        if (translator.getStyleStorage() != null) {
            translator.getStyleStorage().writeStyles(translator.getStyleSet());
        }
    }

    private static void applyDefaultObjectResolvers(ObjectTagResolverMap map) {
        map.put(List.class, Collections.emptyMap(), list -> Component.text(
                (String) list.stream().map(Object::toString).collect(Collectors.joining(", "))
        ));
        map.put(String.class, Collections.emptyMap(), Component::text);
        map.put(Integer.class, Collections.emptyMap(), Component::text);
        map.put(Short.class, Collections.emptyMap(), Component::text);
        map.put(Double.class, Collections.emptyMap(), Component::text);
        map.put(Float.class, Collections.emptyMap(), Component::text);
        map.put(Long.class, Collections.emptyMap(), Component::text);
        map.put(Boolean.class, Collections.emptyMap(), Component::text);
        map.put(char.class, Collections.emptyMap(), Component::text);
        map.put(Locale.class, Collections.emptyMap(), l -> Component.text(l.toLanguageTag()));

    }
}
