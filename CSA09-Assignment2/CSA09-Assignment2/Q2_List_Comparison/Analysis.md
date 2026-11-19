
# Q2 - ArrayList, LinkedList and ArrayDeque Comparison

## Aim
To compare ArrayList, LinkedList and ArrayDeque based on insertion, deletion and accessing operations.

## Experiment
Number of elements: 10,000

## Observations

1. ArrayList stores elements in a dynamic array.
2. LinkedList stores elements as connected nodes.
3. ArrayDeque uses a resizable array and supports insertion and deletion at both ends.

## Time Complexity Comparison

| Operation | ArrayList | LinkedList | ArrayDeque |
|---|---|---|---|
| Add at end | O(1) amortized | O(1) | O(1) amortized |
| Insert at front | O(n) | O(1) | O(1) amortized |
| Insert in middle | O(n) | O(n) | O(n) |
| Get kth element | O(1) | O(n) | Not supported |
| Iterate | O(n) | O(n) | O(n) |
| Remove front | O(n) | O(1) | O(1) |

## Space Complexity

- ArrayList: O(n), uses array storage.
- LinkedList: O(n), uses additional memory for nodes and links.
- ArrayDeque: O(n), uses a resizable array.

## Advantages

ArrayList:
- Fast random access.
- Simple to use.
- Suitable for frequent reading.

LinkedList:
- Efficient insertion and deletion at both ends.
- Useful when working with nodes.

ArrayDeque:
- Fast insertion and deletion at both ends.
- Suitable for queue operations.

## Conclusion

ArrayList is suitable for playlists when users frequently access songs by position and add songs at the end.

LinkedList is useful when frequent insertion and deletion are required at known positions.

ArrayDeque is a better choice for an Up Next queue where songs are frequently added at both ends and removed from the front.
