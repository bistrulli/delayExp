#!/bin/sh

java -jar ./target/N1-0.0.1-jar-with-dependencies.jar --cpuEmu 0 --cgv2 1 --port 3100 --name N1 --alfa 0.975 --tgt 0.4 --nr 20 --dbHost localhost
