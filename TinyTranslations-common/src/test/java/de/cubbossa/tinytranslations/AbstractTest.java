package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.ansi.ColorLevel;
import net.kyori.examination.string.MultiLineStringExaminer;
import org.intellij.lang.annotations.RegExp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class AbstractTest {

    protected static final ANSIComponentSerializer ANSI = ANSIComponentSerializer.builder()
            .colorLevel(ColorLevel.TRUE_COLOR)
            .build();

    protected File dir;
    protected MessageTranslator translator;

    public Component render(Component component) {
        return render(component, Locale.ENGLISH);
    }

    public Component render(Component component, Locale locale) {
        component = GlobalTranslator.render(component, locale);
        List<Component> children = component.children();
        return component.children(
                children.stream().map(c -> render(c, locale)).toList()
        );
    }

    @BeforeEach
    void beforeEach(@TempDir File d) {
        dir = new File(d, "/test/");
        dir.mkdirs();
        translator = TinyTranslations.application("TestApp");
    }

    @AfterEach
    void afterEach() {
        translator.close();
        deleteFile(dir);
        dir = null;
        translator = null;
    }

    void deleteFile(File file) {
        if (file.isDirectory() && file.listFiles().length > 0) {
            for (File listFile : file.listFiles()) {
                deleteFile(listFile);
            }
        }
        file.delete();
    }


    String fileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void replaceInFile(File file, @RegExp String regex, String val) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String fileContent = new String(bytes);
            fileContent = fileContent.replaceAll(regex, val);
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(fileContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertRenderEquals(Component expected, Component actual) {
        final Component expectedCompacted = expected.compact();
        final String expectedSerialized = this.prettyPrint(expectedCompacted);

        final Component actualCompacted = render(actual).compact();
        final String pretty = this.prettyPrint(actualCompacted);

        Assertions.assertEquals(expectedSerialized, pretty, () -> "Expected parsed value did not match actual:\n"
                + "  Expected: " + ANSI.serialize(expectedCompacted) + '\n'
                + "  Actual:   " + ANSI.serialize(actualCompacted));
    }

    protected final String prettyPrint(final Component component) {
        return component.examine(MultiLineStringExaminer.simpleEscaping()).collect(Collectors.joining("\n"));
    }
}
