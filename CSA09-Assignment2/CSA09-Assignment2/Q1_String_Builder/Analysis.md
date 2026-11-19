
# Q1 - String, StringBuilder and StringBuffer Comparison

## Aim
To compare the performance of String, StringBuilder and StringBuffer while generating parking tickets.

## Experiment
- Number of tickets: 10,000
- Programming Language: Java
- Measurement: System.nanoTime()

## Observations

1. String creates a new object whenever its value is modified.
2. StringBuilder modifies the same mutable sequence and is generally faster for repeated appending in a single thread.
3. StringBuffer also supports mutable strings and provides synchronized methods for thread safety.

## Execution Time

- String: ______ ns
- StringBuilder: ______ ns
- StringBuffer: ______ ns

## Result
The experiment compares the execution time of three string-handling approaches. StringBuilder is generally suitable for repeated string modifications in a single thread, while StringBuffer is useful when synchronized access is required.
