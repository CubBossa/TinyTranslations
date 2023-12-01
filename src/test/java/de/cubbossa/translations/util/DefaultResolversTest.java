package de.cubbossa.translations.util;

import net.kyori.adventure.text.Component;
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
        TagResolver resolver = TagResolver.resolver("url", DefaultResolvers.shortURL(0, 0));

        Assertions.assertEquals(Component.text(noUrl), miniMessage.deserialize("<url>" + noUrl + "</url>", resolver));
    }

    @Test
    void url2() {
        String url = "https://www.google.com/search?client=firefox-b-d&q=Component";
        String urlShort = "https://www.google.com/sea...ent";
        TagResolver resolver = TagResolver.resolver("url", DefaultResolvers.shortURL(3, 3));

        Assertions.assertEquals(Component.text(urlShort), miniMessage.deserialize("<url>" + url + "</url>", resolver));
    }

    @Test
    void url3() {
        String url = "https://www.google.com/search?client=firefox-b-d&q=Component";
        TagResolver resolver = TagResolver.resolver("url", DefaultResolvers.shortURL(15, 23));

        Assertions.assertEquals(Component.text(url), miniMessage.deserialize("<url>" + url + "</url>", resolver));
    }

    @Test
    void preview() {
        TagResolver resolver = TagResolver.resolver("preview", (argumentQueue, context) -> {
            return DefaultResolvers.preview(argumentQueue.hasNext() ? argumentQueue.pop().asInt().orElse(16) : 16);
        });

        Assertions.assertEquals("aaaaa", plain.apply(miniMessage.deserialize("<preview>aaaaa", resolver)));
        Assertions.assertEquals("", plain.apply(miniMessage.deserialize("<preview:0>aaaaa", resolver)));
        Assertions.assertEquals("a", plain.apply(miniMessage.deserialize("<preview:1>aaaaa", resolver)));
        Assertions.assertEquals("aa", plain.apply(miniMessage.deserialize("<preview:2>aa", resolver)));
        Assertions.assertEquals("aa", plain.apply(miniMessage.deserialize("<preview:2>a<red>aaaa", resolver)));
    }

    @Test
    void toLower() {
        TagResolver resolver = TagResolver.resolver("lowercase", (argumentQueue, context) -> {
            return DefaultResolvers.lower();
        });

        Assertions.assertEquals(Component.text("a"), miniMessage.deserialize("<lowercase>A", resolver));
        Assertions.assertEquals(Component.text("---"), miniMessage.deserialize("<lowercase>---", resolver));
        Assertions.assertEquals(Component.text("aaaaa"), miniMessage.deserialize("<lowercase>AaAaA", resolver));
        Assertions.assertEquals(Component.text("Aa"), miniMessage.deserialize("A<lowercase>A", resolver));
    }
}