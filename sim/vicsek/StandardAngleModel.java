package sim.vicsek;

import sim.core.Particle;

import java.util.List;
import java.util.Random;

/**
 * "Modelo estandar": the base angle is the direction of the average heading of every particle
 * within rc, self included -
 * atan2(promedio del seno de los angulos, promedio del coseno de los angulos).
 */
public final class StandardAngleModel implements AngleModel {

    @Override
    public double baseAngle(Particle self, List<Particle> allParticles, List<Integer> neighborIds,
                             double eta, Random rng) {
        double sinSum = Math.sin(self.getAngle());
        double cosSum = Math.cos(self.getAngle());
        for (int neighborId : neighborIds) {
            double angle = allParticles.get(neighborId).getAngle();
            sinSum += Math.sin(angle);
            cosSum += Math.cos(angle);
        }
        int count = neighborIds.size() + 1;
        double meanSin = sinSum / count;
        double meanCos = cosSum / count;
        return Math.atan2(meanSin, meanCos);
    }
}
