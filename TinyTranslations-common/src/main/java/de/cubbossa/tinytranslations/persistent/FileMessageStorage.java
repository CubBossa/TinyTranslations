package de.cubbossa.tinytranslations.persistent;

import de.cubbossa.tinytranslations.Message;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public abstract class FileMessageStorage implements MessageStorage {

    final File directory;
    final String filePrefix;
    final String fileSuffix;

    public FileMessageStorage(File directory, String prefix, String suffix) {
        directory.mkdirs();
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Language directory must not be a file.");
        }

        this.directory = directory;
        this.filePrefix = prefix;
        this.fileSuffix = suffix;
    }


    void mkDir() {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public Map<Message, String> readMessages(Locale locale) {
        return null;
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
        return null;
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

    public Charset detectCharset(File f, Charset[] charsets) {
        for (Charset charset : charsets) {
            Charset result = detectCharset(f, charset);
            if (result != null) {
                return result;
            }
        }
        return StandardCharsets.UTF_8;
    }

    private Charset detectCharset(File f, Charset charset) {
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(f))){

            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();

            byte[] buffer = new byte[512];
            boolean identified = false;
            while ((input.read(buffer) != -1) && (!identified)) {
                identified = identify(buffer, decoder);
            }

            input.close();

            if (identified) {
                return charset;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
}