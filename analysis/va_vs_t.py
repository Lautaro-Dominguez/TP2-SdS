"""Punto b) Evolucion temporal del observable.

Calcula va(t) = (1/N)|sum_i (cos theta_i(t), sin theta_i(t))| a partir de trayectorias YA
GENERADAS para distintos eta y grafica las curvas superpuestas. El inicio del estado
estacionario se determina a ojo mirando el grafico (un humano lo decide) y ese t se pasa
despues como --t-start a va_vs_eta.py - este script no marca ni calcula ningun t_start
automaticamente.

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
from vicsek_io import read_trajectory, polarization_series


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--traj", action="append", required=True,
                         help="trajectory.txt (repetir por cada eta)")
    parser.add_argument("--label", action="append", required=True,
                         help="etiqueta por --traj, mismo orden (ej: 'eta=0.5')")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    if len(args.traj) != len(args.label):
        raise SystemExit("--traj y --label deben aparecer la misma cantidad de veces")

    fig, ax = plt.subplots(figsize=(7, 5))
    for traj_path, label in zip(args.traj, args.label):
        ts, _, angles = read_trajectory(traj_path)
        va = polarization_series(angles)
        ax.plot(ts, va, label=label)

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
