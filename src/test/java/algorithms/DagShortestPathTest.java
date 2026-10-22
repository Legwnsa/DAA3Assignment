package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graph.Graph;
import graph.GraphIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

class DagShortestPathTest {
    private static final long INF = DagShortestPath.INF;

    private static long[] bellmanFord(Graph g) {
        int n = g.n();
        long[] d = new long[n];
        for (int v = 0; v < n; v++) {
            d[v] = g.inDegree(v) == 0 ? 0 : INF;
        }
        for (int round = 0; round < n; round++) {
            boolean changed = false;
            for (int id = 0; id < g.m(); id++) {
                int u = g.edgeU(id);
                int v = g.edgeV(id);
                if (d[u] != INF && d[u] + g.edgeW(id) < d[v]) {
                    d[v] = d[u] + g.edgeW(id);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return d;
    }

    @Test
    void matchesBellmanFordOnRandomDagsWithNegativeWeights() {
        Random rnd = new Random(9);
        for (int t = 0; t < 300; t++) {
            int n = 1 + rnd.nextInt(60);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n), -10, 20);
            Metrics m = new Metrics();
            DagShortestPath.Result r = DagShortestPath.run(g, m);
            long[] ref = bellmanFord(g);
            assertTrue(Arrays.equals(ref, r.dist()), "graph #" + t);
            long sum = 0;
            for (long x : ref) {
                sum += x;
            }
            assertEquals(sum, r.sumDistances());
            assertEquals(g.m(), m.edgeScans, "one relaxation per edge");
            assertEquals(g.m(), m.comparisons);
        }
    }

    @Test
    void restoredPathsAreRealPathsWithTheReportedLength() {
        Random rnd = new Random(21);
        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(40);
            Graph g = TestGraphs.randomDag(rnd, n, 3 * n, -10, 20);
            DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
            for (int v = 0; v < n; v++) {
                List<Integer> path = r.pathTo(v);
                assertEquals(v, path.get(path.size() - 1));
                assertEquals(0, g.inDegree(path.get(0)), "a path starts at a source");
                long len = 0;
                for (int i = 0; i + 1 < path.size(); i++) {
                    len += cheapestEdge(g, path.get(i), path.get(i + 1));
                }
                assertEquals(r.dist()[v], len);
            }
        }
    }

    private static long cheapestEdge(Graph g, int u, int v) {
        long best = INF;
        for (int i = 0; i < g.degree(u); i++) {
            if (g.to(u, i) == v) {
                best = Math.min(best, g.weight(u, i));
            }
        }
        assertTrue(best != INF, "edge " + u + "->" + v + " must exist");
        return best;
    }

    @Test
    void sampleDagWithNegativeEdge() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_dag.txt"));
        DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
        assertTrue(Arrays.equals(new long[] {0, 5, 3, 10, 5, 5}, r.dist()));
        assertEquals(28, r.sumDistances());
        assertEquals(List.of(0, 2, 3, 4), r.pathTo(4));
        assertEquals(List.of(0, 2, 5), r.pathTo(5));
        assertEquals(List.of(0), r.pathTo(0));
    }

    @Test
    void severalSourcesStartAtZero() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, -3);
        g.addEdge(2, 3, 1);
        DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
        assertTrue(Arrays.equals(new long[] {0, 0, -3, -2}, r.dist()));
        assertEquals(List.of(1, 2, 3), r.pathTo(3));
    }

    @Test
    void cycleIsReportedNotComputed() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_cyclic.txt"));
        DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
        assertTrue(r.hasCycle());
        assertNull(r.dist());
        assertTrue(r.pathTo(0).isEmpty());
    }

    @Test
    void emptyGraphAndIsolatedVertices() {
        assertNotNull(DagShortestPath.run(new Graph(0, true), new Metrics()));
        DagShortestPath.Result r = DagShortestPath.run(new Graph(3, true), new Metrics());
        assertEquals(0, r.sumDistances());
        assertEquals(List.of(2), r.pathTo(2));
    }
}
