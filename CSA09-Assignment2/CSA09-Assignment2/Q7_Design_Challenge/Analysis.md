
# Q7 - Diagnose, Redesign and Validate

## A. Diagnose the Bottleneck

The original railway ticket generator uses String concatenation inside a loop.

String is immutable. Every concatenation produces a new String containing the previous ticket content and the new field.

As the number of fields increases, the existing characters are repeatedly copied.

This increases execution time and temporary object allocation.

The bottleneck is repeated copying and allocation during ticket construction.

Refer to the Q1 experimental results for the original String performance measurements.

## B. Define the Design Objective

The redesigned solution must:

1. Reduce repeated String creation.
2. Reduce character copying.
3. Improve ticket generation speed.
4. Preserve the original ticket content.
5. Reuse allocated memory across tickets.

Measurable objective:
Achieve at least a 2x speedup for the largest workload while producing identical ticket content.

## C. Design the Revised Solution

The original solution repeatedly concatenates immutable Strings.

The revised solution uses a reusable StringBuilder with an initial capacity of 1024 characters.

Before generating each ticket, setLength(0) clears the previous content while retaining the allocated buffer.

Each field is appended to the buffer.

Finally, toString() returns the completed ticket.

### Pseudocode

Create reusable StringBuilder

FOR each ticket:
    Clear builder
    FOR each field:
        Append field
        Append newline
    END FOR
    Convert builder to String
    Return completed ticket
END FOR

### Benefits

- Avoids repeatedly copying the entire ticket.
- Reduces temporary String objects.
- Reuses previously allocated buffer capacity.
- Preserves the original output.

### Trade-offs

- The reusable buffer retains its allocated capacity.
- A large ticket may cause the buffer to grow.
- The generator must not be shared unsafely between threads.
- Each booking thread should use its own generator.

## D. Prediction Before Implementation

The redesigned solution is expected to perform better as the number of fields increases.

The original solution performs O(n squared) character copying for n constant-length fields.

The redesigned solution performs O(n) character copying, assuming normal amortized buffer growth.

The difference should become more significant for larger workloads.

## E. Implementation and Validation

Files:
- OriginalSolution.java
- RedesignedSolution.java
- DesignBenchmark.java

Both solutions generate tickets from identical input fields.

The benchmark checks output equality before measuring performance.

Three workloads are tested using five repeated runs.

JVM warm-up is performed before measurements.

The execution order is alternated between runs.

### Experimental Results

| Tickets | Fields | Original Average (ns) | Redesigned Average (ns) | Speedup |
|---|---|---|---|---|
| 100 | 10 | | | |
| 1000 | 50 | | | |
| 10000 | 100 | | | |

Fill this table using the Average rows in results.csv.

Speedup = Original Average Time / Redesigned Average Time

### Estimated Object Creation

For a ticket with n fields:

Original:
Approximately n concatenated String results are produced.

Redesigned:
One final String is produced per ticket, with a reusable StringBuilder.

Actual JVM allocation may vary because of compiler and runtime optimizations.

### Complexity Comparison

| Measure | Original | Redesigned |
|---|---|---|
| Time per ticket | O(n squared) | O(n) amortized |
| Peak temporary space | O(n) | O(n) |
| Cumulative character allocation | O(n squared) | O(n) amortized |
| Output correctness | Preserved | Preserved |

For m tickets of n constant-length fields:

Original: O(m x n squared)

Redesigned: O(m x n)

## F. Defend the Final Design

Compare the actual measured speedup with the target of 2x for the largest workload.

If the measured speedup exceeds the target, the redesign achieves its performance objective.

If it does not, discuss JVM warm-up, garbage collection, workload size and measurement variation.

The original solution may still be preferable for very short strings or simple one-time concatenation because it is easy to understand.

The redesigned solution is preferable for repeated generation of large tickets.

The reusable buffer should be owned by one thread or generator instance to avoid concurrency problems.

## Conclusion

Reusing a StringBuilder reduces repeated character copying and unnecessary temporary object creation.

The redesign preserves ticket content and provides a more scalable approach to repeated ticket generation.

The final recommendation must be supported by the actual benchmark results.
