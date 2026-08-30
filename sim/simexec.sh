#!/bin/bash
set -e
cd "$(dirname "$0")"
mkdir -p ../output

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 2 --out ../output/trajectory1.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory1.txt --out ../output/trajectory1.txt \
    --clustersOut ../output/clusters1.txt --timingOut ../output/timing1.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 2 --out ../output/trajectory2.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory2.txt --out ../output/trajectory2.txt \
    --clustersOut ../output/clusters2.txt --timingOut ../output/timing2.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 2 --out ../output/trajectory3.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 2 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory3.txt --out ../output/trajectory3.txt \
    --clustersOut ../output/clusters3.txt --timingOut ../output/timing3.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 2 --out ../output/trajectory4.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 2 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory4.txt --out ../output/trajectory4.txt \
    --clustersOut ../output/clusters4.txt --timingOut ../output/timing4.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 2 --out ../output/trajectory5.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 8 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory5.txt --out ../output/trajectory5.txt \
    --clustersOut ../output/clusters5.txt --timingOut ../output/timing5.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 2 --out ../output/trajectory6.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 8 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory6.txt --out ../output/trajectory6.txt \
    --clustersOut ../output/clusters6.txt --timingOut ../output/timing6.txt"

############################

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 4 --out ../output/trajectory7.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory7.txt --out ../output/trajectory7.txt \
    --clustersOut ../output/clusters7.txt --timingOut ../output/timing7.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 4 --out ../output/trajectory8.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory8.txt --out ../output/trajectory8.txt \
    --clustersOut ../output/clusters8.txt --timingOut ../output/timing8.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 4 --out ../output/trajectory9.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 2 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory9.txt --out ../output/trajectory9.txt \
    --clustersOut ../output/clusters9.txt --timingOut ../output/timing9.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 4 --out ../output/trajectory10.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 2 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory10.txt --out ../output/trajectory10.txt \
    --clustersOut ../output/clusters10.txt --timingOut ../output/timing10.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 4 --out ../output/trajectory11.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 8 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory11.txt --out ../output/trajectory11.txt \
    --clustersOut ../output/clusters11.txt --timingOut ../output/timing11.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 4 --out ../output/trajectory12.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 8 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory12.txt --out ../output/trajectory12.txt \
    --clustersOut ../output/clusters12.txt --timingOut ../output/timing12.txt"

############################

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 8 --out ../output/trajectory13.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory13.txt --out ../output/trajectory13.txt \
    --clustersOut ../output/clusters13.txt --timingOut ../output/timing13.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 8 --out ../output/trajectory14.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 0.5 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory14.txt --out ../output/trajectory14.txt \
    --clustersOut ../output/clusters14.txt --timingOut ../output/timing14.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 8 --out ../output/trajectory15.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 2 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory15.txt --out ../output/trajectory15.txt \
    --clustersOut ../output/clusters15.txt --timingOut ../output/timing15.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 8 --out ../output/trajectory16.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 2 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory16.txt --out ../output/trajectory16.txt \
    --clustersOut ../output/clusters16.txt --timingOut ../output/timing16.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 8 --out ../output/trajectory17.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 8 --iterations 500 --model estandar --rc 1 \
    --in ../output/trajectory17.txt --out ../output/trajectory17.txt \
    --clustersOut ../output/clusters17.txt --timingOut ../output/timing17.txt"

mvn exec:java -Dexec.mainClass=sim.app.GenerateParticlesMain \
    -Dexec.args="--L 10 --density 8 --out ../output/trajectory18.txt"

mvn exec:java -Dexec.mainClass=sim.app.SimulateMain \
    -Dexec.args="--eta 8 --iterations 500 --model votante --rc 1 \
    --in ../output/trajectory18.txt --out ../output/trajectory18.txt \
    --clustersOut ../output/clusters18.txt --timingOut ../output/timing18.txt"