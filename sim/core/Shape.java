package sim.core;

/**
 * Geometric extent of a particle, decoupled from its position.
 *
 * Everything else in the system (overlap checks, border-to-border distance, the Cell Index
 * Method's grid sizing) only needs a single number from a particle's geometry: the radius of
 * the smallest circle centered on its position that contains it. For a circle this is exact.
 * For a hypothetical future non-circular particle (say a rounded rectangle) it would be the
 * circumradius, and border-to-border distance becomes a conservative approximation instead of
 * an exact value - a reasonable, well-known trade-off for keeping neighbor search shape-agnostic.
 */
public interface Shape {

    /** Radius of the smallest enclosing circle centered on the particle's position. */
    double effectiveRadius();
}
