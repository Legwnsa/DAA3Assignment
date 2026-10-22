package algorithms;

public final class DSU {
    private int[] parent;
    private int[] rank;
    private int components;
    private long findCalls;
    private long unionCalls;

    public DSU(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        components = n;
        findCalls = 0;
        unionCalls = 0;
    }

    public int find(int x) {
        findCalls++;
        return root(x);
    }

    public boolean union(int x, int y) {
        int rx = root(x);
        int ry = root(y);
        if (rx == ry) {
            return false;
        }
        unionCalls++;
        if (rank[rx] < rank[ry]) {
            int t = rx;
            rx = ry;
            ry = t;
        }
        parent[ry] = rx;
        if (rank[rx] == rank[ry]) {
            rank[rx]++;
        }
        components--;
        return true;
    }

    public int components() {
        return components;
    }

    public long findCalls() {
        return findCalls;
    }

    public long unionCalls() {
        return unionCalls;
    }

    public long operations() {
        return findCalls + unionCalls;
    }

    private int root(int x) {
        int r = x;
        while (parent[r] != r) {
            r = parent[r];
        }
        while (parent[x] != r) {
            int next = parent[x];
            parent[x] = r;
            x = next;
        }
        return r;
    }
}
