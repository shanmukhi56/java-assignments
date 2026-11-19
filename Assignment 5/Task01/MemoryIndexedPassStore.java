import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Task B1 Alternative: In-Memory Index using HashMap<String, String[]>
 * Time complexity: O(n) startup load, O(1) per search -> Total for q searches: O(n + q)
 * Contrast with File scan: O(q * n)
 */
public class MemoryIndexedPassStore {
    private final Path file;
    private final Map<String, String[]> index = new HashMap<>();

    public MemoryIndexedPassStore(Path file) throws IOException {
        this.file = file;
        loadIndex();
    }

    private void loadIndex() throws IOException {
        if (!Files.exists(file)) return;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length == 5) {
                    index.put(parts[0].trim(), parts);
                }
            }
        }
    }

    public synchronized void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        String[] row = new String[]{passId, regNo, name, String.valueOf(route), String.valueOf(amount)};
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", row));
            w.newLine();
        }
        index.put(passId, row);
    }

    public String[] find(String passId) {
        return index.get(passId);
    }

    public int size() {
        return index.size();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== TASK B1 ALTERNATIVE: HASHMAP INDEXED PASS STORE ===");
        Path temp = Files.createTempFile("indexed_passes", ".csv");
        MemoryIndexedPassStore store = new MemoryIndexedPassStore(temp);

        store.add("P1001", "192311001", "Arun", 12, 4500.0);
        store.add("P1002", "192311002", "Bala", 12, 4500.0);
        store.add("P1003", "192311003", "Divya", 7, 3800.0);

        long start = System.nanoTime();
        int queries = 100_000;
        for (int i = 0; i < queries; i++) {
            store.find("P1003");
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("Executed %d in-memory lookups in %d ms (avg %.2f ns/lookup)\n",
                queries, durationMs, ((double) durationMs * 1_000_000) / queries);
        System.out.println("Result for P1003: " + java.util.Arrays.toString(store.find("P1003")));
        Files.deleteIfExists(temp);
    }
}
