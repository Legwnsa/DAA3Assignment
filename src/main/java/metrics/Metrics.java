package metrics;

public final class Metrics {
    public long dsuOps;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public void reset() {
        dsuOps = 0;
        edgeScans = 0;
        comparisons = 0;
        maxDepth = 0;
    }

    @Override
    public String toString() {
        return "Metrics{dsuOps=" + dsuOps + ", edgeScans=" + edgeScans + ", comparisons=" + comparisons + ", maxDepth=" + maxDepth + "}";
    }
}
