package sim.vicsek;

import sim.core.Particle;

import java.util.List;
import java.util.Random;

/**
 * Strategy for computing a particle's new heading angle for the next time step, given its
 * current neighborhood (as found by the Cell Index Method at the current time). Both Vicsek
 * variants the assignment asks for - "estandar" (neighborhood average) and "votante" (copy a
 * random neighbor) - only differ in how they arrive at a base angle before the same uniform
 * noise term is added, so that shared step (the delta-angle+wrap) lives in VicsekSimulator
 * instead of being duplicated in both implementations.
 */
public interface AngleModel {

    /**
     * @param self         the particle being updated
     * @param allParticles the full particle list at the current time, indexed by id (particle i
     *                     is at index i, matching how neighbor ids from CellIndexMethod are used)
     * @param neighborIds  ids of self's neighbors within rc, as returned by CellIndexMethod -
     *                     never includes self
     * @param eta          noise amplitude; the caller adds a fresh uniform draw in
     *                     [-eta/2, eta/2] on top of whatever base angle this method returns
     * @param rng          shared RNG for any randomness the model itself needs (e.g. picking a
     *                     random neighbor)
     * @return the base heading angle (in radians, before noise is added)
     */
    double baseAngle(Particle self, List<Particle> allParticles, List<Integer> neighborIds,
                      double eta, Random rng);
}
