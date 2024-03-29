package de.cubbossa.tinytranslations;

import java.util.Stack;

public class MessageReferenceLoopException extends RuntimeException {

    public MessageReferenceLoopException(Throwable cause) {
        super(cause);
    }

    public MessageReferenceLoopException(Message origin, Stack<String> path) {
        super(String.format("A message reference loop was detected. Cannot resolve infinite message loop for '" + origin.key() + "'.\n" +
                " -> " + String.join(" -> ", path)) + " -> (msg) " + origin.key());
    }
}
