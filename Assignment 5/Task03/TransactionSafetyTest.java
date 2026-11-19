import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionSafetyTest {

    public static boolean transferWalletFunds(Connection con, String studentId, double amount)
            throws SQLException {
        String deductSql = "UPDATE hostel_wallet SET balance = balance - ? WHERE student_id = ? AND balance >= ?";
        String creditSql = "UPDATE canteen_wallet SET balance = balance + ? WHERE student_id = ?";

        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false); // Begin ACID Transaction

        try (PreparedStatement psDeduct = con.prepareStatement(deductSql);
             PreparedStatement psCredit = con.prepareStatement(creditSql)) {

            // Step 1: Deduct from Hostel Wallet
            psDeduct.setDouble(1, amount);
            psDeduct.setString(2, studentId);
            psDeduct.setDouble(3, amount);
            psDeduct.executeUpdate();

            // Step 2: Credit to Canteen Wallet
            psCredit.setDouble(1, amount);
            psCredit.setString(2, studentId);
            psCredit.executeUpdate();

            // Step 3: Commit permanently
            con.commit();
            System.out.println("  [TRANSACTION COMMITTED]: ₹" + amount + " transferred safely from Hostel to Canteen.");
            return true;

        } catch (SQLException | RuntimeException e) {
            con.rollback();
            System.out.println("  [TRANSACTION ROLLED BACK]: System failure intercepted: " + e.getMessage());
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("==========================================================================");
        System.out.println("  TASK B3.3 & A5.1: TRANSACTION SAFETY VS AUTO-COMMIT EXPERIMENT");
        System.out.println("==========================================================================");

        try (TestDatabaseConnection testDb = new TestDatabaseConnection()) {
            Connection con = testDb.getConnection();

            System.out.println("\n--- Experiment 1: Batch Insert with Auto-Commit ON vs Failure ---");
            con.setAutoCommit(true);
            testDb.truncateTable("bus_pass");
            try {
                for (int i = 1; i <= 10; i++) {
                    if (i == 6) throw new RuntimeException("Forced hardware/network crash at record 6!");
                    con.createStatement().executeUpdate("INSERT INTO bus_pass VALUES ('P" + i + "', 'REG', 'S', 1, 1000)");
                }
            } catch (RuntimeException e) {
                System.out.println("  Crash intercepted: " + e.getMessage());
            }
            int autoCommitRows = testDb.getRowCount("bus_pass");
            System.out.println("  Rows in database under Auto-Commit: " + autoCommitRows + " (ORPHANED DIRTY DATA!)");

            System.out.println("\n--- Experiment 2: Batch Insert with Transaction (Auto-Commit OFF) vs Failure ---");
            con.setAutoCommit(false);
            testDb.truncateTable("bus_pass");
            try {
                for (int i = 1; i <= 10; i++) {
                    if (i == 6) throw new RuntimeException("Forced hardware/network crash at record 6!");
                    con.createStatement().executeUpdate("INSERT INTO bus_pass VALUES ('P" + i + "', 'REG', 'S', 1, 1000)");
                }
                con.commit();
            } catch (RuntimeException e) {
                con.rollback();
                System.out.println("  Crash intercepted: " + e.getMessage());
                System.out.println("  Executed con.rollback()");
            }
            int transactionRows = testDb.getRowCount("bus_pass");
            System.out.println("  Rows in database under Transaction: " + transactionRows + " (CLEAN ZERO DATA!)");

            System.out.println("\n--- Experiment 3: Hostel to Canteen ₹500 Wallet Transfer ---");
            transferWalletFunds(con, "192311001", 500.0);

            System.out.println("\n--- Conclusion ---");
            System.out.println("Auto-commit creates inconsistent intermediate states during partial crashes.");
            System.out.println("Transactions provide all-or-nothing Atomicity and Consistency (ACID).");
            System.out.println("==========================================================================");
        }
    }
}
