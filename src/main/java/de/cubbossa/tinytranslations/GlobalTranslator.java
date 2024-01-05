package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.persistent.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.persistent.PropertiesStyleStorage;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Locale;

@Getter
@Setter
public class GlobalTranslator extends AppTranslator implements Translator {

    protected GlobalTranslator(File dir) {
        super(null, "global");

        if (!dir.exists()) {
            throw new IllegalArgumentException("Global translations directory must exist.");
        }
        String dirName;
        try {
            dirName = dir.getCanonicalFile().getName();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        if (!dirName.equals("plugins") && !dirName.equals("test")) {
            throw new IllegalArgumentException("Global translations directory must be the plugins directory of a server. Instead '" + dir.getName() + "'.");
        }
        File globalLangDir = new File(dir, "/lang/");


        // If lang dir exists, whatever happens in there is the choice of administrators
        boolean createStartFiles = !globalLangDir.exists();

        if (createStartFiles && !globalLangDir.mkdirs()) {
            throw new IllegalStateException("Could not create /lang/ directory for global translations.");
        }
        if (createStartFiles) {
            writeResourceIfNotExists(globalLangDir, "README.txt");
            writeResourceIfNotExists(globalLangDir, "lang/global_styles.properties", "global_styles.properties");
        }

        setMessageStorage(new PropertiesMessageStorage(globalLangDir));
        setStyleStorage(new PropertiesStyleStorage(new File(globalLangDir, "global_styles.properties")));

        addMessages(TinyTranslations.messageFieldsFromClass(GlobalMessages.class));
        saveLocale(Locale.ENGLISH);

        writeMissingDefaultStyles();
    }

    private void writeResourceIfNotExists(File langDir, String name) {
        writeResourceIfNotExists(langDir, name, name);
    }

    private void writeResourceIfNotExists(File langDir, String name, String as) {
        File file = new File(langDir, as);
        if (file.exists()) {
            return;
        }
        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Could not create resource");
            }
            InputStream is = getClass().getResourceAsStream("/" + name);
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

    private void writeMissingDefaultStyles() {
        File tempFile;
        try {
            tempFile = File.createTempFile("stream_to_file", ".properties");
            tempFile.deleteOnExit();
            InputStream is = getClass().getResourceAsStream("/lang/global_styles.properties");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                out.write(is.readAllBytes());
            }
        } catch (Throwable t) {
            throw new RuntimeException("Could not create temp file to append missing default styles.");
        }
        PropertiesStyleStorage storage = new PropertiesStyleStorage(tempFile);
        storage.loadStyles().forEach((s, messageStyle) -> {
            if (!this.getStyleSet().containsKey(s)) {
                this.getStyleSet().put(s, messageStyle);
            }
        });
        if (this.getStyleStorage() != null) {
            this.getStyleStorage().writeStyles(this.getStyleSet());
        }
    }

    @Override
    public Translator forkWithStorage(String name) {
        throw new IllegalStateException("Cannot fork global Translations with storage. Use fork instead.");
    }

    @Override
    public @NotNull Locale getUserLocale(@Nullable Audience user) {
        return Locale.ENGLISH;
    }
}
