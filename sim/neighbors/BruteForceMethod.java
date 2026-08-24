package sim.neighbors;

import sim.core.Particle;
import sim.core.Space;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static sim.neighbors.NeighborRecording.checkAndRecord;
import static sim.neighbors.NeighborRecording.toSortedResult;

/**
 * Standalone O(n^2) neighbor search: checks every pair of particles once, independent of
 * CellIndexMethod's cell machinery. The assignment asks for this as a separate implementation
 * (not just CellIndexMethod at M=1) so its timings can be cross-checked and plotted against the
 * Cell Index Method's on the same N-vs-time chart.
 */
public final class BruteForceMethod implements NeighborFinder {

    @Override
    public Map<Integer, List<Integer>> findNeighbors(Space space, List<Particle> particles, double rc) {
        Map<Integer, Set<Integer>> neighborSets = new LinkedHashMap<>();
        for (Particle p : particles) {
            neighborSets.put(p.getId(), new LinkedHashSet<>());
        }

        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                checkAndRecord(particles.get(i), particles.get(j), space, rc, neighborSets);
            }
        }

        return toSortedResult(particles, neighborSets);
    }
}
