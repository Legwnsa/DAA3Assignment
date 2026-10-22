package algorithms;

import graph.Graph;
import java.util.Arrays;
import metrics.Metrics;

public final class Kosaraju {
    private Kosaraju() {
    }

    public record Result(int count, int[] component) {
    }

    public static Result run(Graph g, Metrics metrics) {
        int n = g.n();
        int[] stack = new int[n];
        int[] next = new int[n];
        int[] finish = new int[n];
        boolean[] seen = new boolean[n];
        int finished = 0;
        long scans = 0;
        int maxDepth = metrics.maxDepth;

        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }
            int sp = 0;
            stack[sp++] = s;
            seen[s] = true;
            maxDepth = Math.max(maxDepth, sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                if (next[u] < g.degree(u)) {
                    int v = g.to(u, next[u]++);
                    scans++;
                    if (!seen[v]) {
                        seen[v] = true;
                        stack[sp++] = v;
                        maxDepth = Math.max(maxDepth, sp);
                    }
                } else {
                    finish[finished++] = u;
                    sp--;
                }
            }
        }

        Graph rev = g.reverse();
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        Arrays.fill(next, 0);
        int count = 0;
        for (int k = n - 1; k >= 0; k--) {
            int s = finish[k];
            if (comp[s] != -1) {
                continue;
            }
            int sp = 0;
            stack[sp++] = s;
            comp[s] = count;
            maxDepth = Math.max(maxDepth, sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                if (next[u] < rev.degree(u)) {
                    int v = rev.to(u, next[u]++);
                    scans++;
                    if (comp[v] == -1) {
                        comp[v] = count;
                        stack[sp++] = v;
                        maxDepth = Math.max(maxDepth, sp);
                    }
                } else {
                    sp--;
                }
            }
            count++;
        }

        metrics.edgeScans += scans;
        metrics.maxDepth = maxDepth;
        return new Result(count, comp);
    }
}
