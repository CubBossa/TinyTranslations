package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StyleDeserializerImplTest {

    MiniMessage miniMessage = MiniMessage.miniMessage();
    StyleDeserializer serializer;

    @BeforeEach
    void beforeEach() {
        serializer = new StyleDeserializerImpl();
    }

    @Test
    void deserialize1() {
        String testString = "ABC";
        String style = "a{slot}";
        Component expected = Component.text("ABC");

        Component result = miniMessage.deserialize(testString, serializer.deserialize("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize2() {
        String testString = "<a>ABC</a>";
        String style = "<red>{slot}</red>";
        Component expected = Component.text("ABC", NamedTextColor.RED);

        Component result = miniMessage.deserialize(testString, serializer.deserialize("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize3() {
        String testString = "<a>ABC</a>";
        String style = "x{slot}";
        Component expected = Component.text("xABC");

        Component result = miniMessage.deserialize(testString, serializer.deserialize("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize4() {
        String testString = "<url>https://www.google.com/</url>";
        String style = "<blue><u><hover:show_text:Click>{slot}</hover></u></blue>";
        Component expected = Component.text("https://www.google.com/")
                .decorate(TextDecoration.UNDERLINED)
                .color(NamedTextColor.BLUE)
                .hoverEvent(Component.text("Click"));

        Component result = miniMessage.deserialize(testString, serializer.deserialize("url", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize5() {
        String testString = "<a>ABC</a>";
        String style = "x{slot}y{slot}z<red>{slot}</red>";
        Component expected = Component.text("xABCyABCz").append(Component.text("ABC", NamedTextColor.RED));

        Component result = miniMessage.deserialize(testString, serializer.deserialize("a", style));
        Assertions.assertEquals(expected, result);
    }
}