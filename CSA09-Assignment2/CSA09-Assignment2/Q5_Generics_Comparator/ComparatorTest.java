
import java.util.*;

public class ComparatorTest {

    public static void main(String[] args) {

        List<Team> teams = new ArrayList<>();

        teams.add(new Team("Zebras", 10, 5));
        teams.add(new Team("Lions", 10, 5));
        teams.add(new Team("Tigers", 10, 8));
        teams.add(new Team("Bears", 7, 3));
        teams.add(new Team("Wolves", 10, 2));

        // Faulty comparator: ignores tie-breakers
        Comparator<Team> faulty =
            (a, b) -> Integer.compare(b.points, a.points);

        // Correct comparator
        Comparator<Team> correct = Comparator
            .comparingInt((Team t) -> t.points)
            .reversed()
            .thenComparing(
                Comparator.comparingInt(
                    (Team t) -> t.goalDiff
                ).reversed()
            )
            .thenComparing(t -> t.name);

        List<Team> faultyResult = new ArrayList<>(teams);
        faultyResult.sort(faulty);

        List<Team> correctResult = new ArrayList<>(teams);
        correctResult.sort(correct);

        System.out.println("Faulty Ranking:");
        System.out.println(faultyResult);

        System.out.println("\nCorrect Ranking:");
        System.out.println(correctResult);
    }
}
