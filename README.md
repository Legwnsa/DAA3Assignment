DAA Assignment 3 - Graphs: DSU, MST, SCC and Shortest Paths

Temirlan Zhangirkhan, group SE-2521.
GitHub: (branch `main`, tag `v1.0`)

Own implementations of DSU (union by rank + path compression), Kruskal, Prim, Kosaraju (iterative DFS), Kahn and
shortest paths in a DAG on one adjacency-list `Graph` class, with operation counters, a self-checking benchmark and JUnit 5 tests.

Requirements

JDK 17 or newer, Maven 3.8+. Only for the plots: Python 3 with `numpy` and `matplotlib`.

Build, test, run

mvn test                         # compile + all JUnit 5 tests
mvn compile exec:exec            # benchmark: writes results/results.csv (java -Xmx4g, about 20-30 s)
python3 scripts/plots.py         # three PNGs in results/plots/ + the ratios and slopes used in REPORT.md


Without Maven's exec plugin: `mvn -q compile && java -Xmx4g -cp target/classes benchmark.Benchmark`.

The benchmark prints the reference table next to the measured values and stops with an exception (and writes no CSV)
if Kruskal and Prim disagree, a topological order is invalid, an exact scan count (Kosaraju 2m, Kahn m, DAG m) is
wrong, counters change between runs, or a result differs from the reference values. The same seed gives the same
counters on every machine.

Layout

src/main/java/graph        Graph (adjacency list, directed and undirected), Edge, GraphIO
src/main/java/algorithms   DSU, Kruskal, Prim, MstResult, Kosaraju, Kahn, DagShortestPath
src/main/java/metrics      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/benchmark    GraphGenerator (Random(42)), ReferenceValues, Benchmark
src/test/java              DSU, MST, SCC, topological order, DAG paths, stack safety, GraphIO, reference values
data/                      city_connected, city_disconnected, jobs_cyclic, jobs_dag
results/results.csv        50 rows; results/plots/ three PNG charts
scripts/plots.py           plots + numbers for the report
REPORT.md                  complexity table, counters vs formulas, proofs, "which algorithm when", slopes, plots, time


Input format: first line `n m directed|undirected`, then `m` lines `u v w` (lines starting with `#` are comments).

What the counters mean

- `dsuOps` = DSU `find` calls + successful `union` calls (the root look-ups inside `union` belong to that union).
- `edgeScans` = edges looked at. Kruskal: edges taken from the sorted list (it stops after n-1 tree edges).
  Prim, Kosaraju: adjacency entries (2m). Kahn and the DAG relaxation: m. In-degrees are maintained by `Graph` while edges are added, so they are not an edge scan.
- `comparisons` = weights compared: merge sort (Kruskal), PriorityQueue comparator calls (Prim), distance comparisons (DAG relaxation).
- `maxDepth` = deepest the explicit DFS stack got (Kosaraju); 0 for algorithms without a stack.
- `DagShortestPath` rows cover the relaxation pass only; its topological order is the one produced by the `Kahn` row.
- CSV `result`: MST weight (Kruskal, Prim), number of SCCs (Kosaraju), vertices in the order (Kahn), sum of distances (DagShortestPath).

Git workflow

`main` holds only working code (tag `v1.0`). Work was done on `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths`,
`feature/metrics` and merged with `--no-ff`. Commit messages follow `feat(...)`, `test(...)`, `docs(...)`.

AI use acknowledgement

Used AI to fix bugs and understand them