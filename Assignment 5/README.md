# CSA09 Assignment 5 - Data Persistence Engineering (Files and Databases)

SIMATS Engineering | Programming in Java (CSA09) | CO5 | 50 Marks

**Student Name:** Anusha  
**Register Number:** 192324316L 
**Repository:** https://github.com/Anusha2321/CSA0906-Assignment---5.git  
**Verification Environment:** OpenJDK 21.0.10 (64-Bit Server VM, Windows 11 Enterprise x64)

---

## 1. Quick Start Guide (Windows / Linux)
1. Ensure JDK 21 is installed and `javac` / `java` are available in your system `PATH`.
2. To compile all tasks simultaneously:
   - **Windows:** Double-click `compile_all.bat` or run `.\compile_all.bat` in Command Prompt / PowerShell.
   - **Linux / macOS:** Run `chmod +x compile_all.sh && ./compile_all.sh`.
3. To run tasks interactively:
   - Double-click `run_task.bat` and select any task from 1 to 13.
4. Review `Assignment5_Report.pdf` and `Report.md` for complete design specifications, SIMATS algorithmic pseudocode, mathematical complexity analyses, and benchmark interpretations.

---

## 2. Contents and Execution Commands

Run commands from the repository root:

| Task Folder | Program Name | Command | Purpose & Result |
|---|---|---|---|
| **Task01** | `PassStore.java` | `java -cp Task01 PassStore` | Completed TODO 1: Searches CSV line-by-line using `BufferedReader`, skips damaged entries. Outputs `[P1003, ...]` and `null`. |
| **Task01** | `PassStoreValidationTest.java` | `java -cp Task01 PassStoreValidationTest` | Task B1.1: Comprehensive validation of add, find, damaged lines skip, and list by route. |
| **Task01** | `LineCounterBenchmark.java` | `java -cp Task01 LineCounterBenchmark` | Task B1.3 & A2: Measures unbuffered `FileInputStream.read()` vs `BufferedInputStream` on 1MB, 10MB, 50MB logs (measured up to 2,391x speedup). |
| **Task01** | `FileGrowthBenchmark.java` | `java -cp Task01 FileGrowthBenchmark` | Task B1.4: Measures search time scaling from 1,000 to 100,000 records, verifying linear O(n) scan trend. |
| **Task01** | `MemoryIndexedPassStore.java` | `java -cp Task01 MemoryIndexedPassStore` | Task B1 Alternative: In-memory `HashMap<String, String[]>` indexing achieving 50 ns lookups (O(n+q)). |
| **Task01** | `RandomAccessPassStore.java` | `java -cp Task01 RandomAccessPassStore` | Task A1 Alternative: Fixed-record `RandomAccessFile` direct O(1) seek; calculates 40.6% padding fragmentation. |
| **Task02** | `AdversarialLoginTest.java` | `java -cp Task02 AdversarialLoginTest` | Task B2 & A5.2: Tautology and comment SQL injection bypass on dynamic SQL vs rejection on `PreparedStatement`. |
| **Task02** | `AdversarialFileStoreTest.java` | `java -cp Task02 AdversarialFileStoreTest` | Task B2: Stress tests corrupted bus-pass lines (wrong field count, non-numeric amount/route, empty lines). |
| **Task02** | `InputSanitizerAlternative.java` | `java -cp Task02 InputSanitizerAlternative` | Task B2 Alternative: Compares quote escaping and regex pattern matching vs `PreparedStatement`. |
| **Task03** | `PassLoader.java` | `java -cp Task03 PassLoader` | Completed TODO 1, 2, 3: Batch insert in 1,000-row chunks; rollback on halfway invalid amount (0 rows remain). |
| **Task03** | `JdbcBenchmark.java` | `java -cp Task03 JdbcBenchmark` | Task B3.1 & B3.2: Statement vs PreparedStatement vs Batch updates across 1k, 10k, 100k records; metadata verification. |
| **Task03** | `TransactionSafetyTest.java` | `java -cp Task03 TransactionSafetyTest` | Task B3.3 & A5.1: Auto-commit dirty data (5 rows) vs transaction rollback (0 rows); Hostel to Canteen ₹500 transfer. |
| **Task04** | `PassMigration.java` | `java -cp Task04 PassMigration` | Task B4.1: CSV to Database migration in batches/transaction with validation failure report. |
| **Task04** | `DatabaseInspector.java` | `java -cp Task04 DatabaseInspector` | Task B4.2: Inspects server metadata, driver version, batch support, and active table catalogues. |
| **Task04** | `GenericDaoDemo.java` | `java -cp Task04 GenericDaoDemo` | Task A3: Generic `Dao<T, K>`, `StudentDao`, and object-table mapping. |
| **Task04** | `HostelSerializationDemo.java` | `java -cp Task04 HostelSerializationDemo` | Task A4: `Hostel`->`Room`->`Resident` graph serialization; transient nullification and `serialVersionUID`. |
| **Task04** | `MultiCampusComparison.java` | `java -cp Task04 MultiCampusComparison` | Task B4.3: 12-campus evaluation matrix selecting RDBMS for 60 concurrent counters and ACID safety. |

---

## 3. Database Setup (MySQL Production Mode)
The assignment includes a zero-dependency embedded proxy driver that enables immediate out-of-the-box execution and testing. To execute against a real production MySQL server:
1. Start MySQL Server and run `Task03/schema.sql`:
   ```bash
   mysql -u root -p < Task03/schema.sql
   ```
2. Place the official `mysql-connector-j-8.x.x.jar` in a `lib/` directory.
3. Configure environment variables:
   - **Windows:**
     ```cmd
     set DB_URL=jdbc:mysql://localhost:3306/campus_transport
     set DB_USER=root
     set DB_PASSWORD=your_password
     ```
   - **Linux / macOS:**
     ```bash
     export DB_URL=jdbc:mysql://localhost:3306/campus_transport
     export DB_USER=root
     export DB_PASSWORD=your_password
     ```

---

## 4. Verification Evidence Included
- `compile_results.txt`: All source groups compiled successfully with 0 errors on OpenJDK 21.0.10.
- `Task01/test_results.txt` & `Task01/benchmark_results.txt`: Actual measured I/O and growth timings.
- `Task02/test_results.txt`: Real injection bypass logs and file shield audit records.
- `Task03/test_results.txt` & `Task03/benchmark_results.txt`: Measured batch throughput and rollback verification.
- `Task04/test_results.txt`: Migration validation audit and DatabaseMetaData output.
- `TaskXX/screenshots/*.png`: Rendered high-resolution terminal output images for every task.
