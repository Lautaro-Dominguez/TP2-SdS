# TP2 — Autómata Celular Off-Lattice (Vicsek)

Simula el modelo de Vicsek sobre un cuadrado `L x L` con condición de borde periódica: en cada
paso, cada partícula reorienta su ángulo según sus vecinas dentro de un radio de interacción
`rc` (promedio de ángulos, modelo `estandar`, o copiando a una vecina al azar, modelo `votante`)
y avanza en línea recta. La búsqueda de vecinos reutiliza el Cell Index Method de TP1
(`sim.neighbors.CellIndexMethod`) sin modificarlo.

Todo el código vive en `sim/` (packages `sim.core`, `sim.neighbors`, `sim.vicsek`, `sim.io`,
`sim.app`).

## Compilar

```bash
cd sim
mvn compile
```

## 1. Generar partículas

```bash
mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 0.8 --seed 42 --out output/trajectory.txt"
```

Coloca N partículas puntuales (`N = round(density * L^2)`) en posiciones uniformes dentro de
`[0,L) x [0,L)`, cada una con un ángulo inicial uniforme en `[0, 2π)`.

Flags:
- `--L` (default `10`)
- `--density` (obligatorio — partículas por unidad de área, `N = density * L^2`)
- `--seed` (default: aleatoria)
- `--out` (default `output/trajectory.txt`)

Salida — crea el archivo de trayectoria con el bloque `t=0`:
```
0
x1 y1 angulo1
x2 y2 angulo2
...
```

## 2. Simular

```bash
mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 200 --model estandar --rc 1 \
    --in output/trajectory.txt --out output/trajectory.txt \
    --clustersOut output/clusters.txt --timingOut output/timing.txt"
```

Corre `--iterations` pasos del modelo elegido sobre el archivo generado en el paso 1, extendiendo
ese mismo archivo con un bloque nuevo por paso (`--in`/`--out` pueden apuntar al mismo path para
"ampliarlo" in place, o a paths distintos para no tocar el original).

Flags:
- `--eta` (obligatorio — amplitud del ruido; `deltaAngulo ~ U[-eta/2, eta/2]`)
- `--iterations` (obligatorio — cantidad de pasos a simular, `tn`)
- `--model` (obligatorio — `estandar` o `votante`)
- `--rc` (default `1.0` — radio de interacción, usado tanto para promediar ángulos como para
  definir clusters)
- `--L` (default `10` — debe coincidir con el usado al generar)
- `--periodic` (default `true`)
- `--speed` (default `0.03`)
- `--seed` (default: aleatoria)
- `--in` (default `output/trajectory.txt`)
- `--out` (default: el mismo valor que `--in`)
- `--clustersOut` (default `output/clusters.txt`)
- `--timingOut` (default `output/timing.txt`)

M (grilla del Cell Index Method) se calcula solo a partir de `L` y `rc`, no es un flag.

Salidas:
- **Trayectoria** (`--out`): el archivo de entrada extendido con un bloque por paso, `0` a `tn`
  (mismo formato que el paso 1).
- **Clusters** (`--clustersOut`): `S(0)` a `S(tn)`, una línea por valor — fracción de partículas
  en el cluster conexo más grande (partículas conectadas por cadenas de vecinos a distancia
  `< rc`), calculado con los mismos vecinos que ya calculó el Cell Index Method en ese paso.
  ```
  S(0)
  S(1)
  ...
  S(tn)
  ```
- **Timing** (`--timingOut`): cuánto tardó cada corrida del Cell Index Method, en nanosegundos.
  Se ejecuta una vez por paso registrado (`tn+1` veces en total: una por cada estado `0..tn`,
  incluyendo la última, que no genera un paso nuevo pero sí su `S(tn)`).
  ```
  tn+1
  N
  tiempo registrado 1
  ...
  tiempo registrado tn+1
  ```

## Estructura

```
sim.core       Vector2D, Space/RectangularSpace, Shape/PointShape, Particle (posición + ángulo)
sim.neighbors  NeighborFinder, CellIndexMethod, BruteForceMethod, NeighborRecording (interno) — de TP1, sin cambios
sim.vicsek     AngleModel, StandardAngleModel, VoterAngleModel, VicsekSimulator, ClusterAnalysis
sim.io         TrajectoryFileWriter/Reader, ClusterSizeFileWriter, CimTimingFileWriter
sim.app        GenerateParticlesMain, SimulateMain
```
