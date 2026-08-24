package sim.neighbors;

import sim.core.Particle;
import sim.core.Space;

import java.util.List;
import java.util.Map;

/**
 * Finds, for every particle, the ids of the other particles whose border-to-border distance is
 * less than rc. Symmetric by construction: if q ends up in p's list, p ends up in q's list.
 */
public interface NeighborFinder {

    Map<Integer, List<Integer>> findNeighbors(Space space, List<Particle> particles, double rc);
}
