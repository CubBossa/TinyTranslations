package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.NanoMessage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TinyTranslations {

    private static final Logger LOGGER = Logger.getLogger("Translations");
    public static final Locale FALLBACK_DEFAULT_LOCALE = Locale.ENGLISH;

    public static final NanoMessage NM = NanoMessage.nanoMessage();

    protected TinyTranslations() {
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static MessageTranslator application(String name) {
        var tr = new MessageTranslatorImpl(null, name);
        applyDefaultObjectResolvers(tr);
        return tr;
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
                LOGGER.log(Level.WARNING, "Could not extract message '" + messageField.getName() + "' from class " + messageClass.getSimpleName(), t);
            }
        }
        return messages;
    }

    /**
     * Creates the lang directory
     *
     * @param root The root directory, in terms of plugins the /plugin/ directory.
     * @return The global MessageTranslator instance.
     */
    public static MessageTranslator globalTranslator(File root) {

        initGlobalFiles(root);
        MessageTranslator global = new MessageTranslatorImpl(null, "global") {
            @Override
            public void loadStyles() {
                if (initGlobalFiles(root) || !new File(root, "/lang/global_styles.properties").exists()) {
                    writeResourceIfNotExists(root, "lang/global_styles.properties", "lang/global_styles.properties");
                } else {
                    writeMissingDefaultStyles(this);
                }
                super.loadStyles();
            }

            @Override
            public void loadLocales() {
                boolean init = initGlobalFiles(root);
                for (Locale locale : GlobalMessages.LOCALES) {
                    String s = "lang/global_messages_" + locale.toLanguageTag().replaceAll("-", "_") + ".properties";
                    if (init || !new File(root, s).exists()) {
                        writeResourceIfNotExists(root, s);
                    }
                }
                super.loadLocales();
            }

            @Override
            public void loadLocale(Locale locale) {
                String s = "lang/global_messages_" + locale.toLanguageTag().replaceAll("-", "_") + ".properties";
                if (initGlobalFiles(root) || !new File(root, s).exists()) {
                    writeResourceIfNotExists(root, s);
                }
                super.loadLocale(locale);
            }
        };

        global.setMessageStorage(new PropertiesMessageStorage(new File(root, "/lang/"), "global_messages_", ""));
        global.setStyleStorage(new PropertiesStyleStorage(new File(root, "lang/global_styles.properties")));

        global.addMessages(messageFieldsFromClass(GlobalMessages.class));

        global.loadStyles();
        global.loadLocales();

        applyDefaultObjectResolvers(global);

        return global;
    }

    private static boolean initGlobalFiles(File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException("Global translations directory must exist.");
        }
        File globalLangDir = new File(directory, "/lang/");

        boolean init = !globalLangDir.exists();
        if (init && !globalLangDir.mkdirs()) {
            throw new IllegalStateException("Could not create /lang/ directory for global translations.");
        }
        if (init) {
            writeResourceIfNotExists(globalLangDir, "README.txt");
        }
        return init;
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

    private static void applyDefaultObjectResolvers(MessageTranslator translator) {
        translator.add(TinyObjectMapping.alwaysConvert(String.class, Component::text));
        translator.add(TinyObjectMapping.builder(Number.class).withFallbackResolver((value, context, argumentQueue) -> {
            return Formatter.number("avoid_name_collisions", value).resolve("avoid_name_collisions", argumentQueue, context);
        }).build());

        translator.add(TinyObjectMapping.builder(Boolean.class).withFallbackConversion(Component::text).build());
        translator.add(TinyObjectMapping.builder(char.class).withFallbackConversion(Component::text).build());
        translator.add(TinyObjectMapping.builder(Locale.class).withFallbackConversion(l -> Component.text(l.toLanguageTag())).build());

        translator.add(TinyObjectMapping.alwaysConvert(Supplier.class, Supplier::get));
        translator.add(TinyObjectMapping.alwaysConvert(CompletableFuture.class, CompletableFuture::join));
        translator.add(TinyObjectMapping.alwaysConvert(Optional.class, o -> o.orElse(null)));
    }
}
