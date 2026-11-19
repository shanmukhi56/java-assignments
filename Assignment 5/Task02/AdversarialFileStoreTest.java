import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Task B2: Adversarial Bus-Pass File Stress Test
 * Constructs damaged and malformed file entries to test PassStore resilience.
 */
public class AdversarialFileStoreTest {

    public static class RobustPassStore {
        private final Path file;
        private final List<String> errorLog = new ArrayList<>();

        public RobustPassStore(Path file) {
            this.file = file;
        }

        public String[] find(String passId) throws IOException {
            if (!Files.exists(file)) {
                errorLog.add("File not found: " + file);
                return null;
            }
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        errorLog.add("Line " + lineNum + ": Ignored empty/blank line");
                        continue;
                    }

                    // Handle comma delimiter safely
                    String[] parts = line.split(",", -1);
                    if (parts.length != 5) {
                        errorLog.add("Line " + lineNum + ": Rejected invalid field count (" + parts.length + " fields) -> " + line);
                        continue;
                    }

                    // Validate route format
                    try {
                        Integer.parseInt(parts[3].trim());
                    } catch (NumberFormatException e) {
                        errorLog.add("Line " + lineNum + ": Rejected non-numeric route '" + parts[3] + "' -> " + line);
                        continue;
                    }

                    // Validate amount format
                    try {
                        double amt = Double.parseDouble(parts[4].trim());
                        if (amt < 0) {
                            errorLog.add("Line " + lineNum + ": Rejected negative amount '" + amt + "' -> " + line);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        errorLog.add("Line " + lineNum + ": Rejected non-numeric amount '" + parts[4] + "' -> " + line);
                        continue;
                    }

                    if (parts[0].trim().equals(passId)) {
                        return parts;
                    }
                }
            }
            return null;
        }

        public List<String> getErrorLog() {
            return errorLog;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("==========================================================================");
        System.out.println("  TASK B2: ADVERSARIAL BUS-PASS FILE STRESS TEST");
        System.out.println("==========================================================================");

        Path corruptFile = Files.createTempFile("adversarial_passes_", ".csv");
        try (BufferedWriter w = Files.newBufferedWriter(corruptFile)) {
            // Normal record 1
            w.write("P1001,192311001,Arun Kumar,12,4500.0\n");

            // Adversarial Case 1: Wrong number of fields (3 fields)
            w.write("P9001,192311050,IncompleteLine\n");

            // Adversarial Case 2: Non-numeric amount
            w.write("P9002,192311051,Charlie Brown,12,FREE_RIDE\n");

            // Adversarial Case 3: Negative amount
            w.write("P9003,192311052,Bad Actor,8,-500.0\n");

            // Adversarial Case 4: Non-numeric route
            w.write("P9004,192311053,David Miller,EXPRESS_ROUTE,3500.0\n");

            // Adversarial Case 5: Empty and whitespace-only lines
            w.write("\n");
            w.write("         \n");

            // Adversarial Case 6: Delimiter collision (comma in unquoted name creates 6 fields)
            w.write("P9005,192311054,Smith, John,14,4000.0\n");

            // Normal record 2 placed after all corrupted lines
            w.write("P1003,192311003,Divya Murugan,7,3800.0\n");
        }

        RobustPassStore store = new RobustPassStore(corruptFile);

        System.out.println("\n--- Searching for Normal Pass (P1003) following corrupted entries ---");
        String[] found = store.find("P1003");
        System.out.println("Result: " + Arrays.toString(found));
        System.out.println("Search Integrity: " + (found != null && found[0].equals("P1003") ? "MAINTAINED (PASS)" : "BROKEN (FAIL)"));

        System.out.println("\n--- Audit Log of Adversarial Inputs Intercepted ---");
        for (String err : store.getErrorLog()) {
            System.out.println("  [SHIELDED]: " + err);
        }

        System.out.println("\n--- Architectural Explanation ---");
        System.out.println("1. Wrong field count reveals vulnerability to buffer overruns / index out of bounds.");
        System.out.println("2. Non-numeric amounts reveal unhandled NumberFormatException terminating execution.");
        System.out.println("3. Unquoted commas in names split names into separate fields, breaking alignment.");
        System.out.println("4. Robust design skips corrupted records and maintains audit logs rather than failing silently.");
        System.out.println("==========================================================================");

        Files.deleteIfExists(corruptFile);
    }
}
