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

## 3. Analizar (Python)

Todo vive en `analysis/`. Requiere `numpy` y `matplotlib` (`pip install --user numpy matplotlib`).
Los scripts leen los `.txt` que ya escribe el motor Java — no reimplementan nada de `sim/`. Los que
barren `η`/densidad (`va_vs_eta.py`, `s_vs_eta.py`, `va_vs_s.py`) invocan `GenerateParticlesMain` /
`SimulateMain` por vos (vía `run_java.py`) con **azar fresco en cada realización** (`--seeds` es
la cantidad de repeticiones, no una semilla fija — la reproducibilidad viene de promediar entre
varias corridas, no de fijar el RNG).

| Script | Punto del enunciado | Qué hace | Ejemplo |
|---|---|---|---|
| `animate.py` | a) | Anima una trayectoria ya generada (vector = velocidad, color = ángulo). | `python3 animate.py --traj ../output/trajectory.txt --L 10 --out anim.gif` |
| `va_vs_t.py` | b) | Superpone `va(t)` de varias corridas para elegir a ojo el `t_start` del estacionario. | `python3 va_vs_t.py --traj traj_eta0.5.txt --label "eta=0.5" --out va_vs_t.png` |
| `s_vs_t.py` | d) (evolución) | Mismo criterio que `va_vs_t.py`, pero para `S(t)` (`clusters.txt`). | `python3 s_vs_t.py --clusters clusters_eta0.5.txt --label "eta=0.5" --out s_vs_t.png` |
| `va_vs_eta.py` | c) | `va` vs `η` con error bars, por densidad. | `python3 va_vs_eta.py --densities 2,4,8 --etas 0:5:0.25 --seeds 5 --t-start 150 --iterations 300 --model estandar --out va_vs_eta.png` |
| `s_vs_eta.py` | d) (gráfico 7) | `S` vs `η` con error bars, mismo criterio que `va_vs_eta.py`. | `python3 s_vs_eta.py --densities 2,4,8 --etas 0:5:0.25 --seeds 5 --t-start 150 --iterations 300 --model estandar --out s_vs_eta.png` |
| `va_vs_s.py` | e) (gráfico 8) | `va` vs `S`: un punto por `(densidad, η)`, promediados en el estacionario, distinguiendo densidades. | `python3 va_vs_s.py --densities 2,4,8 --etas 0:5:0.25 --seeds 5 --t-start 150 --iterations 300 --model estandar --out va_vs_s.png` |
| `stationary.py` | — | Helper interno compartido por `s_vs_eta.py`/`va_vs_s.py` (corre cada realización una sola vez para ambos). No se ejecuta directo. |  |

Para el modelo de votante, agregar `--model votante` a cualquiera de los comandos de barrido.

**Densidades bajas para el estudio de clusters**: además de `ρ = 2, 4, 8`, la cátedra sumó
`ρ = 1/π ≈ 0.3183`, `1/(2π) ≈ 0.1592`, `1/(3π) ≈ 0.1061` (por debajo del umbral de percolación
continua) para `s_vs_eta.py`/`va_vs_s.py`. A esa escala conviene `--iterations 1000 --t-start 600`
en vez de `300`/`150` — con tan pocas partículas el "band forming" del modelo de Vicsek tarda más
en asentarse.

## 4. Scripts de barrido (`sim/`)

Automatizan los pasos 1-3 para el barrido `densidad ∈ {2, 4, 8} × η ∈ {0.5, 2, 8} × modelo ∈
{estandar, votante}` (18 combinaciones), `L=10`, `rc=1`, `iterations=500`. Se corren desde
`sim/` (hacen `cd` a su propio directorio, así que también andan invocados con path absoluto).

| Script | Qué hace | Uso |
|---|---|---|
| `simexec.sh` | Pasos 1+2: para cada una de las 18 combinaciones, genera partículas y simula, escribiendo `output/trajectory{N}.txt`, `clusters{N}.txt`, `timing{N}.txt`. | `./simexec.sh` |
| `analyzeexec.sh` | Lee esos 18 archivos (no simula nada nuevo) y corre `va_vs_t.py`/`s_vs_t.py`: una imagen por `(densidad, modelo)` — 6 combinaciones — superponiendo las 3 curvas de `η`. También corre `animate.py` por corrida si `RUN_ANIMATIONS=true` (apagado por default, son 18 GIFs pesados). | `./analyzeexec.sh` |
| `analyzeexec_eta.sh` | Corre `va_vs_eta.py`/`s_vs_eta.py`/`va_vs_s.py`: simulan desde cero (no leen los archivos de `simexec.sh`) barriendo `η` con `--etas 0:8:0.5` (editable como `ETA_SWEEP` en el script), una imagen por modelo superponiendo las 3 densidades. Necesita el `t_start` del estado estacionario, elegido a ojo mirando los `va_vs_t_*.png`/`s_vs_t_*.png` que generó `analyzeexec.sh` — por eso se corre aparte y recibe ese valor como argumento. | `./analyzeexec_eta.sh <t_start>` (ej. `./analyzeexec_eta.sh 250`) |

**Numeración**: `simexec.sh` y `analyzeexec.sh` (para las animaciones) numeran los archivos con
`N = densidad_idx*6 + eta_idx*2 + modelo_idx + 1` (índices 0-based, en ese orden de anidado:
densidad afuera, η en el medio, modelo adentro — mismo orden que barren los `for` de ambos
scripts). `analyzeexec.sh` (para `va_vs_t`/`s_vs_t`) y `analyzeexec_eta.sh` no numeran sus
salidas porque cada imagen ya agrupa varias corridas (varios `N`): las nombran por
`densidad`/`modelo` en su lugar (`va_vs_t_d2_estandar.png`, `va_vs_eta_votante.png`, etc).

Orden de uso: `simexec.sh` → `analyzeexec.sh` → mirar los `va_vs_t_*.png`/`s_vs_t_*.png` para
elegir `t_start` → `analyzeexec_eta.sh <t_start>`.

## Estructura

```
sim.core       Vector2D, Space/RectangularSpace, Shape/PointShape, Particle (posición + ángulo)
sim.neighbors  NeighborFinder, CellIndexMethod, BruteForceMethod, NeighborRecording (interno) — de TP1, sin cambios
sim.vicsek     AngleModel, StandardAngleModel, VoterAngleModel, VicsekSimulator, ClusterAnalysis
sim.io         TrajectoryFileWriter/Reader, ClusterSizeFileWriter, CimTimingFileWriter
sim.app        GenerateParticlesMain, SimulateMain
analysis       Scripts de Python para animar y graficar (ver sección 3) — vicsek_io.py y run_java.py
               son utilidades compartidas, no se ejecutan directo
sim/simexec.sh          Barrido de generación+simulación (ver sección 4)
sim/analyzeexec.sh      Barrido de va_vs_t/s_vs_t (+ animaciones opcionales) (ver sección 4)
sim/analyzeexec_eta.sh  Barrido de va_vs_eta/s_vs_eta/va_vs_s (ver sección 4)
```
