package de.cubbossa.translations.persistent;

import de.cubbossa.translations.GlobalMessageBundle;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.logging.Logger;

public abstract class FileStorage {

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

        File global = GlobalMessageBundle.get().getDataFolder();

        try {
            // TODO the following code creates undeletable files on linux and no files at all on windows
            // Files.createSymbolicLink(new File(global, directory.getParentFile().getName()).toPath(), directory.toPath());
            // Files.createSymbolicLink(new File(directory, global.getName()).toPath(), global.toPath());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    void mkDir() {
        if (!directory.exists()) {
            directory.mkdirs();
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
