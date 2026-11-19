#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
for folder in Task01 Task02 Task03 Task04 Task05/CompleteCode Task05/Baseline Task05/Updated Task06; do
  (cd "$folder"; javac *.java)
done
printf 'All programs compiled successfully.\n'
