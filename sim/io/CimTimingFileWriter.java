package sim.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Writes how long each Cell Index Method call took during a simulation run, in nanoseconds
 * (System.nanoTime() resolution - millisecond precision truncates to 0 for the particle counts
 * this simulation typically uses):
 *
 *   cantidadDeCorridas
 *   N
 *   tiempo registrado 1
 *   ...
 *   tiempo registrado cantidadDeCorridas
 *
 * The header count is the actual number of CIM invocations performed (one per recorded time
 * step, including the final one) - for an --iterations tn run that's tn+1, not tn, since CIM
 * also runs once against the last state to report its S(tn) with no further transition after it.
 */
public final class CimTimingFileWriter {

    private CimTimingFileWriter() {}

    public static void write(Path path, int n, List<Long> timesNs) {
        StringBuilder sb = new StringBuilder();
        sb.append(timesNs.size()).append('\n');
        sb.append(n).append('\n');
        for (long t : timesNs) {
            sb.append(String.format(Locale.US, "%d", t)).append('\n');
        }
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, sb.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
