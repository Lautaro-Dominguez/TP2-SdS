#!/bin/bash
# va_vs_eta / s_vs_eta / va_vs_s: corren simulaciones nuevas (no leen los .txt de
# simexec.sh) y necesitan un --t-start elegido a ojo mirando los va_vs_t_*/s_vs_t_*.png
# que genera analyzeexec.sh. Correr aparte, una vez decidido ese t:
#
#   ./analyzeexec_eta.sh <t_start>
set -e
cd "$(dirname "$0")"

if [ -z "$1" ]; then
    echo "Uso: $0 <t_start>" >&2
    echo "  t_start: elegido a ojo mirando va_vs_t_*.png/s_vs_t_*.png (generados por analyzeexec.sh)" >&2
    exit 1
fi
T_START=$1

OUT=../output
ANALYSIS=../analysis
mkdir -p "$OUT"

MODELS=(estandar votante)
ETA_SWEEP=0:8:0.5  # barrido real para trazar la curva (no los 3 eta fijos de simexec.sh)
ITERATIONS=500       # editable: cuantos pasos simula cada realizacion del barrido

echo "== va_vs_eta / s_vs_eta / va_vs_s: una imagen por modelo, curvas superpuestas por densidad (t_start=$T_START) =="
for m in "${MODELS[@]}"; do
    # --seeds no se pasa: usa el default del script (5 realizaciones por punto)
    python3 "$ANALYSIS/va_vs_eta.py" --densities 2,4,8 --etas "$ETA_SWEEP" \
        --t-start "$T_START" --iterations "$ITERATIONS" --model "$m" --rc 1 --L 10 \
        --out "$OUT/va_vs_eta_${m}.png"

    python3 "$ANALYSIS/s_vs_eta.py" --densities 2,4,8 --etas "$ETA_SWEEP" \
        --t-start "$T_START" --iterations "$ITERATIONS" --model "$m" --rc 1 --L 10 \
        --out "$OUT/s_vs_eta_${m}.png"

    python3 "$ANALYSIS/va_vs_s.py" --densities 2,4,8 --etas "$ETA_SWEEP" \
        --t-start "$T_START" --iterations "$ITERATIONS" --model "$m" --rc 1 --L 10 \
        --out "$OUT/va_vs_s_${m}.png"
done
