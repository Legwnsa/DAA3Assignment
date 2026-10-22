package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graph.Graph;
import graph.GraphIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

class TopologicalOrderTest {

    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(3);
        for (int t = 0; t < 200; t++) {
            int n = 1 + rnd.nextInt(100);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n), 1, 9);
            Metrics m = new Metrics();
            Kahn.Result r = Kahn.run(g, m);
            assertFalse(r.hasCycle());
            assertEquals(n, r.count());
            assertTrue(Kahn.isValidOrder(g, r.order()), "pos[u] < pos[v] for every edge");
            assertEquals(g.m(), m.edgeScans, "Kahn scans every edge once");
        }
    }

    @Test
    void sampleDagGivesValidOrder() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_dag.txt"));
        Kahn.Result r = Kahn.run(g, new Metrics());
        assertFalse(r.hasCycle());
        assertTrue(Kahn.isValidOrder(g, r.order()));
    }

    @Test
    void cycleIsDetectedInsteadOfReturningAWrongOrder() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_cyclic.txt"));
        Kahn.Result r = Kahn.run(g, new Metrics());
        assertTrue(r.hasCycle());
        assertTrue(r.count() < g.n());
        assertFalse(Kahn.isValidOrder(g, r.order()), "a partial order is never a valid full order");
    }

    @Test
    void cycleHiddenBehindAcyclicPartIsDetected() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 1, 1);
        g.addEdge(3, 4, 1);
        Kahn.Result r = Kahn.run(g, new Metrics());
        assertTrue(r.hasCycle());
        assertEquals(1, r.count());
    }

    @Test
    void selfLoopIsACycle() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 1, 1);
        assertTrue(Kahn.run(g, new Metrics()).hasCycle());
    }

    @Test
    void validatorRejectsWrongOrders() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        assertTrue(Kahn.isValidOrder(g, new int[] {0, 1, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[] {2, 1, 0}));
        assertFalse(Kahn.isValidOrder(g, new int[] {0, 1}));
        assertFalse(Kahn.isValidOrder(g, new int[] {0, 0, 2}));
    }

    @Test
    void emptyGraphAndUndirectedInput() {
        Kahn.Result r = Kahn.run(new Graph(0, true), new Metrics());
        assertFalse(r.hasCycle());
        assertEquals(0, r.count());
        assertThrows(IllegalArgumentException.class, () -> Kahn.run(new Graph(2, false), new Metrics()));
    }
}
