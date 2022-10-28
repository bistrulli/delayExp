#!/bin/sh

redis-cli set N2_sla 0.15
java -jar ./target/N2-0.0.1-jar-with-dependencies.jar --cpuEmu 0 --cgv2 1 --port 3200 --name N2 --alfa 0.95 --tgt 0.15 --nr 20  --dbHost localhost 
