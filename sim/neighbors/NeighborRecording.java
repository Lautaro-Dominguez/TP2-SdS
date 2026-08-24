package sim.neighbors;

import sim.core.Particle;
import sim.core.Space;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared border-to-border distance check and result-shaping logic for {@link NeighborFinder}
 * implementations, so CellIndexMethod and BruteForceMethod can't drift apart on what "neighbor"
 * means or how the final map gets built.
 */
final class NeighborRecording {

    private NeighborRecording() {}

    static void checkAndRecord(Particle p, Particle q, Space space, double rc,
                                Map<Integer, Set<Integer>> neighborSets) {
        if (p.borderDistanceTo(q, space) < rc) {
            neighborSets.get(p.getId()).add(q.getId());
            neighborSets.get(q.getId()).add(p.getId());
        }
    }

    static Map<Integer, List<Integer>> toSortedResult(List<Particle> particles,
                                                        Map<Integer, Set<Integer>> neighborSets) {
        Map<Integer, List<Integer>> result = new LinkedHashMap<>();
        for (Particle p : particles) {
            List<Integer> ids = new ArrayList<>(neighborSets.get(p.getId()));
            ids.sort(null);
            result.put(p.getId(), ids);
        }
        return result;
    }
}
