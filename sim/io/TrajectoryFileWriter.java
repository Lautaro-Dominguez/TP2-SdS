package sim.io;

import sim.core.Particle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;

/**
 * Writes the TP2 trajectory format: one block per time step, a header line with just the time
 * index followed by one "x y angulo" line per particle -
 *
 *   0
 *   x1 y1 angulo1
 *   ...
 *   1
 *   x1 y1 angulo1
 *   ...
 *
 * writeInitial creates the file with block 0 (what the generator produces); appendBlock extends
 * an existing trajectory file with the next block (what the simulation stage does every step),
 * so the same file grows in place instead of the simulation needing its own separate output file.
 */
public final class TrajectoryFileWriter {

    private TrajectoryFileWriter() {}

    public static void writeInitial(Path path, List<Particle> particles) {
        write(path, false, 0, particles);
    }

    public static void appendBlock(Path path, int t, List<Particle> particles) {
        write(path, true, t, particles);
    }

    private static void write(Path path, boolean append, int t, List<Particle> particles) {
        StringBuilder sb = new StringBuilder();
        sb.append(t).append('\n');
        for (Particle p : particles) {
            sb.append(fmt(p.getPosition().x)).append(' ')
              .append(fmt(p.getPosition().y)).append(' ')
              .append(fmt(p.getAngle())).append('\n');
        }
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            if (append) {
                Files.writeString(path, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.writeString(path, sb.toString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.6f", v);
    }
}
