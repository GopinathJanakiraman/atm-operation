#!/usr/bin/env bash

set -e

cd "$(dirname "$0")"

rm -rf out
mkdir -p out

find src -name "*.java" > sources.txt
javac -d out @sources.txt
rm sources.txt

java -cp out com.mitsubishi.atm.main.StartAtm