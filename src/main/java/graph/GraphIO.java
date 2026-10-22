package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GraphIO {
    private GraphIO() {
    }

    public static Graph read(Path file) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return read(in);
        }
    }

    public static Graph read(Reader reader) throws IOException {
        BufferedReader in = reader instanceof BufferedReader b ? b : new BufferedReader(reader);

        String header = nextLine(in);
        if (header == null) {
            throw new IllegalArgumentException("empty input: expected 'n m directed|undirected'");
        }
        String[] h = header.split("\\s+");
        if (h.length != 3) {
            throw new IllegalArgumentException("bad header (expected 'n m directed|undirected'): " + header);
        }
        int n = parseInt(h[0], header);
        int m = parseInt(h[1], header);
        boolean directed;
        if (h[2].equalsIgnoreCase("directed")) {
            directed = true;
        } else if (h[2].equalsIgnoreCase("undirected")) {
            directed = false;
        } else {
            throw new IllegalArgumentException("third header field must be directed or undirected: " + header);
        }
        if (m < 0) {
            throw new IllegalArgumentException("m must be >= 0: " + header);
        }

        Graph g = new Graph(n, directed);
        for (int i = 0; i < m; i++) {
            String line = nextLine(in);
            if (line == null) {
                throw new IllegalArgumentException("expected " + m + " edges, found only " + i);
            }
            String[] t = line.split("\\s+");
            if (t.length != 3) {
                throw new IllegalArgumentException("bad edge line (expected 'u v w'): " + line);
            }
            g.addEdge(parseInt(t[0], line), parseInt(t[1], line), parseInt(t[2], line));
        }
        return g;
    }

    private static String nextLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && line.charAt(0) != '#') {
                return line;
            }
        }
        return null;
    }

    private static int parseInt(String s, String line) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not an integer '" + s + "' in line: " + line);
        }
    }
}
