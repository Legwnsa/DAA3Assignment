package graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

import graph.Graph;
import graph.GraphIO;
import org.junit.jupiter.api.Test;

class GraphIOTest {

    @Test
    void readsUndirectedFileWithLoopsAndParallelEdges() throws IOException {
        Graph g = GraphIO.read(Path.of("data/city_disconnected.txt"));
        assertEquals(8, g.n());
        assertEquals(7, g.m());
        assertFalse(g.isDirected());
        assertEquals(3, g.degree(0));
        assertEquals(2, g.degree(3));
        assertEquals(0, g.degree(5));
        assertEquals(0, g.degree(7));
    }

    @Test
    void readsDirectedFileAndKeepsInDegrees() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_dag.txt"));
        assertTrue(g.isDirected());
        assertEquals(6, g.n());
        assertEquals(9, g.m());
        assertEquals(0, g.inDegree(0));
        assertEquals(2, g.inDegree(3));
        assertEquals(2, g.inDegree(5));
    }

    @Test
    void reverseSwapsEveryEdge() throws IOException {
        Graph g = GraphIO.read(Path.of("data/jobs_dag.txt"));
        Graph r = g.reverse();
        assertEquals(g.m(), r.m());
        for (int id = 0; id < g.m(); id++) {
            assertEquals(g.edgeU(id), r.edgeV(id));
            assertEquals(g.edgeV(id), r.edgeU(id));
            assertEquals(g.edgeW(id), r.edgeW(id));
        }
    }

    @Test
    void graphWithoutEdgesIsFine() throws IOException {
        Graph g = GraphIO.read(new StringReader("3 0 undirected\n"));
        assertEquals(3, g.n());
        assertEquals(0, g.m());
    }

    @Test
    void badInputIsRejectedWithAClearError() {
        assertThrows(IllegalArgumentException.class, () -> GraphIO.read(new StringReader("")));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.read(new StringReader("3 1 sideways\n0 1 1\n")));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.read(new StringReader("3 2 directed\n0 1 1\n")));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.read(new StringReader("3 1 directed\n0 3 1\n")));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.read(new StringReader("3 1 directed\n0 1\n")));
    }
}
