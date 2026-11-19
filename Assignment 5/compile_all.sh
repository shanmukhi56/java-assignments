#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
for D in Task01 Task02 Task03 Task04; do
  echo "Compiling $D..."
  (cd "$D" && javac *.java)
done
echo "All Assignment 5 tasks compiled successfully on JDK 21."
