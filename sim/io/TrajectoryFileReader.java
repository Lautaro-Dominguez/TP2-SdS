package sim.io;

import sim.core.Particle;
import sim.core.PointShape;
import sim.core.Vector2D;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the first block (t=0) of a TP2 trajectory file - what the generator writes and what the
 * simulation stage reads as its initial condition before appending further blocks. N is never
 * stored explicitly anywhere in the file, so blocks are told apart by shape alone: a header line
 * has exactly one token (the time index), a particle line has exactly three (x, y, angulo).
 */
public final class TrajectoryFileReader {

    private TrajectoryFileReader() {}

    public static List<Particle> readFirstBlock(Path path) {
        List<String> lines = readNonEmptyLines(path);
        List<Particle> particles = new ArrayList<>();
        int id = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] tokens = lines.get(i).split("\\s+");
            if (tokens.length < 3) {
                break;
            }
            double x = Double.parseDouble(tokens[0]);
            double y = Double.parseDouble(tokens[1]);
            double angle = Double.parseDouble(tokens[2]);
            particles.add(new Particle(id++, new Vector2D(x, y), angle, PointShape.INSTANCE, 0.0));
        }
        return particles;
    }

    private static List<String> readNonEmptyLines(Path path) {
        try {
            List<String> nonEmpty = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.strip();
                if (!trimmed.isEmpty()) {
                    nonEmpty.add(trimmed);
                }
            }
            return nonEmpty;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
