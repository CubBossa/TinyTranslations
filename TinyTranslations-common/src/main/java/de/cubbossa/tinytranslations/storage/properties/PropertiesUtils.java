package de.cubbossa.tinytranslations.storage.properties;


import de.cubbossa.tinytranslations.storage.StorageEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesUtils {

    private static final String SEPARATOR_FORMAT = " = ";

    private static final Pattern COMMENT = Pattern.compile("^[!#].*$");
    private static final Pattern LINE = Pattern.compile("^([a-zA-Z0-9._-]+)( *[:=] *)(.*)$");
    private static final Pattern MULTILINE = Pattern.compile("^.+[^\\\\]+(\\\\\\\\)*\\\\$");

    public static List<StorageEntry> loadProperties(Reader file) throws IOException {
        List<StorageEntry> entries = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(file)) {
            String key = null;
            boolean lineBroke = false;
            List<String> comments = new LinkedList<>();
            List<String> values = new LinkedList<>();

            int lineIndex = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineIndex++;

                // only collect comments if we are not in a multiline context
                if (!lineBroke) {
                    // collect empty lines
                    if (line.isEmpty()) {
                        comments.add("");
                        continue;
                    }
                    // collect comments
                    if (COMMENT.matcher(line).matches()) {
                        comments.add(line.substring(1));
                        continue;
                    }
                }
                if (lineBroke) {
                    lineBroke = MULTILINE.matcher(line).matches();
                    if (lineBroke) {
                        line = line.replaceAll("^(.+)\\\\$", "$1");
                    }
                    values.add(line);
                    if (lineBroke) {
                        continue;
                    }
                } else {
                    Matcher matcher = LINE.matcher(line);
                    if (matcher.matches()) {
                        key = matcher.group(1);
                        String value = matcher.group(3);

                        lineBroke = MULTILINE.matcher(line).matches();
                        if (lineBroke) {
                            value = value.replaceAll("^(.+)\\\\$", "$1");
                        }
                        values.add(value);
                        if (lineBroke) {
                            continue;
                        }
                    } else {
                        throw new IOException("Could not parse line " + (lineIndex - 1) + " of properties file.\n> " + line);
                    }
                }
                String merged = "";
                if (values.size() > 1) {
                    merged = "\n" + String.join("\n", values.subList(1, values.size()))
                            .stripIndent();
                }
                String first = values.get(0);
                first = first.startsWith("\"") && first.endsWith("\"") ? first.substring(1, first.length() - 1) : first;
                merged = first + merged;

                entries.add(new StorageEntry(key, merged, new ArrayList<>(comments)));

                key = null;
                values.clear();
                comments.clear();
            }
        }
        return entries;
    }

    public static void write(Writer writer, List<StorageEntry> entries) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            for (StorageEntry entry : entries) {
                for (String comment : entry.comments()) {
                    bufferedWriter.write("#".repeat(comment.isEmpty() ? 0 : 1) + comment + "\n");
                }
                List<String> values = List.of(entry.value().split("\n"));
                String line = entry.key() + SEPARATOR_FORMAT + values.get(0);
                if (values.size() > 1) {
                    bufferedWriter.write(line + "\\\n");
                    String indent = " ".repeat(entry.key().length() + SEPARATOR_FORMAT.length());
                    for (int i = 1; i < values.size(); i++) {
                        bufferedWriter.write(indent + values.get(i) + (i == values.size() - 1 ? "\n" : "\\\n"));
                    }
                } else {
                    bufferedWriter.write(line + "\n");
                }
            }
        }
    }
}
