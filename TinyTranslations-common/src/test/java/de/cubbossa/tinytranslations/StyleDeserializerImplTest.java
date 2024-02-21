package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.NanoMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StyleDeserializerImplTest {

    NanoMessage nanoMessage = NanoMessage.nanoMessage();

    @Test
    void deserialize0() {
        String testString = "<a>red";
        String style = "<red>";
        Component expected = Component.text("red", NamedTextColor.RED);

        Component result = nanoMessage.deserialize(testString, MessageStyle.messageStyle("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize1() {
        String testString = "ABC";
        String style = "a{slot}";
        Component expected = Component.text("ABC");

        Component result = nanoMessage.deserialize(testString, MessageStyle.messageStyle("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize2() {
        String testString = "<a>ABC</a>";
        String style = "<red>{slot}</red>";
        Component expected = Component.text("ABC", NamedTextColor.RED);

        Component result = nanoMessage.deserialize(testString, MessageStyle.messageStyle("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize3() {
        String testString = "<a>ABC</a>";
        String style = "x{slot}";
        Component expected = Component.text("xABC");

        Component result = nanoMessage.deserialize(testString, MessageStyle.messageStyle("a", style));
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

        Component result = nanoMessage.deserialize(testString, MessageStyle.messageStyle("url", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserialize5() {
        String testString = "<a>ABC</a>";
        String style = "x{slot}y{slot}z<red>{slot}</red>";
        Component expected = Component.text("xABCyABCz").append(Component.text("ABC", NamedTextColor.RED));

        Component result = nanoMessage.deserialize(testString, MessageStyle.messageStyle("a", style));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserializeArgs() {
        MessageStyle style = MessageStyle.messageStyle("a", "{slot}: {arg0}/{arg1}");
        String msg = "<a:1:2>test</a>";
        Assertions.assertEquals(
                Component.text("test: 1/2"),
                nanoMessage.deserialize(msg, style)
        );
    }

    @Test
    void deserializeArgs1() {
        MessageStyle style = MessageStyle.messageStyle("a", "{slot}: 1/{arg1}");
        String msg = "<a:1:2>test</a>";
        Assertions.assertEquals(
                Component.text("test: 1/2"),
                nanoMessage.deserialize(msg, style)
        );
    }
}