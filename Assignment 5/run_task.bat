@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   CSA09 ASSIGNMENT 5 - DATA PERSISTENCE RUNNER
echo ========================================================
echo 1 Task 01: PassStore TODO 1 Complete Code
echo 2 Task 01: Validation Test (B1.1)
echo 3 Task 01: Buffering Benchmark (B1.3 - 1MB, 10MB, 50MB)
echo 4 Task 01: Growth Scaling Benchmark (B1.4 - 1k to 100k)
echo 5 Task 02: Adversarial SQL Injection vs PreparedStatement
echo 6 Task 02: Corrupted Bus-Pass File Store Resilience
echo 7 Task 03: PassLoader Batch & Rollback TODO Complete Code
echo 8 Task 03: JDBC Statement vs PreparedStatement vs Batch
echo 9 Task 03: Transaction Safety & Wallet Transfer
echo 10 Task 04: CSV to DB Migration with Validation Report
echo 11 Task 04: DatabaseMetaData System Inspection
echo 12 Task 04: Hostel Object Graph Serialization
echo 13 Task 04: 12-Campus Enterprise Comparison
set /p task=Choose 1-13: 
if "%task%"=="1" java -cp Task01 PassStore
if "%task%"=="2" java -cp Task01 PassStoreValidationTest
if "%task%"=="3" java -cp Task01 LineCounterBenchmark
if "%task%"=="4" java -cp Task01 FileGrowthBenchmark
if "%task%"=="5" java -cp Task02 AdversarialLoginTest
if "%task%"=="6" java -cp Task02 AdversarialFileStoreTest
if "%task%"=="7" java -cp Task03 PassLoader
if "%task%"=="8" java -cp Task03 JdbcBenchmark
if "%task%"=="9" java -cp Task03 TransactionSafetyTest
if "%task%"=="10" java -cp Task04 PassMigration
if "%task%"=="11" java -cp Task04 DatabaseInspector
if "%task%"=="12" java -cp Task04 HostelSerializationDemo
if "%task%"=="13" java -cp Task04 MultiCampusComparison
pause
