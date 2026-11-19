# Task 03 Screenshots & Verification Evidence

Save actual execution screenshots here using the corresponding test ID:
- `T3-01_PassLoader_Output.png`: Terminal output of `PassLoader` showing normal load (2500 rows) and 0 rows remaining after halfway failure rollback.
- `T3-02_MetadataAndBenchmark.png`: Terminal output of `JdbcBenchmark` displaying `ResultSetMetaData` column types and Statement vs PreparedStatement vs Batch timing across 1k, 10k, 100k records.
- `T3-03_TransactionSafety.png`: Terminal output of `TransactionSafetyTest` showing auto-commit failure state (5 dirty rows) versus transaction rollback (0 rows) and the wallet transfer output.
