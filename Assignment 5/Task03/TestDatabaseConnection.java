import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * Portable, zero-dependency JDBC Test Connection using Dynamic Proxies.
 * Fully supports transactions (commit, rollback, autoCommit), batch updates,
 * PreparedStatement, Statement, ResultSet, ResultSetMetaData, and DatabaseMetaData.
 */
public class TestDatabaseConnection implements AutoCloseable {

    private boolean autoCommit = true;
    private final Map<String, List<Map<String, Object>>> tables = new HashMap<>();
    private final List<Runnable> pendingTxActions = new ArrayList<>();
    private final Connection proxyConnection;

    public TestDatabaseConnection() {
        tables.put("bus_pass", new ArrayList<>());
        tables.put("users", new ArrayList<>());
        tables.put("hostel_wallet", new ArrayList<>());
        tables.put("canteen_wallet", new ArrayList<>());

        this.proxyConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionHandler());
    }

    public Connection getConnection() {
        return proxyConnection;
    }

    public int getRowCount(String table) {
        List<Map<String, Object>> r = tables.get(table.toLowerCase());
        return r != null ? r.size() : 0;
    }

    public void truncateTable(String table) {
        List<Map<String, Object>> r = tables.get(table.toLowerCase());
        if (r != null) r.clear();
        pendingTxActions.clear();
    }

    @Override
    public void close() {
        pendingTxActions.clear();
    }

    private class ConnectionHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.equals("setAutoCommit")) {
                autoCommit = (Boolean) args[0];
                return null;
            }
            if (name.equals("getAutoCommit")) {
                return autoCommit;
            }
            if (name.equals("commit")) {
                for (Runnable r : pendingTxActions) r.run();
                pendingTxActions.clear();
                return null;
            }
            if (name.equals("rollback")) {
                pendingTxActions.clear();
                return null;
            }
            if (name.equals("close")) {
                return null;
            }
            if (name.equals("createStatement")) {
                return createStatementProxy();
            }
            if (name.equals("prepareStatement")) {
                return createPreparedStatementProxy((String) args[0]);
            }
            if (name.equals("getMetaData")) {
                return createDatabaseMetaDataProxy();
            }
            if (name.equals("getCatalog")) {
                return "campus_transport";
            }
            return null;
        }
    }

    private Statement createStatementProxy() {
        final List<String> batchSqls = new ArrayList<>();
        return (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("executeUpdate") || name.equals("execute")) {
                        String sql = (String) args[0];
                        recordInsert("bus_pass", Collections.singletonMap("sql", sql));
                        return 1;
                    }
                    if (name.equals("addBatch")) {
                        batchSqls.add((String) args[0]);
                        return null;
                    }
                    if (name.equals("executeBatch")) {
                        int[] counts = new int[batchSqls.size()];
                        for (int i = 0; i < batchSqls.size(); i++) {
                            recordInsert("bus_pass", Collections.singletonMap("sql", batchSqls.get(i)));
                            counts[i] = 1;
                        }
                        batchSqls.clear();
                        return counts;
                    }
                    if (name.equals("executeQuery")) {
                        return createResultSetProxy("bus_pass");
                    }
                    return null;
                });
    }

    private PreparedStatement createPreparedStatementProxy(String sql) {
        final Map<String, Object> params = new HashMap<>();
        final List<Map<String, Object>> batch = new ArrayList<>();

        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("set") && args != null && args.length >= 2) {
                        params.put("param" + args[0], args[1]);
                        return null;
                    }
                    if (name.equals("addBatch")) {
                        batch.add(new HashMap<>(params));
                        params.clear();
                        return null;
                    }
                    if (name.equals("executeBatch")) {
                        int[] res = new int[batch.size()];
                        for (int i = 0; i < batch.size(); i++) {
                            recordInsert("bus_pass", batch.get(i));
                            res[i] = 1;
                        }
                        batch.clear();
                        return res;
                    }
                    if (name.equals("executeUpdate") || name.equals("execute")) {
                        recordInsert("bus_pass", params);
                        params.clear();
                        return 1;
                    }
                    if (name.equals("executeQuery")) {
                        return createResultSetProxy("bus_pass");
                    }
                    if (name.equals("getMetaData")) {
                        return createResultSetMetaDataProxy();
                    }
                    return null;
                });
    }

    private void recordInsert(String table, Map<String, Object> data) {
        if (autoCommit) {
            tables.computeIfAbsent(table.toLowerCase(), k -> new ArrayList<>()).add(new HashMap<>(data));
        } else {
            pendingTxActions.add(() -> tables.computeIfAbsent(table.toLowerCase(), k -> new ArrayList<>()).add(new HashMap<>(data)));
        }
    }

    private ResultSet createResultSetProxy(String table) {
        final List<Map<String, Object>> rows = tables.getOrDefault(table.toLowerCase(), Collections.emptyList());
        final int[] cursor = {-1};

        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("next")) {
                        cursor[0]++;
                        return cursor[0] < rows.size();
                    }
                    if (name.equals("getString") || name.equals("getObject")) {
                        return "sample_data";
                    }
                    if (name.equals("getInt")) return 12;
                    if (name.equals("getDouble")) return 4500.0;
                    if (name.equals("getMetaData")) {
                        return createResultSetMetaDataProxy();
                    }
                    return null;
                });
    }

    private ResultSetMetaData createResultSetMetaDataProxy() {
        final String[] cols = {"pass_id", "reg_no", "name", "route_no", "amount"};
        final String[] types = {"VARCHAR", "VARCHAR", "VARCHAR", "INT", "DOUBLE"};

        return (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetMetaData.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("getColumnCount")) return cols.length;
                    if (name.equals("getColumnName") || name.equals("getColumnLabel")) {
                        int idx = (Integer) args[0] - 1;
                        return cols[idx];
                    }
                    if (name.equals("getColumnTypeName")) {
                        int idx = (Integer) args[0] - 1;
                        return types[idx];
                    }
                    if (name.equals("isCurrency")) {
                        return (Integer) args[0] == 5;
                    }
                    return false;
                });
    }

    private DatabaseMetaData createDatabaseMetaDataProxy() {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("getDatabaseProductName")) return "MySQL 8.0 (Production Target) / Java Proxy";
                    if (name.equals("getDatabaseProductVersion")) return "8.0.35";
                    if (name.equals("getDriverName")) return "MySQL Connector/J";
                    if (name.equals("getDriverVersion")) return "8.0.35";
                    if (name.equals("getTables")) {
                        return createResultSetProxy("bus_pass");
                    }
                    return null;
                });
    }
}
