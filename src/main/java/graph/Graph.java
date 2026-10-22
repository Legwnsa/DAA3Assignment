package graph;

import java.util.Arrays;

public class Graph {
    private static final int[] empty= new int[0];

    private final int n;
    private final boolean directed;

    private final int[][] adjTo;
    private final int[][] adjW;
    private final int[][] adjId;
    private final int[] deg;
    private final int[] inDeg; // directed graphs only

    private int[] eu = new int[16];
    private int[] ev = new int[16];
    private int[] ew = new int[16];
    private int m;

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got " + n);
        }
        this.n = n;
        this.directed = directed;
        adjTo = new int[n][];
        adjW = new int[n][];
        adjId = new int[n][];
        Arrays.fill(adjTo, empty);
        Arrays.fill(adjW, empty);
        Arrays.fill(adjId, empty);
        deg = new int[n];
        inDeg = directed ? new int[n] : null;
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            throw new IllegalArgumentException("edge (" + u + "," + v + ") out of range 0.." + (n - 1));
        }
        if (m == eu.length) {
            int cap = m * 2;
            eu = Arrays.copyOf(eu, cap);
            ev = Arrays.copyOf(ev, cap);
            ew = Arrays.copyOf(ew, cap);
        }
        int id = m++;
        eu[id] = u;
        ev[id] = v;
        ew[id] = w;
        push(u, v, w, id);
        if (directed) {
            inDeg[v]++;
        } else if (u != v) {
            push(v, u, w, id);
        }
    }

    private void push(int u, int to, int w, int id) {
        int d = deg[u];
        if (d == adjTo[u].length) {
            int cap = Math.max(4, d * 2);
            adjTo[u] = Arrays.copyOf(adjTo[u], cap);
            adjW[u] = Arrays.copyOf(adjW[u], cap);
            adjId[u] = Arrays.copyOf(adjId[u], cap);
        }
        adjTo[u][d] = to;
        adjW[u][d] = w;
        adjId[u][d] = id;
        deg[u] = d + 1;
    }

    public int n() {
        return n;
    }

    public int m() {
        return m;
    }

    public boolean isDirected() {
        return directed;
    }

    public int degree(int u) {
        return deg[u];
    }

    public int to(int u, int i) {
        return adjTo[u][i];
    }

    public int weight(int u, int i) {
        return adjW[u][i];
    }

    public int edgeId(int u, int i) {
        return adjId[u][i];
    }

    public int inDegree(int v) {
        if (!directed) {
            throw new IllegalStateException("in-degree is defined for directed graphs only");
        }
        return inDeg[v];
    }

    public int edgeU(int id) {
        return eu[id];
    }

    public int edgeV(int id) {
        return ev[id];
    }

    public int edgeW(int id) {
        return ew[id];
    }

    public Edge edge(int id) {
        return new Edge(eu[id], ev[id], ew[id]);
    }

    public Graph reverse() {
        if (!directed) {
            return this;
        }
        Graph r = new Graph(n, true);
        for (int id = 0; id < m; id++) {
            r.addEdge(ev[id], eu[id], ew[id]);
        }
        return r;
    }
}
