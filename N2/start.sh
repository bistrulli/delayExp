#!/bin/sh

sudo echo "100000 100000" > /sys/sys/fs/cgroup/N1/e1/cpu.max

redis-cli set N2_sla 0.15
alfa=0.95
nr=20
java -jar ./target/N2-0.0.1-jar-with-dependencies.jar --cpuEmu 0 --cgv2 1 --port 3200 --name N2 --alfa $alfa --tgt 0.15 --nr $nr  --dbHost localhost 
