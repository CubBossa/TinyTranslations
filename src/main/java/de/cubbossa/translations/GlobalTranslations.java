package de.cubbossa.translations;

import de.cubbossa.translations.persistent.PropertiesMessageStorage;
import de.cubbossa.translations.persistent.PropertiesStyleStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;

@Getter
@Setter
public class GlobalTranslations extends AppTranslations implements Translations {

    @SneakyThrows
    protected GlobalTranslations(File dir) {
        super(null, "global");

        Logger logger = Logger.getLogger("Translations");

        if (!dir.exists()) {
            throw new IllegalArgumentException("Global translations directory must exist.");
        }
        if (!dir.getName().equals("plugins") && !dir.getName().equals("test")) {
            throw new IllegalArgumentException("Global translations directory must be the plugins directory of a server");
        }
        File globalLangDir = new File(dir, "/lang/");


        // If lang dir exists, whatever happens in there is the choice of administrators
        boolean createStartFiles = !globalLangDir.exists();

        if (!globalLangDir.mkdirs()) {
            throw new IllegalStateException("Could not create /lang/ directory for global translations.");
        }

        setMessageStorage(new PropertiesMessageStorage(logger, globalLangDir));
        setStyleStorage(new PropertiesStyleStorage(new File(globalLangDir, "global_styles.properties")));

        if (!createStartFiles) {
            return;
        }
        writeResourceIfNotExists(globalLangDir, "README.txt");
        writeResourceIfNotExists(globalLangDir, "global_styles.properties");
        writeResourceIfNotExists(globalLangDir, "en.properties");
    }

    @SneakyThrows
    private void writeResourceIfNotExists(File langDir, String name) {
        File file = new File(langDir, name);
        if (file.exists()) {
            return;
        }
        if (!file.createNewFile()) {
            throw new IllegalStateException("Could not create resource");
        }
        InputStream is = getClass().getResourceAsStream(name);
        if (is == null) {
            throw new IllegalArgumentException("Could not load resource with name '" + name + "'.");
        }
        FileOutputStream os = new FileOutputStream(file);
        os.write(is.readAllBytes());
        os.close();
        is.close();
    }

    @Override
    public Translations forkWithStorage(String name) {
        throw new IllegalStateException("Cannot fork global Translations with storage. Use fork instead.");
    }

    @Override
    public @NotNull Locale getUserLocale(@Nullable Audience user) {
        return Locale.ENGLISH;
    }
}
