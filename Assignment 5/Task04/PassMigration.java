import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Task B4.1: Practical Migration Program
 * Reads existing pass CSV, validates fields, batch-inserts into database within a single transaction,
 * and generates an audit report for any invalid records.
 */
public class PassMigration {

    public static class MigrationReport {
        public int totalRead = 0;
        public int successfullyLoaded = 0;
        public List<String> validationFailures = new ArrayList<>();
    }

    public static MigrationReport migrate(Path csvFile, Connection con) throws IOException, SQLException {
        MigrationReport report = new MigrationReport();
        if (!Files.exists(csvFile)) {
            report.validationFailures.add("Migration file not found: " + csvFile);
            return report;
        }

        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false); // Begin Transaction

        try (BufferedReader reader = Files.newBufferedReader(csvFile);
             PreparedStatement ps = con.prepareStatement(sql)) {

            String line;
            int lineNum = 0;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                report.totalRead++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    report.validationFailures.add("Line " + lineNum + ": Blank line skipped");
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length != 5) {
                    report.validationFailures.add("Line " + lineNum + ": Invalid field count (" + parts.length + ") -> " + line);
                    continue;
                }

                int route;
                try {
                    route = Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException e) {
                    report.validationFailures.add("Line " + lineNum + ": Malformed route number '" + parts[3] + "'");
                    continue;
                }

                double amount;
                try {
                    amount = Double.parseDouble(parts[4].trim());
                    if (amount <= 0) {
                        report.validationFailures.add("Line " + lineNum + ": Non-positive amount " + amount);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    report.validationFailures.add("Line " + lineNum + ": Malformed amount '" + parts[4] + "'");
                    continue;
                }

                // Add to prepared batch
                ps.setString(1, parts[0].trim());
                ps.setString(2, parts[1].trim());
                ps.setString(3, parts[2].trim());
                ps.setInt(4, route);
                ps.setDouble(5, amount);
                ps.addBatch();

                batchCount++;
                report.successfullyLoaded++;

                if (batchCount % 1000 == 0) {
                    ps.executeBatch();
                }
            }

            if (batchCount % 1000 != 0) {
                ps.executeBatch();
            }

            con.commit(); // Commit all valid records
            return report;

        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== TASK B4.1: BUS-PASS CSV TO DATABASE MIGRATION ===");
        Path sampleCsv = Files.createTempFile("migration_data_", ".csv");

        // Prepare test data with valid rows and deliberate corruptions
        List<String> lines = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            lines.add(String.format("P%06d,192311%03d,Student_%d,%d,4500.0", i, i % 500, i, (i % 20) + 1));
        }
        lines.add("P_ERR_1,192311901,DamagedLineWithoutEnoughFields");
        lines.add("P_ERR_2,192311902,Bad Route,INVALID_ROUTE,4500.0");
        lines.add("P_ERR_3,192311903,Negative Amount,12,-3500.0");
        lines.add("");
        Files.write(sampleCsv, lines);

        try (Task04TestDb testDb = new Task04TestDb()) {
            MigrationReport report = migrate(sampleCsv, testDb.getConnection());

            System.out.println("Total Lines Scanned:      " + report.totalRead);
            System.out.println("Successfully Migrated:    " + report.successfullyLoaded);
            System.out.println("Validation Failures:      " + report.validationFailures.size());

            System.out.println("\n--- Audit Report of Failed Records ---");
            for (String err : report.validationFailures) {
                System.out.println("  [VALIDATION REPORT]: " + err);
            }
        }

        Files.deleteIfExists(sampleCsv);
    }
}
