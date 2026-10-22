package algorithms;

import graph.Graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import metrics.Metrics;

public final class DagShortestPath {
    public static final long INF = Long.MAX_VALUE;

    private DagShortestPath() {
    }


    public record Result(boolean hasCycle, long[] dist, int[] parent, long sumDistances) {

        public List<Integer> pathTo(int v) {
            List<Integer> path = new ArrayList<>();
            if (hasCycle || dist[v] == INF) {
                return path;
            }
            for (int x = v; x != -1; x = parent[x]) {
                path.add(x);
            }
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        return run(g, Kahn.run(g, new Metrics()), metrics);
    }

    public static Result run(Graph g, Kahn.Result topo, Metrics metrics) {
        if (topo.hasCycle()) {
            return new Result(true, null, null, 0);
        }
        int n = g.n();
        long[] dist = new long[n];
        int[] parent = new int[n];
        for (int v = 0; v < n; v++) {
            parent[v] = -1;
            dist[v] = g.inDegree(v) == 0 ? 0 : INF;
        }
        long scans = 0;
        long cmp = 0;
        for (int u : topo.order()) {
            long du = dist[u];
            if (du == INF) {
                continue;
            }
            for (int i = 0; i < g.degree(u); i++) {
                scans++;
                int v = g.to(u, i);
                long cand = du + g.weight(u, i);
                cmp++;
                if (cand < dist[v]) {
                    dist[v] = cand;
                    parent[v] = u;
                }
            }
        }
        metrics.edgeScans += scans;
        metrics.comparisons += cmp;
        long sum = 0;
        for (int v = 0; v < n; v++) {
            if (dist[v] != INF) {
                sum += dist[v];
            }
        }
        return new Result(false, dist, parent, sum);
    }
}
