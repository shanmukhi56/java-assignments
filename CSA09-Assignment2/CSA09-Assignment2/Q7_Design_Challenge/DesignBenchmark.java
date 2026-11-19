
import java.util.Locale;

public class DesignBenchmark {

    private static volatile long checksum;

    public static void main(String[] args) {

        int[] ticketCounts = {100, 1000, 10000};
        int[] fieldCounts = {10, 50, 100};
        int runs = 5;

        RedesignedSolution redesigned =
            new RedesignedSolution();

        System.out.println(
            "Tickets,Fields,Run,Original_ns,Redesigned_ns,Speedup"
        );

        for (int w = 0; w < ticketCounts.length; w++) {

            int tickets = ticketCounts[w];
            int fields = fieldCounts[w];

            String[] data = new String[fields];

            for (int i = 0; i < fields; i++) {
                data[i] = "Field " + i + ": Railway Details";
            }

            // Verify both solutions produce the same output
            if (!OriginalSolution.generateTicket(data)
                    .equals(redesigned.generateTicket(data))) {
                throw new AssertionError("Output mismatch");
            }

            // JVM warm-up
            for (int i = 0; i < 2000; i++) {
                checksum += OriginalSolution
                    .generateTicket(data).length();

                checksum += redesigned
                    .generateTicket(data).length();
            }

            long originalTotal = 0;
            long redesignedTotal = 0;

            for (int run = 1; run <= runs; run++) {

                long originalTime;
                long redesignedTime;

                // Alternate execution order
                if (run % 2 == 1) {
                    originalTime =
                        measureOriginal(data, tickets);

                    redesignedTime =
                        measureRedesigned(
                            redesigned, data, tickets);
                } else {
                    redesignedTime =
                        measureRedesigned(
                            redesigned, data, tickets);

                    originalTime =
                        measureOriginal(data, tickets);
                }

                originalTotal += originalTime;
                redesignedTotal += redesignedTime;

                double speedup =
                    (double) originalTime / redesignedTime;

                System.out.printf(
                    Locale.US,
                    "%d,%d,%d,%d,%d,%.2f%n",
                    tickets, fields, run,
                    originalTime, redesignedTime,
                    speedup
                );
            }

            double avgOriginal =
                originalTotal / (double) runs;

            double avgRedesigned =
                redesignedTotal / (double) runs;

            System.out.printf(
                Locale.US,
                "%d,%d,Average,%.2f,%.2f,%.2f%n",
                tickets, fields,
                avgOriginal, avgRedesigned,
                avgOriginal / avgRedesigned
            );
        }
    }

    private static long measureOriginal(
            String[] data, int tickets) {

        long sum = 0;
        long start = System.nanoTime();

        for (int i = 0; i < tickets; i++) {
            sum += OriginalSolution
                .generateTicket(data).length();
        }

        long time = System.nanoTime() - start;
        checksum = sum;

        return time;
    }

    private static long measureRedesigned(
            RedesignedSolution generator,
            String[] data, int tickets) {

        long sum = 0;
        long start = System.nanoTime();

        for (int i = 0; i < tickets; i++) {
            sum += generator
                .generateTicket(data).length();
        }

        long time = System.nanoTime() - start;
        checksum = sum;

        return time;
    }
}
