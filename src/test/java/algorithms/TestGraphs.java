package algorithms;

import graph.Graph;
import java.util.Random;

public final class TestGraphs {
    private TestGraphs() {
    }

    public static Graph randomConnected(Random rnd, int n, int extra, int maxW) {
        Graph g = new Graph(n, false);
        for (int v = 1; v < n; v++) {
            g.addEdge(rnd.nextInt(v), v, 1 + rnd.nextInt(maxW));
        }
        for (int i = 0; i < extra; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(maxW));
        }
        return g;
    }

    public static Graph randomUndirected(Random rnd, int n, int m, int maxW) {
        Graph g = new Graph(n, false);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(maxW));
        }
        return g;
    }

    public static Graph randomDirected(Random rnd, int n, int m, int minW, int maxW) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), minW + rnd.nextInt(maxW - minW + 1));
        }
        return g;
    }

    public static Graph randomDag(Random rnd, int n, int m, int minW, int maxW) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = rank[i];
            rank[i] = rank[j];
            rank[j] = t;
        }
        Graph g = new Graph(n, true);
        if (n < 2) {
            return g;
        }
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b) {
                continue;
            }
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, minW + rnd.nextInt(maxW - minW + 1));
        }
        return g;
    }

    public static Graph directedPath(int n) {
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) {
            g.addEdge(i, i + 1, 1);
        }
        return g;
    }
}
