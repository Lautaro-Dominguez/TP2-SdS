#!/bin/bash
set -e
cd "$(dirname "$0")"

OUT=../output
ANALYSIS=../analysis
mkdir -p "$OUT"

# mismos params que simexec.sh
DENSITIES=(2 4 8)
ETAS=(0.5 2 8)
MODELS=(estandar votante)

# misma numeracion que simexec.sh: N = di*6 + ei*2 + mi + 1 (indices 0-based
# sobre DENSITIES/ETAS/MODELS, en ese orden de anidado)
index() {
    local di=$1 ei=$2 mi=$3
    echo $((di * 6 + ei * 2 + mi + 1))
}

echo "== va_vs_t / s_vs_t: una imagen por densidad y modelo, curvas superpuestas por eta =="
for di in "${!DENSITIES[@]}"; do
    d="${DENSITIES[$di]}"
    for mi in "${!MODELS[@]}"; do
        m="${MODELS[$mi]}"
        traj_args=()
        clusters_args=()
        for ei in "${!ETAS[@]}"; do
            eta="${ETAS[$ei]}"
            n=$(index "$di" "$ei" "$mi")
            traj_args+=(--traj "$OUT/trajectory${n}.txt" --label "eta=${eta}")
            clusters_args+=(--clusters "$OUT/clusters${n}.txt" --label "eta=${eta}")
        done
        python3 "$ANALYSIS/va_vs_t.py" "${traj_args[@]}" --out "$OUT/va_vs_t_d${d}_${m}.png"
        python3 "$ANALYSIS/s_vs_t.py" "${clusters_args[@]}" --out "$OUT/s_vs_t_d${d}_${m}.png"
    done
done

# Animaciones (punto a): una por corrida, misma numeracion 1..18 que simexec.sh.
# Apagado por default: son pesadas (500 iteraciones x 18 corridas) y no hacen
# falta para el resto del analisis. Poner en true para generarlas.
RUN_ANIMATIONS=true
if [ "$RUN_ANIMATIONS" = true ]; then
    echo "== animate: una animacion por corrida =="
    for di in "${!DENSITIES[@]}"; do
        for ei in "${!ETAS[@]}"; do
            for mi in "${!MODELS[@]}"; do
                n=$(index "$di" "$ei" "$mi")
                python3 "$ANALYSIS/animate.py" --traj "$OUT/trajectory${n}.txt" --L 10 \
                    --out "$OUT/anim${n}.gif"
            done
        done
    done
fi

# va_vs_eta / s_vs_eta / va_vs_s corren simulaciones nuevas (no leen los .txt de
# simexec.sh) y necesitan un --t-start elegido a ojo mirando los va_vs_t_*/s_vs_t_*.png
# generados arriba. Se corren aparte, una vez decidido ese t:
#   ./analyzeexec_eta.sh <t_start>
