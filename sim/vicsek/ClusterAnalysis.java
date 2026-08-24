package sim.vicsek;

import java.util.List;
import java.util.Map;

/**
 * Largest-cluster fraction S: a cluster is a maximal set of particles connected by a chain of
 * neighbor-to-neighbor hops within rc. Reuses the exact neighbor lists the Cell Index Method
 * already computed for the current time step (same call that drives the Vicsek angle update)
 * instead of re-deriving adjacency from scratch.
 */
public final class ClusterAnalysis {

    private ClusterAnalysis() {}

    /** Fraction of the N particles that belong to the largest cluster. */
    public static double largestClusterFraction(Map<Integer, List<Integer>> neighbors, int n) {
        UnionFind uf = new UnionFind(n);
        for (Map.Entry<Integer, List<Integer>> entry : neighbors.entrySet()) {
            int p = entry.getKey();
            for (int q : entry.getValue()) {
                uf.union(p, q);
            }
        }

        int[] clusterSizes = new int[n];
        for (int i = 0; i < n; i++) {
            clusterSizes[uf.find(i)]++;
        }
        int largest = 0;
        for (int size : clusterSizes) {
            largest = Math.max(largest, size);
        }
        return (double) largest / n;
    }

    private static final class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]];
                x = parent[x];
            }
            return x;
        }

        void union(int a, int b) {
            int ra = find(a);
            int rb = find(b);
            if (ra == rb) return;
            if (rank[ra] < rank[rb]) {
                parent[ra] = rb;
            } else if (rank[ra] > rank[rb]) {
                parent[rb] = ra;
            } else {
                parent[rb] = ra;
                rank[ra]++;
            }
        }
    }
}
