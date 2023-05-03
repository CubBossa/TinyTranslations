package de.cubbossa.translations;

public class MessageReferenceLoopException extends RuntimeException {

    public MessageReferenceLoopException(Message message) {
        super("A message had a reference to itself as placeholder. Cannot resolve infinite message loop for '" + message + "'.");
    }
}
