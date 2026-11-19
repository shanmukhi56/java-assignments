
# Q5 - Generic Ranking Component

## Aim
To develop a reusable sports leaderboard using Generics and Comparator in Java.

## Design

The Leaderboard<T> class stores items of any type and accepts a Comparator<? super T> to determine their ranking.

A separate Comparator is preferred because different competitions may use different ranking rules.

## Ranking Rules

1. Points in descending order.
2. Goal difference in descending order.
3. Team name in alphabetical order.

## Experiment 1

Input:
Panthers(9,4)
Eagles(9,6)
Cobras(6,1)
Bisons(9,4)

Expected Output:
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]

Bisons is ranked above Panthers because both have equal points and goal difference, but Bisons comes first alphabetically.

## Experiment 2

A faulty comparator compares only points.

The correct comparator also considers goal difference and team name.

The second dataset demonstrates that comparing only points produces an incorrect ranking according to the competition rules.

## Correctness

The comparator compares fields in priority order.

If points differ, the team with higher points ranks first.

If points are equal, goal difference determines the ranking.

If both are equal, alphabetical order determines the ranking.

This lexicographic comparison is transitive.

Teams with identical ranking fields compare as equal.

Integer comparisons avoid overflow caused by subtraction.

## Generics

Leaderboard<T> can store teams, players, colleges or other objects.

Comparator<? super T> permits comparators defined for T or its parent types.

A method using List<? extends T> can accept lists containing T or its subclasses.

Example:

void addAll(List<? extends T> source) {
    items.addAll(source);
}

## Bounded Types

A natural-order alternative can use:

class Leaderboard<T extends Comparable<? super T>>

This ensures that T supports natural comparison.

## Alternative Concepts

### Comparable

Team can implement Comparable<Team> and define compareTo().

However, this defines one natural ranking order.

### Multiple Stable Sorts

Sort teams by name first, then goal difference, then points.

Stable sorting preserves the order established by earlier sorts when later comparison fields are equal.

## Complexity Comparison

| Approach | Time | Extra Space |
|---|---|---|
| Comparator Chain | O(n log n) | O(n) |
| Comparable | O(n log n) | O(n) |
| Three Stable Sorts | 3 x O(n log n) | O(n) |

All approaches have the same asymptotic sorting complexity.

The comparator chain needs only one sorting pass.

Creating a sorted copy requires O(n) additional space.

## Conclusion

Comparator chains provide a flexible and reusable ranking solution.

The generic Leaderboard class can support teams, players and colleges without changing its internal implementation.
