"""Punto e) / grafico 8 - va en funcion de S, distinguiendo densidades.

"Grafique el valor de la polarizacion va en funcion de la fraccion de particulas en la
componente gigante S, distinguiendo las distintas densidades" (enunciado, punto e).

Interpretacion (confirmada): cada punto del grafico es UNA realizacion promediada en el
estado estacionario, no un instante suelto. Para cada (densidad, eta) se corren --seeds
realizaciones independientes (sin semilla fija); de cada una se promedian va(t) y S(t) por
separado desde --t-start hasta el final (mismo criterio y mismo t_start que s_vs_eta.py /
va_vs_eta.py), y despues se promedia entre realizaciones (el desvio estandar entre ellas da
las barras de error en ambos ejes). Barriendo eta se obtiene una curva (S, va) por densidad:
eta queda implicito como el parametro que recorre cada curva, exactamente igual que en los
graficos vecinos (c y 7), asi que reutiliza las mismas corridas que ellos en vez de agregar
un tipo de corrida nuevo.

Uso:
    python3 va_vs_s.py --t-start 150 --out va_vs_s.png
    python3 va_vs_s.py --densities 2,4,8 --etas 0:5:0.25 --seeds 5 --t-start 150 \
                       --iterations 300 --model estandar --out va_vs_s_estandar.png
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
    parser.add_argument("--etas", default="0:5:0.25", help="start:stop:step, barrido usado para trazar cada curva")
    parser.add_argument("--seeds", type=int, default=5,
                         help="cantidad de realizaciones independientes (sin semilla fija) por (rho, eta)")
    parser.add_argument("--t-start", type=int, required=True,
                         help="t desde donde se promedian va y S (mismo criterio que va_vs_eta.py / s_vs_eta.py)")
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

    workdir, own_tmp = new_workdir(args.workdir, "va_vs_s_")

    fig, ax = plt.subplots(figsize=(7, 5))
    try:
        for density in densities:
            s_means, s_stds, va_means, va_stds = [], [], [], []
            for eta in etas:
                va_runs, s_runs = [], []
                for run_idx in range(args.seeds):
                    va, s = run_realization(density, args.L, eta, args.model, args.rc,
                                             args.speed, args.iterations, run_idx, workdir)
                    va_runs.append(stationary_mean(va, args.t_start, label="va"))
                    s_runs.append(stationary_mean(s, args.t_start, label="S"))
                va_runs = np.array(va_runs)
                s_runs = np.array(s_runs)
                s_means.append(s_runs.mean())
                s_stds.append(s_runs.std())
                va_means.append(va_runs.mean())
                va_stds.append(va_runs.std())
                print(f"rho={density} eta={eta:.3f} -> "
                      f"S={s_runs.mean():.4f}+-{s_runs.std():.4f} "
                      f"va={va_runs.mean():.4f}+-{va_runs.std():.4f}")
            order = np.argsort(s_means)
            s_means_o = np.array(s_means)[order]
            va_means_o = np.array(va_means)[order]
            ax.errorbar(s_means_o, va_means_o,
                        xerr=np.array(s_stds)[order], yerr=np.array(va_stds)[order],
                        marker="o", capsize=3, label=f"rho={density:g}")
    finally:
        cleanup_workdir(workdir, own_tmp, args.keep_workdir)

    ax.set_xlabel("<S>")
    ax.set_ylabel("<va>")
    ax.set_xlim(0, 1.02)
    ax.set_ylim(0, 1.02)
    ax.set_title(f"Polarizacion vs fraccion del cluster mas grande (modelo {args.model})")
    ax.legend()
    fig.tight_layout()
    fig.savefig(args.out, dpi=150)
    print(f"Grafico guardado en {args.out}")


if __name__ == "__main__":
    main()
