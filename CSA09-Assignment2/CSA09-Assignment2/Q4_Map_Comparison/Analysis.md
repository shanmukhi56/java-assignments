
# Q4 - Choosing a Map for Pharmacy Inventory

## Aim
To compare HashMap, TreeMap and LinkedHashMap for storing and retrieving medicine stock details.

## Prediction

HashMap is expected to provide fast insertion and lookup.

TreeMap is expected to be slower for insertion and lookup because it maintains keys in sorted order.

LinkedHashMap is expected to provide fast lookup while preserving insertion order.

## Experimental Setup

Medicine inventory sizes:
- 100 medicines
- 1,000 medicines
- 10,000 medicines

Number of lookups: 100,000

Measurement: System.nanoTime()

## Experimental Results

| Medicines | HashMap Lookup (ns) | TreeMap Lookup (ns) | LinkedHashMap Lookup (ns) |
|---|---|---|---|
| 100 | | | |
| 1000 | | | |
| 10000 | | | |

Fill the table using actual program output.

## Time Complexity

| Operation | HashMap | TreeMap | LinkedHashMap |
|---|---|---|---|
| put() | O(1) average | O(log n) | O(1) average |
| get() | O(1) average | O(log n) | O(1) average |
| Iteration | O(n + capacity) | O(n) | O(n) |
| Code-order report | O(n log n) | O(n) | O(n log n) |
| Insertion-order report | Requires extra tracking | Requires extra tracking | O(n) |

## Space Complexity

All three maps require O(n) space.

HashMap uses buckets and entries.
TreeMap uses tree nodes.
LinkedHashMap uses additional links to maintain insertion order.

## Report Ordering

HashMap:
Does not guarantee iteration order.

TreeMap:
Automatically maintains medicine codes in sorted order.

LinkedHashMap:
Maintains insertion order by default.

## Acceptable Response Time

The target average lookup time is less than 1 millisecond.

Average lookup time = Total lookup time / 100000.

The measured average must be compared with this target.

## Alternative Concept: Sorted Array

Medicines can be stored in an array sorted by medicine code.

Arrays.binarySearch() can locate a medicine.

Complexity:
- Lookup: O(log n)
- Insertion: O(n)
- Code-order report: O(n)
- Space: O(n)

A sorted array is suitable when the inventory changes rarely.

## Why Hashtable Is Not Preferred

Hashtable synchronizes its methods, which can introduce unnecessary overhead in single-threaded programs.

HashMap is generally preferred for ordinary operations. ConcurrentHashMap is more suitable for many concurrent applications.

## Conclusion

LinkedHashMap is a suitable choice for the pharmacy because it supports fast average lookup and preserves insertion order.

A sorted copy of its keys can be used to generate the evening code-order report.

TreeMap is preferable when sorted reports are generated frequently.
