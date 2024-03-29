package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageTranslatorImplInheritanceTest extends AbstractTest {

    private MessageTranslator server;

    @BeforeEach
    void server(@TempDir File dir) {
        server = TinyTranslations.globalTranslator(dir);
        translator = server.fork("Sub");
    }

    @Test
    void globalStyleOverride() {
        MessageTranslator global = server;
        global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
        translator.getStyleSet().put("negative", Style.style(TextDecoration.UNDERLINED));
        Message m = translator.messageBuilder("a").withDefault("<negative>abc").build();
        assertEquals(text("abc").decorate(TextDecoration.UNDERLINED), translator.translate(m));
    }

    @Test
    void globalOverride() {
        MessageTranslator global = server;
        translator.getStyleSet().put("negative", "<yellow>");
        Message m = global.messageBuilder("test").withDefault("<negative>test</negative>").build();
        assertEquals(text("test", NamedTextColor.RED), global.translate(m));
        assertEquals(text("test", NamedTextColor.YELLOW), translator.translate(m));
    }

    @Test
    void globalMsg() {
        MessageTranslator global = server;
        global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();

        Message a = translator.messageBuilder("a").withDefault("<msg:global.brand>a").build();
        assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" a")), render(a).compact());

        Message b = translator.messageBuilder("b").withDefault("<msg:global:brand>b").build();
        assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), render(b).compact());

        Message c = translator.messageBuilder("a").withDefault("<msg:brand>c").build();
        assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" c")), render(c).compact());
    }

    @Test
    void globalMsgOverride() {
        MessageTranslator global = server;
        global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();
        translator.messageBuilder("brand").withDefault("<green>Prefix</green> ").build();

        Message a = translator.messageBuilder("a").withDefault("<msg:brand>a").build();
        assertEquals(empty().append(text("Prefix", NamedTextColor.GREEN)).append(text(" a")), render(a).compact());

        Message b = translator.messageBuilder("b").withDefault("<msg:global:brand>b").build();
        assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), render(b).compact());
    }

    @Test
    void globalStyle() {
        MessageTranslator global = server;
        global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
        Message m = translator.messageBuilder("a").withDefault("<negative>abc").build();
        assertEquals(text("abc", NamedTextColor.RED), translator.translate(m));
    }

    @Test
    void getInParent() {
        Message m1 = translator.messageBuilder("a").withDefault("A").build();
        Message m2 = translator.getParent().messageBuilder("b").withDefault("B").build();
        assertEquals(m1, translator.getMessageInParentTree("a"));
        assertEquals(m2, translator.getMessageInParentTree("b"));
        Assertions.assertNull(translator.getMessageInParentTree("x"));
    }

    @Test
    void getByKey() {
        Message m1 = translator.getParent().message("a");
        assertEquals(m1, translator.getMessageByNamespace("global", "a"));
        Message m2 = translator.message("a");
        assertEquals(m1, translator.getMessageByNamespace("global", "a"));
    }

    @Test
    public void testDefaultsAvailable() {
        Assertions.assertEquals(
                Component.text("X", NamedTextColor.GREEN),
                translator.translate("<positive>X").compact()
        );
        Assertions.assertEquals(
                Component.text("X", NamedTextColor.GREEN),
                translator.translate("<positive>X").compact()
        );
    }

    @Test
    public void overwriteStyles() {
        Assertions.assertFalse(translator.getStyleSet().containsKey("negative"));
        Assertions.assertEquals(
                Component.text("X", NamedTextColor.RED),
                translator.translate("<negative>X")
        );
        translator.getStyleSet().put("negative", "<green>");
        Assertions.assertEquals(
                Component.text("X", NamedTextColor.GREEN),
                translator.translate("<negative>X")
        );
    }
}
