package sim.core;

import java.util.Random;

/**
 * Abstraction of the region where particles live.
 *
 * A Space is responsible for:
 *  - Sampling random points inside itself (optionally leaving a margin near the border,
 *    so a particle with radius r doesn't stick out of a hard wall).
 *  - Computing the distance between two points "as seen by the simulation" - this is where
 *    periodic boundary conditions live. Euclidean distance for open borders, minimum-image
 *    distance for periodic ones.
 *  - Exposing whatever geometric info downstream algorithms need (bounding box, measure)
 *    without exposing how that's represented internally.
 *
 * Concentrating the open-vs-periodic decision inside the Space implementation - instead of an
 * "if periodic" scattered through the generator, the neighbor-search algorithm and the plotting
 * script - means every stage of the pipeline can share one single, tested notion of "distance"
 * and "is this point inside the space". It's also the extension point for future TPs: a circular
 * arena, a 3D box, etc. only need a new Space implementation, nothing else changes.
 */
public interface Space {

    /** "Size" of the space: area for 2D, length for 1D, volume for 3D, etc. */
    double measure();

    /** Smallest axis-aligned box containing the space: {xMin, xMax, yMin, yMax}. Used to size grids. */
    double[] boundingBox();

    /** Whether opposite borders are identified with each other (torus topology). */
    boolean isPeriodic();

    /** True if the point lies inside the space. */
    boolean contains(Vector2D point);

    /**
     * Draws a uniformly random point inside the space, staying at least {@code margin} away
     * from every wall (pass the particle's radius as margin). For periodic spaces margin is
     * ignored, since there is no wall to stay away from.
     */
    Vector2D randomPoint(Random rng, double margin);

    /** Distance between two points as seen by the space (accounts for periodicity). */
    double distance(Vector2D a, Vector2D b);
}
