"""Utilidades compartidas por los scripts de analisis (puntos a, b, c del TP2).

Lee el formato de trayectoria que escribe sim.io.TrajectoryFileWriter: un bloque por paso
de tiempo, con una linea de header (el indice t) seguida de una linea "x y angulo" por
particula. No se toca ni se reimplementa el motor Java - esto solo lee el .txt que ya
produce.
"""
from __future__ import annotations

import numpy as np


def read_trajectory(path):
    """Devuelve (ts, positions, angles).

    - ts: array de enteros (un valor de t por bloque)
    - positions: lista de arrays (N, 2), uno por bloque
    - angles: lista de arrays (N,), uno por bloque
    """
    with open(path) as f:
        lines = [line.strip() for line in f if line.strip()]

    ts = []
    positions = []
    angles = []
    i = 0
    while i < len(lines):
        t = int(lines[i])
        i += 1
        xs, ys, thetas = [], [], []
        while i < len(lines) and len(lines[i].split()) == 3:
            x, y, theta = (float(v) for v in lines[i].split())
            xs.append(x)
            ys.append(y)
            thetas.append(theta)
            i += 1
        ts.append(t)
        positions.append(np.column_stack([xs, ys]) if xs else np.empty((0, 2)))
        angles.append(np.array(thetas))
    return np.array(ts), positions, angles


def polarization(theta):
    """va = (1/N) * |sum_i (cos(theta_i), sin(theta_i))|.

    Formula de la catedra: va = (1/(N*v)) |sum_i v_i|, con v_i = v*(cos theta_i, sin
    theta_i). Como v es la misma para todas las particulas, se cancela al normalizar y
    queda esta expresion, que es la que se calcula directamente.
    """
    n = len(theta)
    if n == 0:
        return 0.0
    vx = np.cos(theta).sum() / n
    vy = np.sin(theta).sum() / n
    return float(np.hypot(vx, vy))


def polarization_series(angles):
    """va(t) para cada bloque de angulos (uno por t) de una trayectoria."""
    return np.array([polarization(theta) for theta in angles])


def steady_state_start(series, window=30, tol=0.02, min_start=0):
    """Heuristica para detectar el inicio del estado estacionario.

    Compara la media movil de `series` en una ventana de tamano `window` contra la misma
    media `window` pasos despues; el primer punto donde la variacion relativa entre ambas
    cae por debajo de `tol` se toma como inicio del estacionario. Pensado para verificarse
    a ojo con el grafico (por eso los scripts de este paquete siempre marcan t_start con
    una linea vertical), tal como pide el enunciado.
    """
    n = len(series)
    if n < 2 * window:
        return max(min_start, n // 2)
    means = np.array([series[i:i + window].mean() for i in range(n - window + 1)])
    last_valid = len(means) - window
    for i in range(min_start, max(min_start, last_valid)):
        a = means[i]
        b = means[i + window]
        denom = abs(a) if abs(a) > 1e-9 else 1e-9
        if abs(b - a) / denom < tol:
            return i
    return max(min_start, n // 2)
