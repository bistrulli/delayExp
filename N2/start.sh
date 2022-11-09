#!/bin/sh

sudo echo "max" > /sys/fs/cgroup/N2/e1/cpu.max

redis-cli set N2_sla 0.14
alfa=0.92
nr=10
taskset -c 1-31 java -jar ./target/N2-0.0.1-jar-with-dependencies.jar --cpuEmu 0 --cgv2 1 --port 3200 --name N2 --alfa $alfa --tgt 0.14 --nr $nr  --dbHost localhost 
