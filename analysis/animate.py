"""Punto a) Animaciones.

Lee un trajectory.txt YA GENERADO (no corre nada nuevo - toma el archivo de texto como
input, tal como pide el enunciado: "el modulo de animacion se ejecuta en forma
independiente tomando estos archivos de texto como input"). Representa cada particula
como el vector (cos theta, sin theta) con origen en su posicion, coloreado segun su
angulo (colormap ciclico hsv, hue = theta / 2pi).

Uso:
    python3 animate.py --traj ../output/examples/traj_low_eta.txt --L 10 \
        --out ../output/examples/anim_low_eta.gif
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation, PillowWriter

sys.path.insert(0, str(Path(__file__).resolve().parent))
from vicsek_io import read_trajectory


def build_animation(traj_path, l, stride=1):
    ts, positions, angles = read_trajectory(traj_path)
    idxs = list(range(0, len(ts), stride))

    fig, ax = plt.subplots(figsize=(6, 6))
    ax.set_xlim(0, l)
    ax.set_ylim(0, l)
    ax.set_aspect("equal")

    pos0 = positions[idxs[0]]
    theta0 = angles[idxs[0]]
    quiver = ax.quiver(
        pos0[:, 0], pos0[:, 1], np.cos(theta0), np.sin(theta0), theta0,
        cmap="hsv", clim=(0, 2 * np.pi), pivot="tail",
        angles="xy", scale_units="xy", scale=1.5,
    )
    title = ax.set_title(f"t = {ts[idxs[0]]}")

    def update(frame_idx):
        i = idxs[frame_idx]
        pos = positions[i]
        theta = angles[i]
        quiver.set_offsets(pos)
        quiver.set_UVC(np.cos(theta), np.sin(theta), theta)
        title.set_text(f"t = {ts[i]}")
        return quiver, title

    anim = FuncAnimation(fig, update, frames=len(idxs), interval=1000 / 20, blit=False)
    return anim, fig


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--traj", required=True, help="trajectory.txt ya generado")
    parser.add_argument("--L", type=float, default=10.0)
    parser.add_argument("--out", required=True, help="archivo de salida (.gif)")
    parser.add_argument("--stride", type=int, default=1, help="tomar 1 de cada N frames")
    parser.add_argument("--fps", type=int, default=15)
    args = parser.parse_args()

    anim, fig = build_animation(args.traj, args.L, stride=args.stride)
    anim.save(args.out, writer=PillowWriter(fps=args.fps))
    plt.close(fig)
    print(f"Animacion guardada en {args.out}")


if __name__ == "__main__":
    main()
