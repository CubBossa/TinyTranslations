package de.cubbossa.tinytranslations.util.compiler;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SimpleStringParser<TokenT, TokenValueT, NodeT> {

    private final Queue<Region> regions;
    private final Stack<Marker> openMarkers;
    private final LinkedList<TokenValueT> tokens;
    private int currentToken = 0;

    public SimpleStringParser(List<TokenValueT> tokens) {
        regions = new LinkedList<>();
        openMarkers = new Stack<>();
        this.tokens = new LinkedList<>(tokens);
    }

    public abstract Node parse();

    public abstract @Nullable TokenT getTokenType(@Nullable TokenValueT value);

    public Marker mark() {
        Marker m = new Marker();
        m.start = currentToken;
        openMarkers.push(m);
        return m;
    }

    public void advance() {
        currentToken++;
    }

    public TokenValueT getCurrentToken() {
        if (tokens.size() <= currentToken) {
            return null;
        }
        return tokens.get(currentToken);
    }

    public TokenT lookAhead(int count) {
        if (currentToken + count >= tokens.size()) {
            return null;
        }
        return getTokenType(tokens.get(currentToken + count));
    }

    public String getTokenText() {
        return getCurrentToken().toString();
    }

    public TokenT getTokenType() {
        return getTokenType(getCurrentToken());
    }

    public Node buildTree() {

        List<Node> sib = new ArrayList<>();

        while (!regions.isEmpty()) {
            Region region = regions.poll();
            List<Node> children = new ArrayList<>();
            for (Node n : new ArrayList<>(sib)) {
                if (n.start >= region.start && n.end <= region.end) {
                    sib.remove(n);
                    children.add(n);
                }
            }
            Node n = new Node(region.type, region.start, region.end, children);
            sib.add(n);
        }
        return new Node(null, 0, tokens.size(), new ArrayList<>(sib));
    }

    public class Marker {

        int start = 0;
        boolean incomplete = true;

        public void rollback() {
            while (!openMarkers.peek().equals(this)) {
                openMarkers.pop().incomplete = false;
            }
            regions.removeIf(region -> region.start >= start);
            openMarkers.pop();
            currentToken = start;
            incomplete = false;
        }

        public void dispose() {
            incomplete = false;
            openMarkers.pop();
        }

        public void done(NodeT type) {
            if (!incomplete) {
                throw new IllegalStateException("This marker is not valid");
            }
            if (openMarkers.isEmpty() || !openMarkers.peek().equals(this)) {
                throw new IllegalStateException("Complete or dispose all markers first that were added after this marker.");
            }
            regions.add(new Region(start, currentToken, type));
            incomplete = false;
            openMarkers.pop();
        }

        @Override
        public String toString() {
            return tokens.subList(start, currentToken).stream().map(Objects::toString).collect(Collectors.joining("")) + (incomplete ? "..." : "");
        }
    }

    private class Region {

        final int start;
        final int end;
        final NodeT type;
        final TokenValueT[] tokens;

        private Region(int start, int end, NodeT type) {
            this.start = start;
            this.end = end;
            this.type = type;
            tokens = (TokenValueT[]) SimpleStringParser.this.tokens.subList(start, end).toArray();
        }

        @Override
        public String toString() {
            return "<" + type + ">['" + Arrays.stream(tokens).map(Objects::toString).collect(Collectors.joining("")) + "']";
        }
    }

    @Getter
    public final class Node {
        private final NodeT type;
        private final List<Node> children;
        int start;
        int end;
        private Node parent;
        private String hardCode = null;

        public Node(NodeT type, int start, int end, String hardCode) {
            this(type, start, end, new ArrayList<>());
            this.hardCode = hardCode;
        }

        public Node(NodeT type, int start, int end, List<Node> children) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.children = children;
            for (Node child : this.children) {
                child.parent = this;
            }
        }

        private Node(Region region) {
            this(region.type, region.start, region.end, new ArrayList<>());
        }

        public void replace(String other) {
            replace(new Node(null, start, end, other));
        }

        public void replace(Node other) {
            if (parent == null) {
                return;
            }
            try {
                int index = parent.children.indexOf(this);
                parent.children.set(index, other);
            } catch (Throwable t) {
                parent = null;
            }

        }

        public void tree(StringBuilder builder, int indent) {
            builder.append(" ".repeat(indent)).append(Objects.toString(type, "null")).append("\n");
            for (Node child : children) {
                child.tree(builder, indent + 1);
            }
        }

        public String getText() {
            if (hardCode != null) {
                return hardCode;
            }
            if (start == end) {
                return "";
            }
            StringBuilder s = new StringBuilder();
            int i = start;
            while (i < end) {
                int before = i;
                for (Node child : children) {
                    if (child.start == i) {
                        s.append(child.getText());
                        i = Integer.max(i, child.end);
                    }
                }
                if (before == i) {
                    s.append(tokens.get(i++));
                }
            }
            return s.toString();
        }

        @Override
        public String toString() {
            return getText();
        }
    }
}
