package de.cubbossa.translations;

import org.apache.commons.lang.SerializationException;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestAnnotations {


    @Test
    public void testAnnotations() throws IOException {

        registerAnnotatedLanguageClass(TestMessages.class);
    }

    public void registerAnnotatedLanguageClass(Class<?> annotatedClass) throws IOException {
        MessageFile messageFile = annotatedClass.getAnnotation(MessageFile.class);
        if (messageFile == null) {
            throw new IllegalArgumentException("To load a class as message class, it has to be annotated with MessageFile.class.");
        }
        Field[] messages = Arrays.stream(annotatedClass.getDeclaredFields())
                .filter(field -> field.getType().equals(Message.class))
                .filter(field -> field.getAnnotation(MessageMeta.class) != null)
                .toArray(Field[]::new);


        for (Field messageField : messages) {
            try {
                Message message = (Message) messageField.get(annotatedClass);
                MessageMeta value = messageField.getAnnotation(MessageMeta.class);

                List<String> comments = Arrays.stream(value.comment()).collect(Collectors.toCollection(ArrayList::new));
                comments.add("Valid placeholders: " + Arrays.stream(value.placeholders())
                        .map(s -> "<" + s + ">")
                        .collect(Collectors.joining(", ")));
                System.out.println("#### Comments:\n" + comments.stream().map(s -> "# " + s).collect(Collectors.joining("\n")));
                System.out.println("#### Value:\n" + message.getKey() + ": " + value.value());

            } catch (Exception e) {
                throw new SerializationException("Could not write message '" + messageField.getName() + "' to file. Skipping.");
            }
        }
    }
}
