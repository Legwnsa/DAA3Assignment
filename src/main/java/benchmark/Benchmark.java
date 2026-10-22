package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Graph;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import metrics.Metrics;


public final class Benchmark {
    static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    static final int[] DENSITIES = {4, 20};
    static final int RUNS = 5;

    private record Row(String algorithm, String graph, int n, int m, double timeMs, Metrics metrics, long result) {
    }

    private record Measured<T>(T result, Metrics metrics, double medianMs) {
    }

    @FunctionalInterface
    private interface Task<T> {
        T run(Metrics metrics);
    }

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        List<Row> rows = new ArrayList<>();

        System.out.printf("%-14s %7s | %10s %10s | %5s %5s | %10s %10s | %s%n", "graph", "m", "MST", "ref", "SCC", "ref", "DAG sum", "ref", "status");
        for (int n : SIZES) {
            for (int d : DENSITIES) {
                runOne(n, d, rows);
            }
        }

        check(rows.size() == 50, "expected 50 CSV rows, got " + rows.size());
        writeCsv(out, rows);
        System.out.println("ALL CHECKS PASSED - wrote " + rows.size() + " rows to " + out);
    }

    private static void runOne(int n, int density, List<Row> rows) {
        int m = density * n;
        String name = "n" + n + (density == 4 ? "_sparse" : "_dense");
        long[] ref = ReferenceValues.lookup(n, m);

        Measured<MstResult> kruskal;
        Measured<MstResult> prim;
        {
            Graph g = GraphGenerator.undirected(n, m);
            check(g.m() == m, name + ": generator produced " + g.m() + " edges");
            kruskal = measure(g, mt -> Kruskal.run(g, mt));
            prim = measure(g, mt -> Prim.run(g, mt));
            rows.add(row("Kruskal", name, g, kruskal, kruskal.result().totalWeight()));
            rows.add(row("Prim", name, g, prim, prim.result().totalWeight()));
        }
        check(kruskal.result().totalWeight() == prim.result().totalWeight(),
                name + ": MST MISMATCH Kruskal=" + kruskal.result().totalWeight()
                        + " Prim=" + prim.result().totalWeight());
        check(kruskal.result().components() == 1 && prim.result().components() == 1,
                name + ": the generated undirected graph must be connected");
        check(kruskal.result().edgeCount() == n - 1 && prim.result().edgeCount() == n - 1,
                name + ": a spanning tree must have n-1 edges");

        Measured<Kosaraju.Result> scc;
        {
            Graph g = GraphGenerator.directed(n, m);
            scc = measure(g, mt -> Kosaraju.run(g, mt));
            rows.add(row("Kosaraju", name, g, scc, scc.result().count()));
            check(scc.metrics().edgeScans == 2L * m,
                    name + ": Kosaraju must scan 2m edges, scanned " + scc.metrics().edgeScans);
        }

        Measured<Kahn.Result> kahn;
        Measured<DagShortestPath.Result> dag;
        {
            Graph g = GraphGenerator.dag(n, m);
            kahn = measure(g, mt -> Kahn.run(g, mt));
            check(!kahn.result().hasCycle() && Kahn.isValidOrder(g, kahn.result().order()),
                    name + ": Kahn returned an invalid topological order");
            dag = measure(g, mt -> DagShortestPath.run(g, kahn.result(), mt));
            rows.add(row("Kahn", name, g, kahn, kahn.result().count()));
            rows.add(row("DagShortestPath", name, g, dag, dag.result().sumDistances()));
            check(kahn.metrics().edgeScans == m, name + ": Kahn must scan m edges");
            check(dag.metrics().edgeScans == m, name + ": DAG relaxation must scan m edges");
        }

        boolean ok = kruskal.result().totalWeight() == ref[0]
                && scc.result().count() == ref[1]
                && dag.result().sumDistances() == ref[2];
        System.out.printf("%-14s %7d | %10d %10d | %5d %5d | %10d %10d | %s%n",
                name, m, kruskal.result().totalWeight(), ref[0], scc.result().count(), ref[1],
                dag.result().sumDistances(), ref[2], ok ? "OK" : "MISMATCH");
        check(ok, name + ": result differs from the reference table");
    }

    private static Row row(String algorithm, String graphName, Graph g, Measured<?> r, long result) {
        return new Row(algorithm, graphName, g.n(), g.m(), r.medianMs(), r.metrics(), result);
    }

    private static <T> Measured<T> measure(Graph g, Task<T> task) {
        double[] times = new double[RUNS];
        T result = null;
        Metrics first = null;
        Metrics last = null;
        for (int r = 0; r < RUNS; r++) {
            Metrics mt = new Metrics();
            long t0 = System.nanoTime();
            result = task.run(mt);
            times[r] = (System.nanoTime() - t0) / 1e6;
            if (first == null) {
                first = mt;
            }
            last = mt;
        }
        check(first.toString().equals(last.toString()),
                "counters changed between runs: " + first + " vs " + last);
        Arrays.sort(times);
        return new Measured<>(result, last, times[RUNS / 2]);
    }

    private static void writeCsv(Path out, List<Row> rows) throws IOException {
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            w.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            for (Row r : rows) {
                w.println(String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                        r.algorithm(), r.graph(), r.n(), r.m(), r.timeMs(),
                        r.metrics().dsuOps, r.metrics().edgeScans, r.metrics().comparisons,
                        r.metrics().maxDepth, r.result()));
            }
        }
    }

    private static void check(boolean ok, String message) {
        if (!ok) {
            throw new IllegalStateException("CHECK FAILED: " + message);
        }
    }
}
