
# Q3 - Engineer the hashCode-equals Contract

## Aim
To understand how equals() and hashCode() work together in HashSet to identify duplicate students.

## Experiment 1: Without hashCode()

The Student class overrides equals() but does not override hashCode().

Expected output:
Unique students: 3

Explanation:
HashSet uses hashCode() to identify the bucket and equals() to compare objects within that bucket. Different hash codes can cause equal Student objects to be stored separately.

## Experiment 2: With hashCode()

The hashCode() method is overridden using regNo.hashCode().

Expected output:
Unique students: 2

Explanation:
Both equals() and hashCode() use regNo. Therefore, students with the same registration number are treated as duplicates.

## Additional Test

If regNo is changed after insertion into HashSet, the object may become difficult to locate or remove because its hash code changes.

## Time Complexity

- HashSet add(): O(1) average.
- HashSet contains(): O(1) average.
- HashSet remove(): O(1) average.
- Worst case: O(n), depending on hash collisions.

## Space Complexity

O(n) for n unique students.

## Alternative Concepts

TreeSet:
Uses a Comparator or Comparable and provides O(log n) insertion and lookup.

HashMap:
Can use registration number as the key and Student as the value. This provides O(1) average lookup.

## Conclusion

The hashCode() method must be consistent with equals(). Both methods should use the same equality-defining fields. Registration numbers should remain unchanged while objects are stored in a HashSet.
