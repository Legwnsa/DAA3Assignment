package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

class MstTest {

    static int countComponents(Graph g) {
        boolean[] seen = new boolean[g.n()];
        int comps = 0;
        int[] stack = new int[g.n() + 1];
        for (int s = 0; s < g.n(); s++) {
            if (seen[s]) {
                continue;
            }
            comps++;
            int sp = 0;
            stack[sp++] = s;
            seen[s] = true;
            while (sp > 0) {
                int u = stack[--sp];
                for (int i = 0; i < g.degree(u); i++) {
                    int v = g.to(u, i);
                    if (!seen[v]) {
                        seen[v] = true;
                        stack[sp++] = v;
                    }
                }
            }
        }
        return comps;
    }

    @Test
    void kruskalAndPrimAgreeOn100RandomConnectedGraphs() {
        Random rnd = new Random(7);
        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(60);
            Graph g = TestGraphs.randomConnected(rnd, n, rnd.nextInt(4 * n), 1 + rnd.nextInt(30));
            MstResult k = Kruskal.run(g, new Metrics());
            MstResult p = Prim.run(g, new Metrics());
            assertEquals(k.totalWeight(), p.totalWeight(), "graph #" + t);
            assertEquals(n - 1, k.edgeCount());
            assertEquals(n - 1, p.edgeCount());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
            assertEquals(k.totalWeight(), sum(k), "Kruskal tree edges sum to the reported weight");
            assertEquals(p.totalWeight(), sum(p), "Prim tree edges sum to the reported weight");
        }
    }

    @Test
    void disconnectedRandomGraphsGiveAForest() {
        Random rnd = new Random(11);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(60);
            Graph g = TestGraphs.randomUndirected(rnd, n, rnd.nextInt(n + 1), 20);
            int comps = countComponents(g);
            MstResult k = Kruskal.run(g, new Metrics());
            MstResult p = Prim.run(g, new Metrics());
            assertEquals(comps, k.components());
            assertEquals(comps, p.components());
            assertEquals(n - comps, k.edgeCount());
            assertEquals(n - comps, p.edgeCount());
            assertEquals(k.totalWeight(), p.totalWeight());
        }
    }

    @Test
    void sampleConnectedGraph() throws IOException {
        Graph g = GraphIO.read(Path.of("data/city_connected.txt"));
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(33, k.totalWeight());
        assertEquals(33, p.totalWeight());
        assertEquals(5, k.edgeCount());
        assertEquals(1, p.components());
    }

    @Test
    void sampleDisconnectedGraphWithLoopsAndParallelEdges() throws IOException {
        Graph g = GraphIO.read(Path.of("data/city_disconnected.txt"));
        for (MstResult r : new MstResult[] {Kruskal.run(g, new Metrics()), Prim.run(g, new Metrics())}) {
            assertEquals(10, r.totalWeight());
            assertEquals(5, r.components());
            assertEquals(3, r.edgeCount());
        }
    }

    @Test
    void countersAreCountedInsideTheAlgorithms() throws IOException {
        Graph g = GraphIO.read(Path.of("data/city_connected.txt"));
        Metrics mk = new Metrics();
        MstResult k = Kruskal.run(g, mk);
        assertEquals(2 * mk.edgeScans + k.edgeCount(), mk.dsuOps);
        assertTrue(mk.edgeScans <= g.m());
        assertTrue(mk.comparisons > 0);

        Metrics mp = new Metrics();
        Prim.run(g, mp);
        assertEquals(2L * g.m(), mp.edgeScans);
        assertTrue(mp.comparisons > 0);
        assertEquals(0, mp.dsuOps);
    }

    @Test
    void emptyAndTinyGraphsDoNotCrash() {
        Graph empty = new Graph(0, false);
        assertEquals(0, Kruskal.run(empty, new Metrics()).components());
        assertEquals(0, Prim.run(empty, new Metrics()).components());

        Graph loopOnly = new Graph(1, false);
        loopOnly.addEdge(0, 0, 5);
        MstResult k = Kruskal.run(loopOnly, new Metrics());
        MstResult p = Prim.run(loopOnly, new Metrics());
        assertEquals(0, k.totalWeight());
        assertEquals(0, p.totalWeight());
        assertEquals(1, k.components());
        assertEquals(1, p.components());

        Graph isolated = new Graph(4, false);
        assertEquals(4, Kruskal.run(isolated, new Metrics()).components());
        assertEquals(4, Prim.run(isolated, new Metrics()).components());
    }

    @Test
    void directedGraphIsRejected() {
        Graph d = new Graph(2, true);
        d.addEdge(0, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> Kruskal.run(d, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> Prim.run(d, new Metrics()));
    }

    private static long sum(MstResult r) {
        long s = 0;
        for (Edge e : r.edges()) {
            s += e.w();
        }
        return s;
    }
}
