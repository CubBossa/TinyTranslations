package de.cubbossa.tinytranslations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

public class MessageTimings extends TestBase {


    @Test
    void testTimings(@TempDir File dir) {

        translator = TinyTranslations.globalTranslator(dir).fork("test");

        Message prefix = translator.messageBuilder("prefix")
                .withDefault("MyTest").build();
        translator.getStyleSet().put("prefix", "<gray>[<primary>{msg:prefix}</primary>] <text>{slot}</text>");
        Message toTest = translator.messageBuilder("test")
                .withDefault("<prefix>Some message <text_hl>with some </text_hl>content to parse<repeat:3>.</repeat></prefix>")
                .build();

        long now = System.currentTimeMillis();
        for (int i = 0; i < 10_000; i++) {
            render(toTest);
        }
        System.out.println("Time required: " + (System.currentTimeMillis() - now));
    }

}
