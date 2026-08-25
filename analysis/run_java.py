"""Invoca sim.app.GenerateParticlesMain / sim.app.SimulateMain directamente con `java`
(usando las clases ya compiladas en sim/target/classes), sin pasar por Maven en cada
corrida - `mvn exec:java` agrega overhead de arranque que se nota cuando se corren cientos
de simulaciones para el punto c. El motor Java en si no se modifica: esto solo lo invoca
como subproceso, igual que se haria a mano desde la linea de comandos.

Requiere haber corrido `mvn compile` (o `mvn package`) al menos una vez en sim/.
"""
from __future__ import annotations

import subprocess
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
CLASSES = REPO_ROOT / "sim" / "target" / "classes"


def _run(main_class, flags):
    if not CLASSES.exists():
        raise SystemExit(
            f"No existe {CLASSES} - corre `mvn compile` en sim/ antes de usar este script.")
    cmd = ["java", "-cp", str(CLASSES), main_class]
    for key, value in flags.items():
        cmd += [f"--{key}", str(value)]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(
            f"{main_class} fallo (args={flags}):\n{result.stdout}\n{result.stderr}")
    return result.stdout


def generate_particles(l, density, seed, out):
    return _run("sim.app.GenerateParticlesMain", {
        "L": l,
        "density": density,
        "seed": seed,
        "out": out,
    })


def simulate(eta, iterations, model, rc, l, periodic, speed, seed, infile, outfile,
             clusters_out, timing_out):
    return _run("sim.app.SimulateMain", {
        "eta": eta,
        "iterations": iterations,
        "model": model,
        "rc": rc,
        "L": l,
        "periodic": str(bool(periodic)).lower(),
        "speed": speed,
        "seed": seed,
        "in": infile,
        "out": outfile,
        "clustersOut": clusters_out,
        "timingOut": timing_out,
    })
