package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NanoMessageCompilerTest {

    @Test
    void parseSpaceRemoval() {

        StringPair[] strings = {
                new StringPair("{a : b : c}", "<a:b:c>"),
                new StringPair("{ a :    b:c   }", "<a:b:c>"),
                new StringPair("<a :    b:c   />", "<a:b:c/>"),
                new StringPair("<a :    b:c   ></a>", "<a:b:c></a>"),
                new StringPair("{a ?b :c}", "<choice:'<a>':'b':'c'>"),
                new StringPair("{ a ?  b :  c }", "<choice:'<a>':'b':'c'>"),
                new StringPair("{ a.b.c:'#.#' }", "<object_notation:'a.b.c':'#.#'>"),
                new StringPair("{ a.b.c ? '<a>' : b :  c }", "<choice:'<object_notation:'a.b.c'>':'<a>':'b':'c'>"),
                new StringPair("{ a.b.c }", "<object_notation:'a.b.c'>"),
//                new StringPair("<a.b.c>lol</a>", "<object_notation:'a.b.c':'#.#'>"),
        };

        NanoMessageCompiler compiler = new NanoMessageCompiler();
        for (StringPair string : strings) {
            Assertions.assertEquals(
                    string.after,
                    compiler.compile(string.before),
                    string.before
            );
        }
    }

    private record StringPair(String before, String after) {
    }
}
