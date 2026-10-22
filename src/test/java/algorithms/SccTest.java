package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graph.Graph;
import graph.GraphIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

class SccTest {

    private static boolean[][] reachability(int n, int[][] adj) {
        boolean[][] reach = new boolean[n][n];
        for (int s = 0; s < n; s++) {
            ArrayDeque<Integer> q = new ArrayDeque<>();
            reach[s][s] = true;
            q.add(s);
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int v : adj[u]) {
                    if (!reach[s][v]) {
                        reach[s][v] = true;
                        q.add(v);
                    }
                }
            }
        }
        return reach;
    }

    private static void assertMatchesBruteForce(Graph g, SccResultView r) {
        int n = g.n();
        int[] outDeg = new int[n];
        int[] inDeg = new int[n];
        for (int id = 0; id < g.m(); id++) {
            outDeg[g.edgeU(id)]++;
            inDeg[g.edgeV(id)]++;
        }
        int[][] fwd = new int[n][];
        int[][] bwd = new int[n][];
        for (int v = 0; v < n; v++) {
            fwd[v] = new int[outDeg[v]];
            bwd[v] = new int[inDeg[v]];
        }
        int[] fo = new int[n];
        int[] bo = new int[n];
        for (int id = 0; id < g.m(); id++) {
            int u = g.edgeU(id);
            int v = g.edgeV(id);
            fwd[u][fo[u]++] = v;
            bwd[v][bo[v]++] = u;
        }
        boolean[][] f = reachability(n, fwd);
        boolean[][] b = reachability(n, bwd);

        int expectedCount = 0;
        for (int u = 0; u < n; u++) {
            boolean first = true;
            for (int v = 0; v < n; v++) {
                boolean same = f[u][v] && b[u][v];
                assertEquals(same, r.component[u] == r.component[v], "pair " + u + "," + v);
                if (same && v < u) {
                    first = false;
                }
            }
            if (first) {
                expectedCount++;
            }
        }
        assertEquals(expectedCount, r.count);
    }

    private record SccResultView(int count, int[] component) {
    }

    @Test
    void matchesBruteForceOnRandomGraphsUpTo200Vertices() {
        Random rnd = new Random(5);
        for (int t = 0; t < 150; t++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(rnd, n, m, 1, 9);
            Kosaraju.Result r = Kosaraju.run(g, new Metrics());
            assertMatchesBruteForce(g, new SccResultView(r.count(), r.component()));
        }
    }

    @Test
    void sampleCyclicGraphHasThreeComponents() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_cyclic.txt"));
        Kosaraju.Result r = Kosaraju.run(g, new Metrics());
        assertEquals(3, r.count());
        int[] c = r.component();
        assertEquals(c[0], c[1]);
        assertEquals(c[1], c[2]);
        assertEquals(c[3], c[4]);
        assertEquals(c[4], c[5]);
        assertTrue(c[0] != c[3] && c[3] != c[6] && c[0] != c[6]);
    }

    @Test
    void scansEveryEdgeTwiceAndRecordsDepth() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_cyclic.txt"));
        Metrics m = new Metrics();
        Kosaraju.run(g, m);
        assertEquals(2L * g.m(), m.edgeScans);
        assertTrue(m.maxDepth >= 1 && m.maxDepth <= g.n());
        assertEquals(0, m.dsuOps);
    }

    @Test
    void edgeCasesDoNotCrash() {
        assertEquals(0, Kosaraju.run(new Graph(0, true), new Metrics()).count());
        Graph g = new Graph(3, true);
        g.addEdge(1, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(0, 2, 7);
        Kosaraju.Result r = Kosaraju.run(g, new Metrics());
        assertEquals(3, r.count());
    }
}
