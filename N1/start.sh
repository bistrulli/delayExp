#!/bin/sh

redis-cli set N1_sla 0.25
alfa=0.9
nr=20
java -jar ./target/N1-0.0.1-jar-with-dependencies.jar --cpuEmu 0 --cgv2 1 --port 3100 --name N1 --alfa $alfa --tgt 0.25 --nr $nr --dbHost localhost
