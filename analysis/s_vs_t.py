"""Evolucion temporal del tamano de cluster.

Grafica S(t) = fraccion de particulas en el cluster conexo mas grande, ya calculada por el
motor Java (sim.vicsek.ClusterAnalysis) y escrita por SimulateMain en clusters.txt - una linea
por valor, S(0) a S(tn). A diferencia de va_vs_t.py, no hace falta recalcular nada a partir de
la trayectoria: este script solo lee esa lista de floats y superpone las curvas pedidas.

Uso:
    python3 s_vs_t.py --clusters clusters_eta0.5.txt --label "eta=0.5" \
                      --clusters clusters_eta2.0.txt --label "eta=2.0" \
                      --clusters clusters_eta4.0.txt --label "eta=4.0" \
                      --out s_vs_t.png
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

sys.path.insert(0, str(Path(__file__).resolve().parent))
from vicsek_io import read_cluster_sizes


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--clusters", action="append", required=True,
                         help="clusters.txt (repetir por cada corrida/eta)")
    parser.add_argument("--label", action="append", required=True,
                         help="etiqueta por --clusters, mismo orden (ej: 'eta=0.5')")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    if len(args.clusters) != len(args.label):
        raise SystemExit("--clusters y --label deben aparecer la misma cantidad de veces")

    fig, ax = plt.subplots(figsize=(7, 5))
    for clusters_path, label in zip(args.clusters, args.label):
        s = read_cluster_sizes(clusters_path)
        ts = range(len(s))
        ax.plot(ts, s, label=label)

    ax.set_xlabel("t")
    ax.set_ylabel("S(t)")
    ax.set_ylim(0, 1.02)
    ax.set_title("Evolucion temporal del cluster mas grande")
    ax.legend()
    fig.tight_layout()
    fig.savefig(args.out, dpi=150)
    print(f"Grafico guardado en {args.out}")


if __name__ == "__main__":
    main()
