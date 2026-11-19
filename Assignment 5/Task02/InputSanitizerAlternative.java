import java.util.regex.Pattern;

/**
 * Task B2 Alternative: Input Sanitization via Quote Escaping & Regex Validation
 * Compares security robustness and computational time complexity against PreparedStatement.
 */
public class InputSanitizerAlternative {

    // Regex allowing alphanumeric and underscores, length 3 to 30
    private static final Pattern SAFE_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    /**
     * Alternative 1: Escaping single quotes by doubling them (' -> '')
     */
    public static String escapeQuotes(String input) {
        if (input == null) return "''";
        return "'" + input.replace("'", "''") + "'";
    }

    /**
     * Alternative 2: Regex validation
     */
    public static boolean validateRegex(String input) {
        if (input == null) return false;
        return SAFE_PATTERN.matcher(input).matches();
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  TASK B2 ALTERNATIVE: ESCAPING & REGEX VALIDATION VS PREPAREDSTATEMENT");
        System.out.println("==========================================================================");

        String maliciousInput = "admin' OR '1'='1";
        String backslashAttack = "admin\\' OR 1=1 --";

        System.out.println("\n--- Testing Quote Escaping Alternative ---");
        String escaped = escapeQuotes(maliciousInput);
        System.out.println("Input:   " + maliciousInput);
        System.out.println("Escaped: " + escaped);
        System.out.println("Weakness: Dialect differences (e.g. MySQL NO_BACKSLASH_ESCAPES flag) can bypass simple replace().");

        System.out.println("\n--- Testing Regex Validation Alternative ---");
        boolean valid1 = validateRegex("valid_user123");
        boolean valid2 = validateRegex(maliciousInput);
        System.out.printf("Validation ('valid_user123'): %s\n", valid1 ? "ACCEPTED" : "REJECTED");
        System.out.printf("Validation ('%s'): %s\n", maliciousInput, valid2 ? "ACCEPTED" : "REJECTED");
        System.out.println("Weakness: Overly restrictive regex breaks valid names (e.g. O'Connor, Jean-Luc, Mary-Jane).");

        // Performance / Overhead benchmark
        System.out.println("\n--- Performance / Time Overhead Benchmark (1,000,000 iterations) ---");
        int iterations = 1_000_000;

        // 1. Regex validation time
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            validateRegex("student_user_test");
        }
        long regexTimeMs = (System.nanoTime() - start) / 1_000_000;

        // 2. Quote escaping time
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            escapeQuotes("student_user_test");
        }
        long escapeTimeMs = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("Regex Validation: %d ms (avg %.2f ns/op) [O(m) pattern matching]\n",
                regexTimeMs, ((double) regexTimeMs * 1_000_000) / iterations);
        System.out.printf("Quote Escaping:   %d ms (avg %.2f ns/op) [O(m) string copy & heap alloc]\n",
                escapeTimeMs, ((double) escapeTimeMs * 1_000_000) / iterations);
        System.out.println("PreparedStatement: O(1) binary parameter binding with zero client regex parsing or string allocations.");
        System.out.println("==========================================================================");
    }
}
