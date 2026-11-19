
import java.util.Random;

public class TriageBenchmark {

    public static void main(String[] args) {

        int[] sizes = {100, 1000, 10000};
        int runs = 5;

        System.out.println("HOSPITAL TRIAGE BENCHMARK");

        for (int size : sizes) {

            long totalAdd = 0;
            long totalRemove = 0;

            System.out.println("\nQueue Size: " + size);

            for (int run = 1; run <= runs; run++) {

                Triage er = new Triage();
                Random random = new Random(42);

                String[] names = new String[size];
                int[] severities = new int[size];

                for (int i = 0; i < size; i++) {
                    names[i] = "Patient" + i;
                    severities[i] = random.nextInt(5) + 1;
                }

                long start = System.nanoTime();

                for (int i = 0; i < size; i++) {
                    er.admit(names[i], severities[i]);
                }

                long addTime = System.nanoTime() - start;

                start = System.nanoTime();

                for (int i = 0; i < size; i++) {
                    er.next();
                }

                long removeTime = System.nanoTime() - start;

                totalAdd += addTime;
                totalRemove += removeTime;

                System.out.println(
                    "Run " + run +
                    " | Add: " + addTime + " ns" +
                    " | Remove: " + removeTime + " ns"
                );
            }

            System.out.println(
                "Average Admission Time: " +
                (totalAdd / runs) + " ns"
            );

            System.out.println(
                "Average Removal Time: " +
                (totalRemove / runs) + " ns"
            );

            System.out.println(
                "Average Time Per Admission: " +
                (totalAdd / (double)(runs * size)) + " ns"
            );

            System.out.println(
                "Average Time Per Removal: " +
                (totalRemove / (double)(runs * size)) + " ns"
            );
        }
    }
}
