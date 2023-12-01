package de.cubbossa.translations.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class DefaultResolversTest {

    MiniMessage miniMessage = MiniMessage.miniMessage();
    PlainTextComponentSerializer p = PlainTextComponentSerializer.plainText();
    Function<Component, String> plain = p::serialize;

    @Test
    void url1() {
        String noUrl = "httbs://www.google.com/search?client=firefox-b-d&q=Component";
        TagResolver resolver = TagResolver.resolver("url", DefaultResolvers.shortURLTag(0, 0));

        Assertions.assertEquals(Component.text(noUrl), miniMessage.deserialize("<url>" + noUrl + "</url>", resolver));
    }

    @Test
    void url2() {
        String url = "https://www.google.com/search?client=firefox-b-d&q=Component";
        String urlShort = "https://www.google.com/sea...ent";
        TagResolver resolver = TagResolver.resolver("url", DefaultResolvers.shortURLTag(3, 3));

        Assertions.assertEquals(Component.text(urlShort), miniMessage.deserialize("<url>" + url + "</url>", resolver));
    }

    @Test
    void url3() {
        String url = "https://www.google.com/search?client=firefox-b-d&q=Component";
        TagResolver resolver = TagResolver.resolver("url", DefaultResolvers.shortURLTag(15, 23));

        Assertions.assertEquals(Component.text(url), miniMessage.deserialize("<url>" + url + "</url>", resolver));
    }

    @Test
    void preview() {
        TagResolver resolver = DefaultResolvers.preview("preview");

        Assertions.assertEquals("aaaaa", plain.apply(miniMessage.deserialize("<preview>aaaaa", resolver)));
        Assertions.assertEquals("", plain.apply(miniMessage.deserialize("<preview:0>aaaaa", resolver)));
        Assertions.assertEquals("a", plain.apply(miniMessage.deserialize("<preview:1>aaaaa", resolver)));
        Assertions.assertEquals("aa", plain.apply(miniMessage.deserialize("<preview:2>aa", resolver)));
        Assertions.assertEquals("aa", plain.apply(miniMessage.deserialize("<preview:2>a<red>aaaa", resolver)));
    }

    @Test
    void toLower() {
        TagResolver resolver = DefaultResolvers.lower("lowercase");

        Assertions.assertEquals(Component.text(""), miniMessage.deserialize("<lowercase>", resolver));
        Assertions.assertEquals(Component.text("a"), miniMessage.deserialize("<lowercase>A", resolver));
        Assertions.assertEquals(Component.text("---"), miniMessage.deserialize("<lowercase>---", resolver));
        Assertions.assertEquals(Component.text("aaaaa"), miniMessage.deserialize("<lowercase>AaAaA", resolver));
        Assertions.assertEquals(Component.text("Aa"), miniMessage.deserialize("A<lowercase>A", resolver));
    }

    @Test
    void toUpper() {
        TagResolver resolver = DefaultResolvers.upper("upper");

        Assertions.assertEquals(Component.text(""), miniMessage.deserialize("<upper>", resolver));
        Assertions.assertEquals(Component.text("A"), miniMessage.deserialize("<upper>a", resolver));
        Assertions.assertEquals(Component.text("---"), miniMessage.deserialize("<upper>---", resolver));
        Assertions.assertEquals(Component.text("AAAAA"), miniMessage.deserialize("<upper>AaAaA", resolver));
        Assertions.assertEquals(Component.text("aA"), miniMessage.deserialize("a<upper>A", resolver));
    }

    @Test
    void repeat() {
        TagResolver resolver = DefaultResolvers.repeat("repeat");

        Assertions.assertEquals(Component.text(""), miniMessage.deserialize("<repeat:0>abc</repeat>", resolver));
        Assertions.assertEquals(Component.text("abcabc"), miniMessage.deserialize("<repeat:a3>abc</repeat>", resolver));
        Assertions.assertEquals(Component.text("abcabcabc"), miniMessage.deserialize("<repeat:3>abc</repeat>", resolver));
        Assertions.assertEquals(Component.text("abcabc"), miniMessage.deserialize("<repeat>abc</repeat>", resolver));
    }

    @Test
    void reverse() {
         TagResolver resolver = DefaultResolvers.reverse("reverse");

        Assertions.assertEquals(Component.text("cba"), miniMessage.deserialize("<reverse>abc</reverse>", resolver).compact());
        Assertions.assertEquals(Component.text(""), miniMessage.deserialize("<reverse></reverse>", resolver).compact());
        Assertions.assertEquals(
                Component.empty().append(Component.text("cb", NamedTextColor.RED)).append(Component.text("a")),
                miniMessage.deserialize("<reverse>a<red>bc</red></reverse>", resolver).compact()
        );
        Assertions.assertEquals(
                Component.text("c", NamedTextColor.YELLOW).append(Component.text("b", NamedTextColor.RED)).append(Component.text("a")),
                miniMessage.deserialize("<reverse><yellow>a<red>b</red>c</yellow></reverse>", resolver).compact()
        );
    }
}