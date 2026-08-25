"""Punto b) Evolucion temporal del observable.

Calcula va(t) = (1/N)|sum_i (cos theta_i(t), sin theta_i(t))| a partir de trayectorias YA
GENERADAS para distintos eta, grafica las curvas superpuestas y marca con una linea
vertical el t donde cada una entra en estado estacionario segun el criterio de
vicsek_io.steady_state_start (media movil que deja de variar mas de --tol). Imprime tambien
<va> y su desvio en ese tramo estacionario, para elegir a ojo (viendo el grafico) el
--t-start que despues se usa en va_vs_eta.py.

Uso:
    python3 va_vs_t.py --traj traj_eta0.5.txt --label "eta=0.5" \
                       --traj traj_eta2.0.txt --label "eta=2.0" \
                       --traj traj_eta4.0.txt --label "eta=4.0" \
                       --out va_vs_t.png
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

sys.path.insert(0, str(Path(__file__).resolve().parent))
from vicsek_io import read_trajectory, polarization_series, steady_state_start


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--traj", action="append", required=True,
                         help="trajectory.txt (repetir por cada eta)")
    parser.add_argument("--label", action="append", required=True,
                         help="etiqueta por --traj, mismo orden (ej: 'eta=0.5')")
    parser.add_argument("--window", type=int, default=30,
                         help="ventana de la media movil para detectar estacionario")
    parser.add_argument("--tol", type=float, default=0.02,
                         help="tolerancia relativa de variacion entre ventanas")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    if len(args.traj) != len(args.label):
        raise SystemExit("--traj y --label deben aparecer la misma cantidad de veces")

    fig, ax = plt.subplots(figsize=(7, 5))
    colors = plt.cm.tab10.colors
    for i, (traj_path, label) in enumerate(zip(args.traj, args.label)):
        ts, _, angles = read_trajectory(traj_path)
        va = polarization_series(angles)
        t_start = steady_state_start(va, window=args.window, tol=args.tol)
        color = colors[i % len(colors)]
        ax.plot(ts, va, label=label, color=color)
        ax.axvline(ts[t_start], color=color, linestyle="--", alpha=0.7)
        mean, std = va[t_start:].mean(), va[t_start:].std()
        print(f"{label}: t_start={ts[t_start]} (de {ts[-1]}), "
              f"<va>={mean:.4f} +- {std:.4f}")

    ax.set_xlabel("t")
    ax.set_ylabel("va(t)")
    ax.set_ylim(0, 1.02)
    ax.set_title("Evolucion temporal de la polarizacion")
    ax.legend()
    fig.tight_layout()
    fig.savefig(args.out, dpi=150)
    print(f"Grafico guardado en {args.out}")


if __name__ == "__main__":
    main()
