
# Q6 - Priority, Order and Fairness in a Queue

## Aim

To implement a hospital emergency triage system using PriorityQueue and Comparator in Java.

## Problem Statement

Patients have severity levels from 1 to 5.

Severity 1 represents the most critical patients.

Patients with the same severity must be treated in arrival order.

## Prediction

PriorityQueue admission and removal have O(log n) worst-case heap operation complexity.

As queue size increases, total processing time is expected to increase.

## Why a Normal Queue Is Not Suitable

A normal FIFO queue serves patients in arrival order.

It cannot automatically serve a later-arriving critical patient before a less severe patient.

## PriorityQueue Design

The comparator uses:

1. Severity in ascending order.
2. Arrival counter in ascending order.

The arrival counter ensures FIFO ordering among patients with equal severity.

## Correctness Test

Expected treatment order for eight patients:

1. Kiran(S1)
2. Vijay(S1)
3. Priya(S2)
4. Rahul(S2)
5. Divya(S3)
6. Sneha(S3)
7. Arun(S4)
8. Neha(S5)

Compare this order with the output from TriageTest.java.

## Why Printing PriorityQueue Is Not Sorted

PriorityQueue uses a heap internally.

Only the head is guaranteed to contain the highest-priority element.

The remaining elements are not necessarily stored in sorted order.

To display patients in treatment order without changing the original queue, create a copy and repeatedly call poll() on the copy.

## Experimental Results

Five runs were performed at each queue size.

| Queue Size | Average Admission Time (ns) | Average Removal Time (ns) |
|---|---|---|
| 100 | | |
| 1000 | | |
| 10000 | | |

Fill the table using benchmark_results.txt.

## Starvation

A severity-5 patient may wait indefinitely if more severe patients continuously arrive.

This problem is called starvation.

## Fairness Solution

Introduce an aging mechanism.

Record the waiting time of every patient.

For every 30 minutes of waiting, improve the effective severity by one level, without going below severity 1.

When priorities change, rebuild the PriorityQueue so that the heap remains correctly ordered.

Among patients with the same effective severity, retain the original arrival order.

In a real hospital, medical staff must approve priority changes based on clinical urgency.

## Complexity Comparison

| Data Structure | Admission | Serve Next | Space |
|---|---|---|---|
| PriorityQueue | O(log n) | O(log n) | O(n) |
| Sorted LinkedList | O(n) | O(1) | O(n) |
| Five FIFO Queues | O(1) amortized | O(1) | O(n) |

## Alternative 1: Sorted LinkedList

Insert every patient at the correct position according to severity and arrival order.

Insertion requires scanning the list.

Serving the next patient removes the first element.

## Alternative 2: Five FIFO Queues

Create one ArrayDeque for each severity level.

Admission:
queues[severity - 1].addLast(patient)

Serving:
Check queues from severity 1 to severity 5.
Remove the first patient from the first non-empty queue.

Since there are only five severity levels, checking them takes constant time.

## Deployment Decision

A hospital receiving 5,000 patients per day can potentially use a PriorityQueue because admission and removal are efficient.

However, daily patient count is not the same as the maximum number of patients waiting simultaneously.

Actual deployment must consider peak arrivals, concurrent access, clinical safety and fairness.

PriorityQueue is not thread-safe, so concurrent access requires appropriate synchronization.

## Conclusion

PriorityQueue provides an efficient method for treating patients based on medical priority.

The arrival counter preserves fairness among patients with equal severity.

For a fixed set of five severity levels, five FIFO queues provide an efficient alternative with constant-time admission and removal.
