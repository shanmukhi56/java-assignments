import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class ResultProcessor {
    static class ResultTask implements Callable<String> {
        private final Path file;

        ResultTask(Path file) {
            this.file = file;
        }

        public String call() throws Exception {
            if (!Files.exists(file)) {
                throw new IOException("File is missing: " + file.getFileName());
            }
            String text = Files.readString(file).trim();
            if (text.isEmpty()) {
                throw new IOException("File is empty: " + file.getFileName());
            }
            int score = Integer.parseInt(text);
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("Invalid score in " + file.getFileName());
            }
            return file.getFileName() + ": " + score;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Path> files = Arrays.asList(
            Paths.get("scores/student1.txt"),
            Paths.get("scores/student2.txt"),
            Paths.get("scores/missing.txt"),
            Paths.get("scores/student3.txt"),
            Paths.get("scores/corrupt.txt")
        );

        ExecutorService pool = Executors.newFixedThreadPool(4);
        ConcurrentLinkedQueue<String> successes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();

        List<Future<String>> futures = new ArrayList<>();
        for (Path file : files) {
            futures.add(pool.submit(new ResultTask(file)));
        }

        for (Future<String> future : futures) {
            try {
                successes.add(future.get());
            } catch (ExecutionException e) {
                failures.add(e.getCause().getMessage());
            }
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Successful files: " + successes.size());
        for (String result : successes) System.out.println("SUCCESS " + result);
        System.out.println("Failed files: " + failures.size());
        for (String result : failures) System.out.println("FAILED " + result);
    }
}
