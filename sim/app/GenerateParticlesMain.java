package sim.app;

import sim.core.Particle;
import sim.core.PointShape;
import sim.core.Vector2D;
import sim.io.TrajectoryFileWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TP2, punto 1 - genera N particulas puntuales distribuidas uniformemente en un cuadrado
 * L x L, cada una con una posicion (x, y) y un angulo uniforme en [0, 2*pi), y escribe el
 * bloque inicial (t=0) del archivo de trayectoria que sim.app.SimulateMain despues extiende.
 *
 * N se deriva de la densidad pedida: N = round(densidad * L^2).
 *
 * Usage:
 *   java -cp out sim.app.GenerateParticlesMain --L 10 --density 0.8 --seed 42 --out output/trajectory.txt
 */
public final class GenerateParticlesMain {

    public static void main(String[] args) {
        Map<String, String> flags = parseFlags(args);

        double l = Double.parseDouble(flags.getOrDefault("L", "10"));
        String densityStr = flags.get("density");
        if (densityStr == null) {
            throw new IllegalArgumentException("--density es obligatorio (particulas por unidad de area, N = density * L^2)");
        }
        double density = Double.parseDouble(densityStr);
        long seed = Long.parseLong(flags.getOrDefault("seed", String.valueOf(System.nanoTime())));
        Path out = Path.of(flags.getOrDefault("out", "output/trajectory.txt"));

        int n = (int) Math.round(density * l * l);
        if (n <= 0) {
            throw new IllegalArgumentException(String.format(
                "density=%.4f y L=%.4f dan N=%d particulas - subi la densidad o L", density, l, n));
        }

        Random rng = new Random(seed);
        List<Particle> particles = new ArrayList<>(n);
        for (int id = 0; id < n; id++) {
            double x = rng.nextDouble() * l;
            double y = rng.nextDouble() * l;
            double angle = rng.nextDouble() * 2 * Math.PI;
            particles.add(new Particle(id, new Vector2D(x, y), angle, PointShape.INSTANCE, 0.0));
        }

        TrajectoryFileWriter.writeInitial(out, particles);

        System.out.printf(
            "Generadas %d particulas (L=%.2f, density=%.4f, seed=%d) -> %s%n",
            n, l, density, seed, out);
    }

    private static Map<String, String> parseFlags(String[] args) {
        Map<String, String> raw = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) {
            String key = args[i].startsWith("--") ? args[i].substring(2) : args[i];
            raw.put(key, args[i + 1]);
        }
        return raw;
    }
}
