package algorithms;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import metrics.Metrics;


public final class Prim {
    private Prim() {
    }

    public static MstResult run(Graph g, Metrics metrics) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST needs an undirected graph");
        }
        int n = g.n();
        long[] cmp = {0};
        PriorityQueue<Long> pq = new PriorityQueue<>((a, b) -> {
            cmp[0]++;
            return Long.compare(a, b);
        });
        boolean[] inTree = new boolean[n];
        List<Edge> tree = new ArrayList<>(Math.max(0, n - 1));
        long total = 0;
        int components = 0;
        long scans = 0;

        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            scans += scan(g, start, inTree, pq);
            while (!pq.isEmpty()) {
                long key = pq.poll();
                int id = (int) (key & 0xFFFFFFFFL);
                int u = g.edgeU(id);
                int v = g.edgeV(id);
                int next = inTree[u] ? v : u;
                if (inTree[next]) {
                    continue;
                }
                inTree[next] = true;
                tree.add(g.edge(id));
                total += g.edgeW(id);
                scans += scan(g, next, inTree, pq);
            }
        }
        metrics.edgeScans += scans;
        metrics.comparisons += cmp[0];
        return new MstResult(total, components, tree);
    }

    private static int scan(Graph g, int x, boolean[] inTree, PriorityQueue<Long> pq) {
        int d = g.degree(x);
        for (int i = 0; i < d; i++) {
            if (!inTree[g.to(x, i)]) {
                pq.add(((long) g.weight(x, i) << 32) + g.edgeId(x, i));
            }
        }
        return d;
    }
}
