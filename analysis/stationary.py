"""Helper compartido por s_vs_eta.py (grafico 7, S vs eta) y va_vs_s.py (grafico 8, va vs S).

Ambos graficos necesitan, para cada (densidad, eta), varias realizaciones independientes
(sin semilla fija, mismo criterio que va_vs_eta.py) de las que se promedian va(t) y S(t) en
el estado estacionario (desde --t-start, elegido a ojo con va_vs_t.py / s_vs_t.py). Como
una sola corrida de sim.app.SimulateMain ya escribe ambas series (trajectory.txt y
clusters.txt) en un solo llamado, este modulo corre cada realizacion UNA vez y devuelve
ambos promedios, en vez de que cada script vuelva a simular todo desde cero por separado
(lo que duplicaria el costo de computo, que ya es alto: cientos de simulaciones completas
por grafico).

No se toca va_vs_eta.py (puntos a/b/c, ya entregados) - esto es codigo nuevo aparte que solo
reutiliza sim.app.GenerateParticlesMain / SimulateMain como subproceso, igual que ya hacia
run_java.py.
"""
from __future__ import annotations

import shutil
import tempfile
from pathlib import Path

import numpy as np

from vicsek_io import read_trajectory, polarization_series, read_cluster_sizes
from run_java import generate_particles, simulate


def parse_range(spec):
    """'0:5:0.25' -> np.arange(0, 5+0.25, 0.25) (start:stop:step, stop inclusive)."""
    start, stop, step = (float(x) for x in spec.split(":"))
    return np.round(np.arange(start, stop + step / 2, step), 6)


def run_realization(density, l, eta, model, rc, speed, iterations, run_idx, workdir):
    """Una realizacion sin semilla fija (posiciones/angulos iniciales y ruido completamente
    aleatorios, igual que run_one en va_vs_eta.py). Devuelve (va_series, s_series), las dos
    series temporales completas (0..iterations) de esa corrida.
    """
    tag = f"rho{density}_eta{eta:.4f}_run{run_idx}"
    traj = workdir / f"traj_{tag}.txt"
    clusters = workdir / f"clusters_{tag}.txt"
    timing = workdir / f"timing_{tag}.txt"
    generate_particles(l, density, traj)
    simulate(eta, iterations, model, rc, l, True, speed, traj, traj, clusters, timing)
    _, _, angles = read_trajectory(traj)
    va = polarization_series(angles)
    s = read_cluster_sizes(clusters)
    traj.unlink(missing_ok=True)
    clusters.unlink(missing_ok=True)
    timing.unlink(missing_ok=True)
    return va, s


def stationary_mean(series, t_start, label="serie"):
    """Promedio de `series` desde t_start hasta el final (estado estacionario)."""
    if t_start >= len(series):
        raise SystemExit(
            f"--t-start {t_start} >= pasos simulados ({len(series)}) para {label}; "
            "subi --iterations")
    return float(series[t_start:].mean())


def new_workdir(workdir_arg, prefix):
    """Replica el manejo de workdir de va_vs_eta.py: uno propio en /tmp si no se paso
    --workdir, y en ese caso se borra al terminar salvo --keep-workdir."""
    own_tmp = workdir_arg is None
    workdir = Path(workdir_arg) if workdir_arg else Path(tempfile.mkdtemp(prefix=prefix))
    workdir.mkdir(parents=True, exist_ok=True)
    return workdir, own_tmp


def cleanup_workdir(workdir, own_tmp, keep):
    if own_tmp and not keep:
        shutil.rmtree(workdir, ignore_errors=True)
