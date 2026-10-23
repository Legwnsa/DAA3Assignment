#!/usr/bin/env python3
"""Reads results/results.csv, writes 3 plots to results/plots/ and prints the tables for the report.
Usage: python3 scripts/plots.py        (needs matplotlib, numpy)"""
import csv, math, sys
from collections import defaultdict
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

def load(path):
    with open(path) as f:
        rows = list(csv.DictReader(f))
    for r in rows:
        for k in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
            r[k] = int(r[k])
        r["time_ms"] = float(r["time_ms"])
        r["dens"] = "sparse" if r["graph"].endswith("sparse") else "dense"
    return rows

rows = load("results/results.csv")
algos = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
by = defaultdict(list)
for r in rows:
    by[(r["algorithm"], r["dens"])].append(r)
for k in by:
    by[k].sort(key=lambda r: r["n"])

def series(algo, dens, f):
    rs = by[(algo, dens)]
    return [r["n"] for r in rs], [f(r) for r in rs]

def slope(xs, ys):
    return np.polyfit(np.log10(xs), np.log10(ys), 1)[0]

ops = lambda r: r["dsu_ops"] + r["edge_scans"] + r["comparisons"]
colors = {"Kruskal": "tab:red", "Prim": "tab:blue", "Kosaraju": "tab:green", "Kahn": "tab:orange", "DagShortestPath": "tab:purple"}

# ---- plot 1: operations vs n ----
fig, ax = plt.subplots(figsize=(7.5, 5.5))
for a in algos:
    for d, ls in (("sparse", "-"), ("dense", "--")):
        x, y = series(a, d, ops)
        if a == "Kosaraju":   # coincides with DagShortestPath (2m each): draw it thick and translucent underneath
            ax.plot(x, y, ls, marker="o", ms=9, lw=5, alpha=.35, color=colors[a], label=f"{a} ({d})")
        elif a == "DagShortestPath":
            ax.plot(x, y, ls, marker="x", ms=6, lw=1.5, color=colors[a], label=f"{a} ({d})")
        else:
            ax.plot(x, y, ls, marker="o", ms=4, color=colors[a], label=f"{a} ({d})")
ax.text(0.98, 0.03, "Kosaraju and DagShortestPath coincide (2m operations each)", transform=ax.transAxes,
        ha="right", fontsize=7, style="italic")
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("n (vertices)"); ax.set_ylabel("counted operations (dsuOps + edgeScans + comparisons)")
ax.set_title("Operations vs n (log-log)"); ax.grid(True, which="both", alpha=.3); ax.legend(fontsize=7, ncol=2)
fig.tight_layout(); fig.savefig("results/plots/plot1_operations_vs_n.png", dpi=150); plt.close(fig)

# ---- plot 2: scans per edge ----
fig, ax = plt.subplots(figsize=(7.5, 5.5))
for a in algos:
    for d, ls in (("sparse", "-"), ("dense", "--")):
        x, y = series(a, d, lambda r: r["edge_scans"] / r["m"])
        ax.plot(x, y, ls, marker="o", ms=4, color=colors[a], label=f"{a} ({d})")
ax.set_xscale("log"); ax.set_xlabel("n (vertices)"); ax.set_ylabel("edgeScans / m")
ax.set_ylim(0, 2.4); ax.set_title("Scans per edge: a flat line = linear work")
ax.grid(True, which="both", alpha=.3); ax.legend(fontsize=7, ncol=2)
fig.tight_layout(); fig.savefig("results/plots/plot2_scans_per_edge.png", dpi=150); plt.close(fig)

# ---- plot 3: share of edges Kruskal examines ----
fig, ax = plt.subplots(figsize=(7.5, 5))
for d, c in (("sparse", "tab:red"), ("dense", "tab:blue")):
    x, y = series("Kruskal", d, lambda r: 100 * r["edge_scans"] / r["m"])
    ax.plot(x, y, marker="o", color=c, label=f"Kruskal, {d} (m = {'4n' if d=='sparse' else '20n'})")
    for xi, yi in zip(x, y): ax.annotate(f"{yi:.0f}%", (xi, yi), textcoords="offset points", xytext=(0, 7), ha="center", fontsize=8)
ax.set_xscale("log"); ax.set_ylim(0, 112); ax.set_xlabel("n (vertices)"); ax.set_ylabel("share of edges examined, %")
ax.set_title("Share of edges Kruskal examines before the tree is complete")
ax.grid(True, which="both", alpha=.3); ax.legend()
fig.tight_layout(); fig.savefig("results/plots/plot3_kruskal_edge_share.png", dpi=150); plt.close(fig)

# ---- tables for the report ----
def table(title, header, lines):
    print(f"\n### {title}\n")
    print("| " + " | ".join(header) + " |")
    print("|" + "|".join("---" for _ in header) + "|")
    for l in lines: print("| " + " | ".join(l) + " |")

L = math.log2
sizes = sorted({r["n"] for r in rows})
formulas = [
    ("Kruskal", "dsuOps / E", lambda r: r["dsu_ops"] / r["m"]),
    ("Kruskal", "dsuOps / edgeScans", lambda r: r["dsu_ops"] / r["edge_scans"]),
    ("Kruskal", "comparisons / (E log2 E)", lambda r: r["comparisons"] / (r["m"] * L(r["m"]))),
    ("Prim", "edgeScans / E", lambda r: r["edge_scans"] / r["m"]),
    ("Prim", "comparisons / (E log2 V)", lambda r: r["comparisons"] / (r["m"] * L(r["n"]))),
    ("Kosaraju", "edgeScans / (V+E)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
    ("Kosaraju", "edgeScans / E", lambda r: r["edge_scans"] / r["m"]),
    ("Kahn", "edgeScans / E", lambda r: r["edge_scans"] / r["m"]),
    ("DagShortestPath", "edgeScans / E", lambda r: r["edge_scans"] / r["m"]),
    ("DagShortestPath", "comparisons / E", lambda r: r["comparisons"] / r["m"]),
]
for dens in ("sparse", "dense"):
    lines = []
    for a, name, f in formulas:
        d = dict((r["n"], f(r)) for r in by[(a, dens)])
        lines.append([a, name] + [f"{d[n]:.3f}" for n in sizes])
    table(f"Counter / formula, {dens} graphs", ["algorithm", "ratio"] + [f"n={n}" for n in sizes], lines)

lines = []
for a in algos:
    for d in ("sparse", "dense"):
        x, y = series(a, d, lambda r: r["edge_scans"])
        lines.append([a, d, f"{slope(x, y):.3f}"])
table("Log-log slope of edgeScans vs n", ["algorithm", "density", "slope"], lines)
lines = []
for a in algos:
    for d in ("sparse", "dense"):
        x, y = series(a, d, ops)
        lines.append([a, d, f"{slope(x, y):.3f}"])
table("Log-log slope of total counted operations vs n", ["algorithm", "density", "slope"], lines)

lines = []
for a in ("Kruskal", "Prim"):
    for d in ("sparse", "dense"):
        x, y = series(a, d, lambda r: r["comparisons"])
        lines.append([a, d, f"{slope(x, y):.3f}"])
table("Log-log slope of comparisons vs n", ["algorithm", "density", "slope"], lines)

for d in ("sparse", "dense"):
    x, y = series("Kruskal", d, lambda r: r["edge_scans"] / r["m"])
    table(f"Kruskal edge share, {d}", ["n", "edgeScans", "m", "share"],
          [[str(r["n"]), str(r["edge_scans"]), str(r["m"]), f"{100*r['edge_scans']/r['m']:.1f}%"] for r in by[("Kruskal", d)]])

table("maxDepth of Kosaraju (explicit stack)", ["graph", "n", "maxDepth", "SCCs"],
      [[r["graph"], str(r["n"]), str(r["max_depth"]), str(r["result"])] for r in rows if r["algorithm"] == "Kosaraju"])

table("Median wall-clock time, ms (not the main evidence)", ["graph"] + algos,
      [[g] + [f"{next(r['time_ms'] for r in rows if r['graph']==g and r['algorithm']==a):.1f}" for a in algos]
       for g in dict.fromkeys(r["graph"] for r in rows)])
