package de.cubbossa.translations;

import be.seeseemelk.mockbukkit.MockBukkit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

public class TestPlugin {

    public static final File dir = new File("src/main/resources");

    public static final Message TEST_1 = new MessageBuilder("examples.test.first")
            .withComment("Lets test this")
            .withPlaceholder("red", "The color red")
            .withPlaceholders("green", "blue")
            .withDefault("<red>Hello world!")
            .withTranslation(Locale.GERMAN, "<red>Hallo Welt!")
            .build();
    public static final Message TEST_2 = new MessageBuilder("examples.test.second")
            .withComment("Another test with\nline break\n\ncomments")
            .withPlaceholder("abc")
            .withDefault("<green>Luke - I am your father!")
            .build();

    @BeforeAll
    public static void beforeAll() {
        MockBukkit.mock();
    }

    @AfterAll
    public static void afterAll() {
        MockBukkit.unmock();
        for (File file : dir.listFiles()) {
            // file.delete();
        }
    }

    @Test
    public void testLoad() {

        JavaPlugin plugin = MockBukkit.createMockPlugin("testpl");

        PluginTranslations translations = Translations.create(
                "test",
                BukkitAudiences.create(plugin),
                dir,
                Logger.getLogger("default")
        );
        translations.addMessagesClass(this.getClass());

        Assertions.assertEquals(Component.text("Hello world!", NamedTextColor.RED), translations.translate(TEST_1));
        Assertions.assertEquals(Component.text("Luke - I am your father!", NamedTextColor.GREEN), translations.translate(TEST_2));
        Assertions.assertEquals(Component.text("Hallo Welt!", NamedTextColor.RED), translations.translate(TEST_1, Locale.GERMAN));
    }
}
