package de.cubbossa.translations.persistent;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public abstract class FileStorage {

    final File directory;
    final String fileSuffix;

    public FileStorage(File directory, String suffix) {
        directory.mkdirs();
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Language directory must not be a file.");
        }

        this.directory = directory;
        this.fileSuffix = suffix;

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

    @Nullable File localeFileIfExists(Locale locale) {
        File file = new File(directory, locale.toLanguageTag() + fileSuffix);
        if (!file.exists()) {
            return null;
        }
        return file;
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
