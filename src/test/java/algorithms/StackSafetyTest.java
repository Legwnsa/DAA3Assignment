package algorithms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

class StackSafetyTest {
    private static final int N = 100_000;

    @Test
    void sccOnPathOf100000VerticesFinishesWithoutStackOverflow() {
        Graph g = TestGraphs.directedPath(N);
        Metrics m = new Metrics();
        Kosaraju.Result r = assertDoesNotThrow(() -> Kosaraju.run(g, m));
        assertEquals(N, r.count());
        assertEquals(N, m.maxDepth);
        assertEquals(2L * (N - 1), m.edgeScans);
    }

    @Test
    void sccOnOneBigCycleFindsASingleComponent() {
        Graph g = TestGraphs.directedPath(N);
        g.addEdge(N - 1, 0, 1);
        Metrics m = new Metrics();
        Kosaraju.Result r = assertDoesNotThrow(() -> Kosaraju.run(g, m));
        assertEquals(1, r.count());
        assertEquals(N, m.maxDepth);
    }

    @Test
    void kahnOnPathOf100000Vertices() {
        Graph g = TestGraphs.directedPath(N);
        Kahn.Result r = assertDoesNotThrow(() -> Kahn.run(g, new Metrics()));
        assertEquals(N, r.count());
    }
}
