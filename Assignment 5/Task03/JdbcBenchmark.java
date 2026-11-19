import java.sql.*;

public class JdbcBenchmark {

    public static void verifyMetadata(Connection con) throws SQLException {
        System.out.println("\n--- TASK B3.1: VERIFYING TABLE METADATA & COLUMN TYPES ---");
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bus_pass")) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            System.out.println("Total Columns: " + cols);
            for (int i = 1; i <= cols; i++) {
                System.out.printf("  Col %d: %-12s | SQL Type: %-10s | Currency: %s\n",
                        i, meta.getColumnName(i), meta.getColumnTypeName(i), meta.isCurrency(i));
            }
        }
    }

    public static long runStatementInsert(Connection con, int count) throws SQLException {
        long start = System.nanoTime();
        try (Statement st = con.createStatement()) {
            for (int i = 1; i <= count; i++) {
                String sql = String.format("INSERT INTO bus_pass VALUES ('P%06d', '192311%03d', 'Student_%d', %d, %.2f)",
                        i, i % 500, i, (i % 20) + 1, 4000.0 + (i % 500));
                st.executeUpdate(sql);
            }
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static long runPreparedInsert(Connection con, int count) throws SQLException {
        String sql = "INSERT INTO bus_pass VALUES (?, ?, ?, ?, ?)";
        long start = System.nanoTime();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                ps.setString(1, String.format("P%06d", i));
                ps.setString(2, "192311" + (i % 500));
                ps.setString(3, "Student_" + i);
                ps.setInt(4, (i % 20) + 1);
                ps.setDouble(5, 4000.0 + (i % 500));
                ps.executeUpdate();
            }
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static long runBatchInsert(Connection con, int count) throws SQLException {
        String sql = "INSERT INTO bus_pass VALUES (?, ?, ?, ?, ?)";
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        long start = System.nanoTime();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                ps.setString(1, String.format("P%06d", i));
                ps.setString(2, "192311" + (i % 500));
                ps.setString(3, "Student_" + i);
                ps.setInt(4, (i % 20) + 1);
                ps.setDouble(5, 4000.0 + (i % 500));
                ps.addBatch();
                if (i % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            con.commit();
        } finally {
            con.setAutoCommit(oldAuto);
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("==========================================================================");
        System.out.println("  TASK B3.2: STATEMENT VS PREPAREDSTATEMENT VS BATCH BENCHMARK");
        System.out.println("==========================================================================");

        try (TestDatabaseConnection testDb = new TestDatabaseConnection()) {
            Connection con = testDb.getConnection();
            verifyMetadata(con);

            int[] scales = {1000, 10000, 100000};
            System.out.printf("\n%-12s | %-16s | %-18s | %-16s | %-10s\n",
                    "Record Count", "Statement (ms)", "PreparedStatement", "Batch 1000 (ms)", "Batch Speedup");
            System.out.println("--------------------------------------------------------------------------");

            for (int n : scales) {
                testDb.truncateTable("bus_pass");
                long tStmt = runStatementInsert(con, n);

                testDb.truncateTable("bus_pass");
                long tPrep = runPreparedInsert(con, n);

                testDb.truncateTable("bus_pass");
                long tBatch = runBatchInsert(con, n);

                double speedup = (double) tStmt / Math.max(1, tBatch);
                System.out.printf("%-12d | %-16d | %-18d | %-16d | %-10.1fx\n",
                        n, tStmt, tPrep, tBatch, speedup);
            }

            System.out.println("\n--- Engineering Trade-Off Analysis (Task B3.4) ---");
            System.out.println("(a) Nightly Import of 100,000 passes:");
            System.out.println("    Use PreparedStatement with addBatch()/executeBatch() inside a single transaction.");
            System.out.println("    Minimizes network round-trips, bypasses repeated SQL parsing, and guarantees ACID atomicity.");
            System.out.println("(b) Single Pass Issued at the Counter:");
            System.out.println("    Use PreparedStatement with auto-commit = true.");
            System.out.println("    Immediate disk persistence; low latency for a single row without holding open transaction locks.");
            System.out.println("==========================================================================");
        }
    }
}
