package de.cubbossa.translations;

@MessageFile(author = "CubBossa")
@MessageGroupMeta(path = "general", comment = "a group comment")
public class TestMessages {

    @MessageMeta(value = "This is just an example to test annotations.", comment = {"multi", "line", "comment"}, placeholders = {"error", "ms"})
    public static final Message HELLO_WORLD = new Message("general.hello_world");
}
