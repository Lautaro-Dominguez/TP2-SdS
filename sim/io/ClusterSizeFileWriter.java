package sim.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Writes S(0), S(1), ..., S(tn) - one largest-cluster-fraction value per line. */
public final class ClusterSizeFileWriter {

    private ClusterSizeFileWriter() {}

    public static void write(Path path, List<Double> sValues) {
        StringBuilder sb = new StringBuilder();
        for (double s : sValues) {
            sb.append(String.format(Locale.US, "%.6f", s)).append('\n');
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
