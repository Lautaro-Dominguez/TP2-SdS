package sim.neighbors;

import sim.core.Particle;
import sim.core.Space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static sim.neighbors.NeighborRecording.checkAndRecord;
import static sim.neighbors.NeighborRecording.toSortedResult;

/**
 * Cell Index Method: bins particles into an M x M grid over the space and, for each CELL, only
 * compares it against itself (each same-cell pair once) and half of its Moore neighborhood -
 * the textbook "arriba y las 3 celdas de la derecha" trick (self + N, NE, E, SE) - instead of
 * every other particle. Comparing only that forward half, instead of all 8 neighbors, is what
 * makes every particle pair get checked exactly once instead of twice (once from each side).
 *
 * The "forward half" here isn't the four fixed compass directions: it's whichever of a cell's
 * Moore neighbors has a strictly greater (cx, cy) key. For any interior, large-enough grid that
 * is exactly N/NE/E/SE (see the walkthrough in findNeighbors), but plain compass directions
 * quietly double-count under periodic boundary conditions once M gets small enough (1 or 2) that
 * opposite directions alias to the same cell - e.g. with M=2, "the cell one to the left" IS "the
 * cell one to the right" (wrapping the other way), so a fixed-direction rule would have both of
 * two neighboring cells "discover" each other. Ordering by key sidesteps that: for any two
 * distinct cells, exactly one has the greater key, so exactly one of them ever processes the
 * pair, for any M and either boundary condition.
 *
 * M=1 degenerates to a single cell containing everyone - i.e. brute force - which is exactly what
 * the M-sweep in the assignment's point 3 starts from. BruteForceMethod duplicates that O(n^2)
 * behavior as its own standalone NeighborFinder (the assignment asks for it as a separate
 * implementation, to cross-check this one), sharing the pair-checking/result-building logic below
 * via NeighborRecording instead of going through the cell machinery.
 *
 * This is only correct when the cell size is large enough that two particles closer than rc
 * (border to border) can never end up more than one cell apart: cellSize > rc + 2*rMax (see
 * SimulationConfig#minCellSize for the point-particle-vs-radius derivation). findNeighbors checks
 * this against the actual particles it receives - not just a configured upper bound - and throws
 * rather than silently returning a wrong answer, per the assignment's "si M supera el máximo
 * permitido, debe dar un error".
 */
public final class CellIndexMethod implements NeighborFinder {

    private final int m;

    public CellIndexMethod(int m) {
        if (m < 1) {
            throw new IllegalArgumentException("M must be >= 1");
        }
        this.m = m;
    }

    @Override
    public Map<Integer, List<Integer>> findNeighbors(Space space, List<Particle> particles, double rc) {
        double[] box = space.boundingBox();
        double width = box[1] - box[0];
        double height = box[3] - box[2];
        double cellWidth = width / m;
        double cellHeight = height / m;

        double maxRadius = 0.0;
        for (Particle p : particles) {
            maxRadius = Math.max(maxRadius, p.getShape().effectiveRadius());
        }
        double minCellSize = rc + 2 * maxRadius;
        if (cellWidth <= minCellSize || cellHeight <= minCellSize) {
            int maxM = (int) Math.floor(Math.min(width, height) / minCellSize);
            throw new IllegalArgumentException(String.format(
                "M=%d es demasiado alto: el tamaño de celda resultante (%.4f) no supera "
                + "rc + 2*rMax (%.4f), por lo que se podrían perder vecinos. El M máximo válido "
                + "para estas partículas es %d.",
                m, Math.min(cellWidth, cellHeight), minCellSize, maxM));
        }

        boolean periodic = space.isPeriodic();
        Map<Long, List<Particle>> cells = new HashMap<>();

        for (Particle p : particles) {
            int cx = clamp(index(p.getPosition().x, box[0], cellWidth), m, periodic);
            int cy = clamp(index(p.getPosition().y, box[2], cellHeight), m, periodic);
            cells.computeIfAbsent(key(cx, cy), k -> new ArrayList<>()).add(p);
        }

        Map<Integer, Set<Integer>> neighborSets = new LinkedHashMap<>();
        for (Particle p : particles) {
            neighborSets.put(p.getId(), new LinkedHashSet<>());
        }

        for (Map.Entry<Long, List<Particle>> entry : cells.entrySet()) {
            long homeKey = entry.getKey();
            List<Particle> home = entry.getValue();
            int cx = (int) (homeKey / 1_000_000L);
            int cy = (int) (homeKey % 1_000_000L);

            for (int i = 0; i < home.size(); i++) {
                for (int j = i + 1; j < home.size(); j++) {
                    checkAndRecord(home.get(i), home.get(j), space, rc, neighborSets);
                }
            }

            Set<Long> forwardNeighborKeys = new HashSet<>();
            for (int dx = -1; dx <= 1; dx++) {
                int nx = cx + dx;
                if (periodic) {
                    nx = Math.floorMod(nx, m);
                } else if (nx < 0 || nx >= m) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int ny = cy + dy;
                    if (periodic) {
                        ny = Math.floorMod(ny, m);
                    } else if (ny < 0 || ny >= m) {
                        continue;
                    }
                    long neighborKey = key(nx, ny);
                    if (neighborKey > homeKey) { // the other half is left to that cell's own turn
                        forwardNeighborKeys.add(neighborKey);
                    }
                }
            }

            for (long neighborKey : forwardNeighborKeys) {
                List<Particle> neighborCell = cells.get(neighborKey);
                if (neighborCell == null) continue;
                for (Particle p : home) {
                    for (Particle q : neighborCell) {
                        checkAndRecord(p, q, space, rc, neighborSets);
                    }
                }
            }
        }

        return toSortedResult(particles, neighborSets);
    }

    private static int index(double coord, double origin, double cellSize) {
        return (int) Math.floor((coord - origin) / cellSize);
    }

    private static int clamp(int idx, int m, boolean periodic) {
        return periodic ? Math.floorMod(idx, m) : Math.max(0, Math.min(m - 1, idx));
    }

    private static long key(int cx, int cy) {
        return (long) cx * 1_000_000L + cy;
    }
}
