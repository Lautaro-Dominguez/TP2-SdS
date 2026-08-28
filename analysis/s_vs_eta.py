"""Punto d) segunda mitad / grafico 7 - S vs eta con barras de error, para varias densidades.

Calco de va_vs_eta.py pero para el observable de clusters: "Graficar el valor medio de S en
el estacionario con su desvio en funcion de eta para las densidades consideradas, siguiendo
un procedimiento equivalente al realizado en (c) para la polarizacion" (enunciado, punto d).

Para cada (densidad, eta) corre --seeds realizaciones independientes (sin semilla fija, via
sim.app.GenerateParticlesMain / SimulateMain sin tocarlos). Para cada realizacion promedia
S(t) desde --t-start (el inicio del estacionario de S, determinado a ojo con s_vs_t.py - no
necesariamente el mismo t que el de va, son observables distintos) hasta el final, y despues
promedia esos escalares entre realizaciones: el desvio estandar entre ellas es la barra de
error.

Uso:
    python3 s_vs_eta.py --t-start 150 --out s_vs_eta.png
    python3 s_vs_eta.py --densities 2,4,8 --etas 0:5:0.25 --seeds 5 --t-start 150 \
                        --iterations 300 --model estandar --out s_vs_eta_estandar.png
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

sys.path.insert(0, str(Path(__file__).resolve().parent))
from stationary import parse_range, run_realization, stationary_mean, new_workdir, cleanup_workdir


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--densities", default="2,4,8")
    parser.add_argument("--etas", default="0:5:0.25", help="start:stop:step")
    parser.add_argument("--seeds", type=int, default=5,
                         help="cantidad de realizaciones independientes (sin semilla fija) por (rho, eta)")
    parser.add_argument("--t-start", type=int, required=True,
                         help="t desde donde se promedia S (ver criterio de s_vs_t.py)")
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

    workdir, own_tmp = new_workdir(args.workdir, "s_vs_eta_")

    fig, ax = plt.subplots(figsize=(7, 5))
    try:
        for density in densities:
            means, stds = [], []
            for eta in etas:
                per_run = []
                for run_idx in range(args.seeds):
                    _, s = run_realization(density, args.L, eta, args.model, args.rc,
                                            args.speed, args.iterations, run_idx, workdir)
                    per_run.append(stationary_mean(s, args.t_start, label="S"))
                per_run = np.array(per_run)
                means.append(per_run.mean())
                stds.append(per_run.std())
                print(f"rho={density} eta={eta:.3f} -> "
                      f"S={per_run.mean():.4f} +- {per_run.std():.4f}")
            ax.errorbar(etas, means, yerr=stds, marker="o", capsize=3,
                        label=f"rho={density:g}")
    finally:
        cleanup_workdir(workdir, own_tmp, args.keep_workdir)

    ax.set_xlabel("eta")
    ax.set_ylabel("<S>")
    ax.set_ylim(0, 1.02)
    ax.set_title(f"Fraccion del cluster mas grande vs ruido (modelo {args.model})")
    ax.legend()
    fig.tight_layout()
    fig.savefig(args.out, dpi=150)
    print(f"Grafico guardado en {args.out}")


if __name__ == "__main__":
    main()
