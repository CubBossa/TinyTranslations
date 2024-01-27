package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.intellij.lang.annotations.RegExp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public class TestBase {

    protected File dir;
    protected MessageTranslator translator;

    public Component render(Component component) {
        return render(component, Locale.ENGLISH);
    }

    public Component render(Component component, Locale locale) {
        component = GlobalTranslator.renderer().render(component, locale);
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
}
