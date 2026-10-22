package algorithms;

import graph.Edge;
import java.util.List;

public record MstResult(long totalWeight, int components, List<Edge> edges) {
    public int edgeCount() {
        return edges.size();
    }
}
