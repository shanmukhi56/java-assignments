import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * Embedded database proxy for Task 04 verification.
 */
public class Task04TestDb implements AutoCloseable {
    private final Connection proxyConnection;

    public Task04TestDb() {
        this.proxyConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("setAutoCommit") || name.equals("commit") || name.equals("rollback") || name.equals("close")) {
                        return null;
                    }
                    if (name.equals("getAutoCommit")) return true;
                    if (name.equals("prepareStatement")) {
                        return createPreparedStatementProxy();
                    }
                    if (name.equals("getMetaData")) {
                        return createDatabaseMetaDataProxy();
                    }
                    return null;
                });
    }

    public Connection getConnection() { return proxyConnection; }

    private PreparedStatement createPreparedStatementProxy() {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("executeBatch")) return new int[]{1};
                    if (name.equals("executeUpdate")) return 1;
                    return null;
                });
    }

    private DatabaseMetaData createDatabaseMetaDataProxy() {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("getDatabaseProductName")) return "MySQL Server (Enterprise Edition)";
                    if (name.equals("getDatabaseProductVersion")) return "8.0.35-commercial";
                    if (name.equals("getDriverName")) return "MySQL Connector/J";
                    if (name.equals("getDriverVersion")) return "8.0.35";
                    if (name.equals("supportsBatchUpdates")) return true;
                    if (name.equals("supportsTransactions")) return true;
                    if (name.equals("getDefaultTransactionIsolation")) return Connection.TRANSACTION_READ_COMMITTED;
                    if (name.equals("getTables")) {
                        return createTablesResultSet();
                    }
                    return null;
                });
    }

    private ResultSet createTablesResultSet() {
        final String[][] tables = {
                {"bus_pass", "TABLE"},
                {"users", "TABLE"},
                {"hostel_wallet", "TABLE"},
                {"canteen_wallet", "TABLE"}
        };
        final int[] cursor = {-1};

        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("next")) {
                        cursor[0]++;
                        return cursor[0] < tables.length;
                    }
                    if (name.equals("getString")) {
                        String col = (String) args[0];
                        if (col.equalsIgnoreCase("TABLE_NAME")) return tables[cursor[0]][0];
                        if (col.equalsIgnoreCase("TABLE_TYPE")) return tables[cursor[0]][1];
                    }
                    return null;
                });
    }

    @Override
    public void close() {}
}
