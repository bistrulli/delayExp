#!/bin/sh

sudo echo "max" > /sys/fs/cgroup/N1/e1/cpu.max

redis-cli set N1_sla 0.24
alfa=0.90
nr=10
taskset -c 1-31 java -jar ./target/N1-0.0.1-jar-with-dependencies.jar --cpuEmu 0 --cgv2 1 --port 3100 --name N1 --alfa $alfa --tgt 0.24 --nr $nr --dbHost localhost
