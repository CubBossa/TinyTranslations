package de.cubbossa.translations.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentSplitTest {

    @Test
    void newLine() {

        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

        Component abc = Component.empty()
                .append(Component.text("a")
                        .append(Component.text("b"))
                        .append(Component.newline()))
                .append(Component.text("c"));

        assertEquals(
                List.of("ab", "c"),
                ComponentSplit.split(abc, "\n").stream().map(serializer::serialize).collect(Collectors.toList())
        );


        assertEquals(
                List.of("Hallo"),
                ComponentSplit.split(Component.text("Hallo"), "\n").stream()
                        .map(serializer::serialize)
                        .toList()
        );

        assertEquals(
                List.of("Hallo", "welt", "123"),
                ComponentSplit.split(Component.text("Hallo\nwelt\n123"), "\n").stream()
                        .map(serializer::serialize)
                        .toList()
        );

        Component cs = MiniMessage.miniMessage().deserialize("<gray>» <yellow>right-click air:</yellow> Open GUI</gray>");

        assertEquals(
                List.of(
                        "» right-click air: Open GUI"
                ),
                ComponentSplit.split(cs, "\n").stream().map(serializer::serialize).collect(Collectors.toList())
        );

        Component c = MiniMessage.miniMessage().deserialize("<gray>Assign and remove multiple\n<gray>groups at once.\n\n<gray>» <yellow>right-click air:</yellow> Open GUI</gray>\n<gray>» <yellow>right-click node:</yellow> Add groups</gray>\n<gray>» <yellow>right-click node:</yellow> Remove groups</gray>"
                .replaceAll("\\\\", "\\"));

        assertEquals(
                List.of(
                        "Assign and remove multiple",
                        "groups at once.",
                        "",
                        "» right-click air: Open GUI",
                        "» right-click node: Add groups",
                        "» right-click node: Remove groups"
                ),
                ComponentSplit.split(c, "\n").stream().map(serializer::serialize).collect(Collectors.toList())
        );
    }
}
