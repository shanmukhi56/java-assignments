import java.sql.*;

/**
 * Task B4.2: Database Inspection via DatabaseMetaData
 * Queries server capabilities, driver versions, and active table catalogues.
 */
public class DatabaseInspector {

    public static void inspect(Connection con) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();

        System.out.println("==========================================================================");
        System.out.println("  TASK B4.2: DATABASE ENVIRONMENT & METADATA INSPECTION");
        System.out.println("==========================================================================");
        System.out.println("Database Product Name:    " + meta.getDatabaseProductName());
        System.out.println("Database Product Version: " + meta.getDatabaseProductVersion());
        System.out.println("Driver Name:              " + meta.getDriverName());
        System.out.println("Driver Version:           " + meta.getDriverVersion());
        System.out.println("Supports Batch Updates:   " + meta.supportsBatchUpdates());
        System.out.println("Supports Transactions:    " + meta.supportsTransactions());
        System.out.println("Default Isolation Level:  " + meta.getDefaultTransactionIsolation());

        System.out.println("\n--- Tables Present in Catalogue ---");
        try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                System.out.printf("  Table Name: %-20s | Table Type: %s\n",
                        tables.getString("TABLE_NAME"), tables.getString("TABLE_TYPE"));
            }
        }

        System.out.println("\n--- Engineering Importance When Moving Servers ---");
        System.out.println("1. Dialect & Feature Detection: Identifies SQL syntax variations (e.g. MySQL vs Oracle vs PostgreSQL).");
        System.out.println("2. Driver Compatibility: Verifies whether the installed JDBC driver supports modern batch/connection pooling.");
        System.out.println("3. Schema Verification: Validates that tables and constraints exist before runtime queries execute.");
        System.out.println("4. Environment Integrity: Prevents runtime crashing when moving between staging and production environments.");
        System.out.println("==========================================================================");
    }

    public static void main(String[] args) throws SQLException {
        try (Task04TestDb testDb = new Task04TestDb()) {
            inspect(testDb.getConnection());
        }
    }
}
