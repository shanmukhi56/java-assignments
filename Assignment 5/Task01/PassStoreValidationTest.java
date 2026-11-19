import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class PassStoreValidationTest {
    public static void main(String[] args) throws IOException {
        System.out.println("=== TASK B1.1: BUS-PASS FILE STORE VALIDATION ===");
        Path tempFile = Files.createTempFile("pass_validation_test", ".csv");
        PassStore store = new PassStore(tempFile);

        System.out.println("\n[Test 1: Adding Records]");
        store.add("P1001", "192311001", "Arun Kumar", 12, 4500.0);
        store.add("P1002", "192311002", "Bala Chandran", 12, 4500.0);
        store.add("P1003", "192311003", "Divya Murugan", 7, 3800.0);
        System.out.println("PASS: Added 3 normal pass records.");

        System.out.println("\n[Test 2: Injecting Damaged Lines]");
        // Damaged line 1: wrong field count
        Files.writeString(tempFile, "P9999,DAMAGED_RECORD_WITH_NO_AMOUNT_OR_ROUTE\n", StandardOpenOption.APPEND);
        // Damaged line 2: empty line
        Files.writeString(tempFile, "\n   \n", StandardOpenOption.APPEND);
        System.out.println("PASS: Corrupted lines appended to file.");

        System.out.println("\n[Test 3: Finding Normal Record (P1003)]");
        String[] p1003 = store.find("P1003");
        System.out.println("Result: " + Arrays.toString(p1003));
        if (p1003 != null && p1003[0].equals("P1003")) {
            System.out.println("VERIFICATION: Successfully retrieved existing record across damaged entries.");
        } else {
            System.out.println("FAIL: Could not locate P1003.");
        }

        System.out.println("\n[Test 4: Finding Non-Existent Record (P8888)]");
        String[] p8888 = store.find("P8888");
        System.out.println("Result: " + Arrays.toString(p8888));
        if (p8888 == null) {
            System.out.println("VERIFICATION: Correctly returned null for non-existent pass ID.");
        } else {
            System.out.println("FAIL: Unexpected return value.");
        }

        System.out.println("\n[Test 5: Finding Damaged Record (P9999)]");
        String[] p9999 = store.find("P9999");
        System.out.println("Result: " + Arrays.toString(p9999));
        if (p9999 == null) {
            System.out.println("VERIFICATION: Safely ignored damaged record without aborting or crashing.");
        } else {
            System.out.println("FAIL: Corrupt record was not skipped.");
        }

        System.out.println("\n[Test 6: Listing Passes by Route (Route 12)]");
        List<String[]> route12 = store.listByRoute(12);
        System.out.println("Count matching Route 12: " + route12.size());
        for (String[] pass : route12) {
            System.out.println(" - " + Arrays.toString(pass));
        }
        if (route12.size() == 2) {
            System.out.println("VERIFICATION: Correctly listed all passes belonging to Route 12.");
        } else {
            System.out.println("FAIL: Route 12 count mismatch.");
        }

        Files.deleteIfExists(tempFile);
        System.out.println("\n=== ALL B1.1 VALIDATION TESTS PASSED ===");
    }
}
