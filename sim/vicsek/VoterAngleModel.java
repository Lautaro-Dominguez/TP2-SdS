package sim.vicsek;

import sim.core.Particle;

import java.util.List;
import java.util.Random;

/**
 * "Modelo votante": instead of averaging the neighborhood, the base angle is simply the current
 * angle of one neighbor chosen uniformly at random. Falls back to the particle's own angle when
 * it has no neighbors within rc - there is nobody to copy from.
 */
public final class VoterAngleModel implements AngleModel {

    @Override
    public double baseAngle(Particle self, List<Particle> allParticles, List<Integer> neighborIds,
                             double eta, Random rng) {
        if (neighborIds.isEmpty()) {
            return self.getAngle();
        }
        int chosenId = neighborIds.get(rng.nextInt(neighborIds.size()));
        return allParticles.get(chosenId).getAngle();
    }
}
