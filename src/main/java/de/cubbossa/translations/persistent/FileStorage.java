package de.cubbossa.translations.persistent;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

public class FileStorage {

    final Logger logger;
    final File directory;
    final String fileSuffix;

    public FileStorage(Logger logger, File directory, String suffix) {
        directory.mkdirs();
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Language directory must not be a file.");
        }

        this.logger = logger;
        this.directory = directory;
        this.fileSuffix = suffix;
    }


    void mkDir() {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Could not create language directory");
            }
        }
    }

    File localeFile(Locale locale) {
        mkDir();
        File file = new File(directory, locale.toLanguageTag() + fileSuffix);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }
}
