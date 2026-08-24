package sim.vicsek;

import sim.core.Particle;
import sim.core.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Advances a full particle configuration by one Vicsek time step, given the neighbor lists the
 * Cell Index Method already computed for the current positions:
 *
 *   pos(t+1) = wrap(pos(t) + speed * (cos angle(t), sin angle(t)))
 *   angle(t+1) = baseAngle(t) + deltaAngle,  deltaAngle ~ U[-eta/2, eta/2]
 *
 * baseAngle comes from the injected {@link AngleModel} (neighborhood average for "estandar",
 * a random neighbor's angle for "votante"); position always moves along the *old* angle, per the
 * assignment's formulas. Particle ids are assumed to equal their index in the list, matching how
 * both the trajectory file and CellIndexMethod's neighbor ids are produced/consumed.
 */
public final class VicsekSimulator {

    private final AngleModel angleModel;
    private final double speed;
    private final double eta;
    private final double l;
    private final boolean periodic;
    private final Random rng;

    public VicsekSimulator(AngleModel angleModel, double speed, double eta, double l,
                            boolean periodic, Random rng) {
        this.angleModel = angleModel;
        this.speed = speed;
        this.eta = eta;
        this.l = l;
        this.periodic = periodic;
        this.rng = rng;
    }

    public List<Particle> step(List<Particle> particles, Map<Integer, List<Integer>> neighbors) {
        List<Particle> next = new ArrayList<>(particles.size());
        for (Particle p : particles) {
            List<Integer> neighborIds = neighbors.getOrDefault(p.getId(), List.of());
            double base = angleModel.baseAngle(p, particles, neighborIds, eta, rng);
            double delta = (rng.nextDouble() - 0.5) * eta;
            double newAngle = normalizeAngle(base + delta);

            double oldAngle = p.getAngle();
            double newX = p.getPosition().x + speed * Math.cos(oldAngle);
            double newY = p.getPosition().y + speed * Math.sin(oldAngle);
            if (periodic) {
                newX = wrap(newX, l);
                newY = wrap(newY, l);
            }

            next.add(new Particle(p.getId(), new Vector2D(newX, newY), newAngle, p.getShape(), p.getProperty()));
        }
        return next;
    }

    private static double wrap(double v, double l) {
        double r = v % l;
        return r < 0 ? r + l : r;
    }

    private static double normalizeAngle(double angle) {
        double twoPi = 2 * Math.PI;
        double r = angle % twoPi;
        return r < 0 ? r + twoPi : r;
    }
}
