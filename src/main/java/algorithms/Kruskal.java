package algorithms;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import metrics.Metrics;


public final class Kruskal {
    private Kruskal() {
    }

    public static MstResult run(Graph g, Metrics metrics) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST needs an undirected graph");
        }
        int n = g.n();
        int m = g.m();

        int[] w = new int[m];
        int[] order = new int[m];
        for (int i = 0; i < m; i++) {
            w[i] = g.edgeW(i);
            order[i] = i;
        }
        mergeSort(order, w, metrics);

        DSU dsu = new DSU(n);
        List<Edge> tree = new ArrayList<>(Math.max(0, n - 1));
        long total = 0;
        for (int k = 0; k < m && tree.size() < n - 1; k++) {
            metrics.edgeScans++;
            int id = order[k];
            int u = g.edgeU(id);
            int v = g.edgeV(id);
            if (dsu.find(u) != dsu.find(v)) {
                dsu.union(u, v);
                tree.add(g.edge(id));
                total += w[id];
            }
        }
        metrics.dsuOps += dsu.operations();
        return new MstResult(total, dsu.components(), tree);
    }

    private static void mergeSort(int[] ids, int[] w, Metrics metrics) {
        int len = ids.length;
        int[] src = ids;
        int[] dst = new int[len];
        long cmp = 0;
        for (int width = 1; width < len; width <<= 1) {
            for (int lo = 0; lo < len; lo += 2 * width) {
                int mid = Math.min(lo + width, len);
                int hi = Math.min(lo + 2 * width, len);
                int i = lo;
                int j = mid;
                int k = lo;
                while (i < mid && j < hi) {
                    cmp++;
                    if (w[src[i]] <= w[src[j]]) {
                        dst[k++] = src[i++];
                    } else {
                        dst[k++] = src[j++];
                    }
                }
                while (i < mid) {
                    dst[k++] = src[i++];
                }
                while (j < hi) {
                    dst[k++] = src[j++];
                }
            }
            int[] t = src;
            src = dst;
            dst = t;
        }
        if (src != ids) {
            System.arraycopy(src, 0, ids, 0, len);
        }
        metrics.comparisons += cmp;
    }
}
