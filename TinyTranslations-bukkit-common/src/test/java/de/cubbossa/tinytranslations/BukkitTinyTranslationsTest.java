package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Locale;

import static net.kyori.adventure.text.Component.*;

public class BukkitTinyTranslationsTest {


    @Test
    void testMaterial() {
        MessageTranslator t = BukkitTinyTranslations.application("test");
        Message l = t.messageBuilder("a").withDefault("{materials}").build();

        l = l.insertList("materials", List.of(Material.GOLD_ORE, Material.DIAMOND_ORE));

        Assertions.assertEquals(
                text("")
                        .append(translatable("block.minecraft.gold_ore"))
                        .append(text(", "))
                        .append(translatable("block.minecraft.diamond_ore")),
                GlobalTranslator.translator().translate(l, Locale.ENGLISH)
        );
    }

    @Test
    void testItemStack(@TempDir File dir) {
        MessageTranslator g = BukkitTinyTranslations.globalTranslator(dir);
        MessageTranslator t = g.fork("test");
        Message l = t.messageBuilder("a").withDefault("{materials}").build();

        l = l.insertList("materials", List.of(new ItemStack(Material.GOLD_ORE, 1), new ItemStack(Material.DIAMOND_ORE, 3)));

        Assertions.assertEquals(
                text("1x")
                        .append(translatable("block.minecraft.gold_ore"))
                        .append(text(", 3x")
                            .append(translatable("block.minecraft.diamond_ore"))),
                GlobalTranslator.renderer().render(l, Locale.ENGLISH).compact()
        );
    }
}
