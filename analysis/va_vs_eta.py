"""Punto c) Curva Input vs Observable (va vs eta), con barras de error, para varias
densidades.

Para cada (densidad, eta) corre --seeds realizaciones independientes (genera particulas +
simula desde cero para cada una, via sim.app.GenerateParticlesMain / SimulateMain sin
tocarlos, sin fijar semilla - cada corrida usa la aleatoriedad por defecto de la JVM, asi
que ninguna corrida se puede reproducir igual a otra a proposito). Para cada realizacion
promedia va(t) desde --t-start (el inicio del estacionario, determinado a ojo con
va_vs_t.py) hasta el final de la corrida, y despues promedia esos escalares entre
realizaciones: el desvio estandar entre ellas es la barra de error. Esto combina
promediado temporal en el estacionario y promediado entre realizaciones independientes,
que es el criterio mas preciso de los tres que se evaluaron.

Uso:
    python3 va_vs_eta.py --t-start 150 --out va_vs_eta.png
    python3 va_vs_eta.py --densities 2,4,8 --etas 0:5:0.25 --seeds 5 --t-start 150 \
                         --iterations 300 --model estandar --out va_vs_eta_estandar.png
"""
from __future__ import annotations

import argparse
import shutil
import sys
import tempfile
from pathlib import Path

import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

sys.path.insert(0, str(Path(__file__).resolve().parent))
from vicsek_io import read_trajectory, polarization_series
from run_java import generate_particles, simulate


def parse_range(spec):
    """'0:5:0.25' -> np.arange(0, 5+0.25, 0.25) (start:stop:step, stop inclusive)."""
    start, stop, step = (float(x) for x in spec.split(":"))
    return np.round(np.arange(start, stop + step / 2, step), 6)


def run_one(density, l, eta, model, rc, speed, iterations, run_idx, workdir):
    """Una realizacion sin semilla fija - posiciones/angulos iniciales y ruido son
    completamente aleatorios, no reproducibles a proposito de una corrida a otra."""
    tag = f"rho{density}_eta{eta:.4f}_run{run_idx}"
    traj = workdir / f"traj_{tag}.txt"
    clusters = workdir / f"clusters_{tag}.txt"
    timing = workdir / f"timing_{tag}.txt"
    generate_particles(l, density, traj)
    simulate(eta, iterations, model, rc, l, True, speed, traj, traj, clusters, timing)
    _, _, angles = read_trajectory(traj)
    va = polarization_series(angles)
    traj.unlink(missing_ok=True)
    clusters.unlink(missing_ok=True)
    timing.unlink(missing_ok=True)
    return va


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--densities", default="2,4,8")
    parser.add_argument("--etas", default="0:5:0.25", help="start:stop:step")
    parser.add_argument("--seeds", type=int, default=5,
                         help="cantidad de realizaciones independientes (sin semilla fija) por (rho, eta)")
    parser.add_argument("--t-start", type=int, required=True,
                         help="t desde donde se promedia (ver criterio de va_vs_t.py)")
    parser.add_argument("--iterations", type=int, default=300)
    parser.add_argument("--model", default="estandar", choices=["estandar", "votante"])
    parser.add_argument("--rc", type=float, default=1.0)
    parser.add_argument("--L", type=float, default=10.0)
    parser.add_argument("--speed", type=float, default=0.03)
    parser.add_argument("--out", required=True)
    parser.add_argument("--workdir", default=None,
                         help="directorio para los .txt temporales (default: uno nuevo en /tmp)")
    parser.add_argument("--keep-workdir", action="store_true",
                         help="no borrar el workdir temporal al terminar")
    args = parser.parse_args()

    densities = [float(x) for x in args.densities.split(",")]
    etas = parse_range(args.etas)

    own_tmp = args.workdir is None
    workdir = Path(args.workdir) if args.workdir else Path(tempfile.mkdtemp(prefix="va_vs_eta_"))
    workdir.mkdir(parents=True, exist_ok=True)

    fig, ax = plt.subplots(figsize=(7, 5))
    try:
        for density in densities:
            means, stds = [], []
            for eta in etas:
                per_run = []
                for run_idx in range(args.seeds):
                    va = run_one(density, args.L, eta, args.model, args.rc, args.speed,
                                 args.iterations, run_idx, workdir)
                    if args.t_start >= len(va):
                        raise SystemExit(
                            f"--t-start {args.t_start} >= pasos simulados ({len(va)}); "
                            "subi --iterations")
                    per_run.append(va[args.t_start:].mean())
                per_run = np.array(per_run)
                means.append(per_run.mean())
                stds.append(per_run.std())
                print(f"rho={density} eta={eta:.3f} -> "
                      f"va={per_run.mean():.4f} +- {per_run.std():.4f}")
            ax.errorbar(etas, means, yerr=stds, marker="o", capsize=3,
                        label=f"rho={density:g}")
    finally:
        if own_tmp and not args.keep_workdir:
            shutil.rmtree(workdir, ignore_errors=True)

    ax.set_xlabel("eta")
    ax.set_ylabel("<va>")
    ax.set_ylim(0, 1.02)
    ax.set_title(f"Polarizacion vs ruido (modelo {args.model})")
    ax.legend()
    fig.tight_layout()
    fig.savefig(args.out, dpi=150)
    print(f"Grafico guardado en {args.out}")


if __name__ == "__main__":
    main()
