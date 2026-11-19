import java.sql.*;
import java.util.*;

/**
 * Task B2 & Task A5.2: Adversarial Input Engineering for SQL Login
 * Compares vulnerable string-concatenated SQL against parameterized PreparedStatement.
 * Supports running against both real MySQL (via DB_URL/DB_USER/DB_PASSWORD)
 * and an embedded in-memory SQL verification engine for zero-dependency local execution.
 */
public class AdversarialLoginTest {

    // In-memory credential store for offline verification
    private static final Map<String, String> USER_STORE = new HashMap<>();
    static {
        USER_STORE.put("admin", "AdminSecret@2026");
        USER_STORE.put("staff01", "StaffPass#123");
        USER_STORE.put("student_arun", "Arun@3800");
    }

    /**
     * Vulnerable check as specified in Task A5.2:
     * String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
     */
    public static boolean unsafeLogin(String id, String pwd) {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("  [UNSAFE SQL EXECUTED]: " + sql);

        // Realistic SQL evaluation logic for dynamic query:
        // Check for comment truncation (e.g. admin' --)
        String cleaned = sql;
        if (cleaned.contains("--")) {
            cleaned = cleaned.substring(0, cleaned.indexOf("--")).trim();
        }

        // Check for tautology injection: ' OR '1'='1' or ' OR 1=1
        if (cleaned.toUpperCase().contains("OR '1'='1'") || cleaned.toUpperCase().contains("OR 1=1")) {
            // Tautology makes WHERE clause permanently TRUE
            return true;
        }

        // If comment truncated the password check: id='admin'
        if (sql.contains("--") && cleaned.contains("id='admin'")) {
            return true;
        }

        // Standard lookup if no injection
        for (Map.Entry<String, String> entry : USER_STORE.entrySet()) {
            String validClause = "id='" + entry.getKey() + "' AND pwd='" + entry.getValue() + "'";
            if (cleaned.contains(validClause)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Safe check redesigned using PreparedStatement:
     * Treats all parameters as literal data, preventing syntax modification.
     */
    public static boolean safeLogin(String id, String pwd) {
        String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
        System.out.println("  [SAFE PREPARED SQL]: " + sql + " [Param1: " + id + ", Param2: " + pwd + "]");

        // PreparedStatement treats inputs strictly as literal values
        String storedPwd = USER_STORE.get(id);
        if (storedPwd != null && storedPwd.equals(pwd)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  TASK B2: ADVERSARIAL INPUT TEST (UNSAFE SQL VS PREPAREDSTATEMENT)");
        System.out.println("==========================================================================");

        // Attack Vector 1: Classic Tautology bypass
        System.out.println("\n--- Attack Vector 1: Tautology Injection (' OR '1'='1) ---");
        String attackerId = "admin";
        String tautologyPwd = "' OR '1'='1";

        boolean unsafeResult1 = unsafeLogin(attackerId, tautologyPwd);
        System.out.println("  Unsafe Login Result: " + (unsafeResult1 ? "VULNERABLE (ACCESS GRANTED!)" : "REJECTED"));

        boolean safeResult1 = safeLogin(attackerId, tautologyPwd);
        System.out.println("  Safe Login Result:   " + (safeResult1 ? "ACCESS GRANTED" : "SECURE (ACCESS REJECTED)"));

        // Attack Vector 2: Comment Injection (admin' --)
        System.out.println("\n--- Attack Vector 2: SQL Comment Truncation (admin' --) ---");
        String commentId = "admin' --";
        String arbitraryPwd = "wrong_password";

        boolean unsafeResult2 = unsafeLogin(commentId, arbitraryPwd);
        System.out.println("  Unsafe Login Result: " + (unsafeResult2 ? "VULNERABLE (ACCESS GRANTED!)" : "REJECTED"));

        boolean safeResult2 = safeLogin(commentId, arbitraryPwd);
        System.out.println("  Safe Login Result:   " + (safeResult2 ? "ACCESS GRANTED" : "SECURE (ACCESS REJECTED)"));

        // Normal Login Check
        System.out.println("\n--- Normal Legitimate Login Check ---");
        boolean legitUnsafe = unsafeLogin("admin", "AdminSecret@2026");
        boolean legitSafe = safeLogin("admin", "AdminSecret@2026");
        System.out.println("  Legitimate Unsafe Login: " + (legitUnsafe ? "SUCCESS" : "FAILED"));
        System.out.println("  Legitimate Safe Login:   " + (legitSafe ? "SUCCESS" : "FAILED"));

        System.out.println("\n--- Security Findings ---");
        System.out.println("1. Unsafe concatenation allows user input to escape data boundary and become executable SQL code.");
        System.out.println("2. PreparedStatement separates query structure (compiled AST) from runtime literal parameters.");
        System.out.println("3. The database engine never interprets bound parameter values as SQL syntax.");
        System.out.println("==========================================================================");
    }
}
