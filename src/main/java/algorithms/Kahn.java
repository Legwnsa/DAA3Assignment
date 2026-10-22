package algorithms;

import graph.Graph;
import java.util.Arrays;
import metrics.Metrics;

public final class Kahn {
    private Kahn() {
    }


    public record Result(int[] order, boolean hasCycle) {
        public int count() {
            return order.length;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        if (!g.isDirected()) {
            throw new IllegalArgumentException("topological order needs a directed graph");
        }
        int n = g.n();
        int[] indeg = new int[n];
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;
        for (int v = 0; v < n; v++) {
            indeg[v] = g.inDegree(v);
            if (indeg[v] == 0) {
                queue[tail++] = v;
            }
        }
        long scans = 0;
        while (head < tail) {
            int u = queue[head++];
            for (int i = 0; i < g.degree(u); i++) {
                scans++;
                int v = g.to(u, i);
                if (--indeg[v] == 0) {
                    queue[tail++] = v;
                }
            }
        }
        metrics.edgeScans += scans;
        boolean cycle = tail < n;
        return new Result(cycle ? Arrays.copyOf(queue, tail) : queue, cycle);
    }

    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }
            pos[v] = i;
        }
        for (int id = 0; id < g.m(); id++) {
            if (pos[g.edgeU(id)] >= pos[g.edgeV(id)]) {
                return false;
            }
        }
        return true;
    }
}
