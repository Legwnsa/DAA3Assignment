package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DSUTest {

    @Test
    void findAgreesWithReferenceAfterRandomUnions() {
        Random rnd = new Random(1);
        for (int trial = 0; trial < 200; trial++) {
            int n = 1 + rnd.nextInt(200);
            DSU dsu = new DSU(n);
            int[] label = new int[n];
            for (int i = 0; i < n; i++) {
                label[i] = i;
            }
            int ops = rnd.nextInt(3 * n);
            for (int k = 0; k < ops; k++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                boolean refMerged = label[a] != label[b];
                if (refMerged) {
                    int from = label[b];
                    int to = label[a];
                    for (int i = 0; i < n; i++) {
                        if (label[i] == from) {
                            label[i] = to;
                        }
                    }
                }
                assertEquals(refMerged, dsu.union(a, b));
            }
            Set<Integer> distinct = new HashSet<>();
            for (int x = 0; x < n; x++) {
                distinct.add(label[x]);
                for (int y = 0; y < n; y++) {
                    assertEquals(label[x] == label[y], dsu.find(x) == dsu.find(y));
                }
            }
            assertEquals(distinct.size(), dsu.components());
        }
    }

    @Test
    void countsFindCallsAndSuccessfulUnionsOnly() {
        DSU dsu = new DSU(5);
        assertEquals(5, dsu.components());
        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(2, 3));
        assertFalse(dsu.union(1, 0));
        assertTrue(dsu.union(1, 3));
        dsu.find(0);
        dsu.find(4);
        assertEquals(3, dsu.unionCalls());
        assertEquals(2, dsu.findCalls());
        assertEquals(5, dsu.operations());
        assertEquals(2, dsu.components());
    }

    @Test
    void makeSetResetsEverything() {
        DSU dsu = new DSU(3);
        dsu.union(0, 1);
        dsu.find(2);
        dsu.makeSet(10);
        assertEquals(10, dsu.components());
        assertEquals(0, dsu.operations());
        assertFalse(dsu.find(0) == dsu.find(1));
    }

    @Test
    void longChainOfUnionsIsFastAndStackSafe() {
        int n = 200_000;
        DSU dsu = new DSU(n);
        for (int i = 1; i < n; i++) {
            assertTrue(dsu.union(i - 1, i));
        }
        assertEquals(1, dsu.components());
        assertEquals(dsu.find(0), dsu.find(n - 1));
    }

    @Test
    void emptyDsuIsFine() {
        DSU dsu = new DSU(0);
        assertEquals(0, dsu.components());
    }
}
