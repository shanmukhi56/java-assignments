import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PassLoader {
    /** Loads all passes in one transaction, in batches of 1,000 rows.
     * Returns the number of rows loaded. */
    public static int loadAll(Connection con, List<String[]> passes)
            throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) "
                + "VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            for (String[] p : passes) {
                // TODO 1: Set the five parameters from p (route as int, amount as
                // double), add the row to the batch, and execute the batch after every
                // 1,000 rows
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3].trim()));
                ps.setDouble(5, Double.parseDouble(p[4].trim()));
                ps.addBatch();
                n++;
                if (n % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            // TODO 2: Make all inserted rows permanent and return the number of rows
            // loaded
            con.commit();
            return n;
        } catch (SQLException | RuntimeException e) {
            // TODO 3: Undo every insert made in this transaction, then re-throw the
            // exception
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK B3: PASS LOADER BATCH & TRANSACTION TEST ===");
        try (TestDatabaseConnection testDb = new TestDatabaseConnection()) {
            Connection con = testDb.getConnection();

            // Test 1: Successful load of 2,500 passes
            System.out.println("\n[Test 1: Normal Batch Load (2,500 rows)]");
            List<String[]> passes = new ArrayList<>();
            for (int i = 1; i <= 2500; i++) {
                passes.add(new String[]{String.format("P%06d", i), "192311" + (i % 500), "Student_" + i, String.valueOf((i % 20) + 1), "4200.0"});
            }
            int loaded = loadAll(con, passes);
            System.out.println("Successfully committed rows: " + loaded);
            System.out.println("Table row count: " + testDb.getRowCount("bus_pass"));

            // Test 2: Forced failure half-way through
            System.out.println("\n[Test 2: Forced Failure Half-Way with Rollback]");
            testDb.truncateTable("bus_pass");
            List<String[]> failingPasses = new ArrayList<>();
            for (int i = 1; i <= 1500; i++) {
                if (i == 750) {
                    // Invalid non-numeric amount halfway
                    failingPasses.add(new String[]{"P_FAIL", "192311999", "Corrupt Student", "12", "INVALID_AMOUNT"});
                } else {
                    failingPasses.add(new String[]{String.format("P_ERR_%04d", i), "192311001", "Student", "10", "4000.0"});
                }
            }

            try {
                loadAll(con, failingPasses);
                System.out.println("FAIL: Exception expected but did not occur.");
            } catch (Exception e) {
                System.out.println("CAUGHT EXPECTED EXCEPTION: " + e.getMessage());
            }

            int rowsAfterFailure = testDb.getRowCount("bus_pass");
            System.out.println("Rows remaining in table after rollback: " + rowsAfterFailure);
            if (rowsAfterFailure == 0) {
                System.out.println("VERIFICATION: Transaction rollback succeeded: 0 orphaned rows remain!");
            } else {
                System.out.println("FAIL: Partial rows remained in table!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
