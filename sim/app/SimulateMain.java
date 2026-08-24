package sim.app;

import sim.core.Particle;
import sim.core.RectangularSpace;
import sim.core.Space;
import sim.io.CimTimingFileWriter;
import sim.io.ClusterSizeFileWriter;
import sim.io.TrajectoryFileReader;
import sim.io.TrajectoryFileWriter;
import sim.neighbors.CellIndexMethod;
import sim.neighbors.NeighborFinder;
import sim.vicsek.AngleModel;
import sim.vicsek.ClusterAnalysis;
import sim.vicsek.StandardAngleModel;
import sim.vicsek.VicsekSimulator;
import sim.vicsek.VoterAngleModel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TP2, punto 2 - corre el automata celular off-lattice de Vicsek sobre el estado inicial que
 * sim.app.GenerateParticlesMain escribio: en cada paso reusa una unica corrida del Cell Index
 * Method (sin modificarlo) para (a) actualizar posicion/angulo de cada particula, (b) calcular
 * el tamano de cluster mas grande S(t), y (c) medir cuanto tardo esa corrida de CIM.
 *
 * El loop corre para t = 0..iterations (ambos inclusive): eso da iterations+1 corridas de CIM,
 * iterations+1 valores de S y un archivo de trayectoria con bloques 0..iterations - la corrida
 * en t=iterations solo aporta S(iterations)/timing, no genera un bloque nuevo.
 *
 * Usage:
 *   java -cp out sim.app.SimulateMain --eta 0.5 --iterations 200 --model estandar --rc 1 \
 *       --in output/trajectory.txt --out output/trajectory.txt \
 *       --clustersOut output/clusters.txt --timingOut output/timing.txt
 */
public final class SimulateMain {

    public static void main(String[] args) {
        Map<String, String> flags = parseFlags(args);

        String etaStr = flags.get("eta");
        String iterationsStr = flags.get("iterations");
        String modelStr = flags.get("model");
        if (etaStr == null || iterationsStr == null || modelStr == null) {
            throw new IllegalArgumentException("--eta, --iterations y --model son obligatorios");
        }
        double eta = Double.parseDouble(etaStr);
        int iterations = Integer.parseInt(iterationsStr);
        double rc = Double.parseDouble(flags.getOrDefault("rc", "1.0"));
        double l = Double.parseDouble(flags.getOrDefault("L", "10"));
        boolean periodic = Boolean.parseBoolean(flags.getOrDefault("periodic", "true"));
        double speed = Double.parseDouble(flags.getOrDefault("speed", "0.03"));
        long seed = Long.parseLong(flags.getOrDefault("seed", String.valueOf(System.nanoTime())));
        Path in = Path.of(flags.getOrDefault("in", "output/trajectory.txt"));
        Path out = Path.of(flags.getOrDefault("out", flags.getOrDefault("in", "output/trajectory.txt")));
        Path clustersOut = Path.of(flags.getOrDefault("clustersOut", "output/clusters.txt"));
        Path timingOut = Path.of(flags.getOrDefault("timingOut", "output/timing.txt"));

        AngleModel angleModel = angleModelFor(modelStr);

        List<Particle> particles = TrajectoryFileReader.readFirstBlock(in);
        int n = particles.size();
        if (!out.equals(in)) {
            TrajectoryFileWriter.writeInitial(out, particles);
        }

        Space space = RectangularSpace.square(l, periodic);
        int m = computeM(l, rc);
        NeighborFinder finder = new CellIndexMethod(m);
        Random rng = new Random(seed);
        VicsekSimulator simulator = new VicsekSimulator(angleModel, speed, eta, l, periodic, rng);

        List<Double> sValues = new ArrayList<>(iterations + 1);
        List<Long> timesNs = new ArrayList<>(iterations + 1);

        for (int t = 0; t <= iterations; t++) {
            long start = System.nanoTime();
            Map<Integer, List<Integer>> neighbors = finder.findNeighbors(space, particles, rc);
            long elapsedNs = System.nanoTime() - start;
            timesNs.add(elapsedNs);

            sValues.add(ClusterAnalysis.largestClusterFraction(neighbors, n));

            if (t < iterations) {
                particles = simulator.step(particles, neighbors);
                TrajectoryFileWriter.appendBlock(out, t + 1, particles);
            }
        }

        ClusterSizeFileWriter.write(clustersOut, sValues);
        CimTimingFileWriter.write(timingOut, n, timesNs);

        System.out.printf(
            "Simulados %d pasos (N=%d, model=%s, eta=%.4f, rc=%.2f, M=%d, periodic=%b) -> %s, %s, %s%n",
            iterations, n, modelStr, eta, rc, m, periodic, out, clustersOut, timingOut);
    }

    private static AngleModel angleModelFor(String model) {
        return switch (model) {
            case "estandar" -> new StandardAngleModel();
            case "votante" -> new VoterAngleModel();
            default -> throw new IllegalArgumentException(
                "--model debe ser 'estandar' o 'votante', se recibio: " + model);
        };
    }

    /** Mayor M tal que L/M > rc (particulas puntuales, sin radio propio). */
    private static int computeM(double l, double rc) {
        int m = (int) Math.floor(l / rc);
        while (m > 1 && l / m <= rc) {
            m--;
        }
        return Math.max(m, 1);
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
