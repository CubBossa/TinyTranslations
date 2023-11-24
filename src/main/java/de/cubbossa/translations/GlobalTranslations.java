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
        setMiniMessage(MiniMessage.miniMessage());

        Logger logger = Logger.getLogger("Translations");
        File globalLangDir = new File(dir, "/lang/");
        globalLangDir.mkdirs();

        setMessageStorage(new PropertiesMessageStorage(logger, globalLangDir));
        setStyleStorage(new PropertiesStyleStorage(new File(globalLangDir, "global_styles.properties")));

        File readme = new File(globalLangDir, "README.txt");
        readme.createNewFile();
        InputStream is = getClass().getResourceAsStream("/README.txt");
        FileOutputStream os = new FileOutputStream(readme);
        os.write(is.readAllBytes());
        os.close();
        is.close();
    }

    @Override
    public @NotNull Locale getUserLocale(@Nullable Audience user) {
        return Locale.ENGLISH;
    }
}
