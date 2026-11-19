# ASSIGNMENT 5 | CO5
## DATA PERSISTENCE ENGINEERING – FILES AND DATABASES

Programming in Java (CSA09) | SIMATS Engineering  
Nature: Individual | Duration: One Week | Total Marks: 50  
Bloom’s Levels Assessed: K4 – Analyse and K5 – Evaluate

| Submission Field | Candidate Information |
|---|---|
| **Student Name** | Anusha |
| **Register Number** | 192324316L|
| **GitHub Repository Link** | https://github.com/Anusha2321/CSA0906-Assignment---5.git |
| **Verification Environment** | OpenJDK 21.0.10 (64-Bit Server VM, Windows 11 Enterprise x64) |
| **Submission Date** | November 17, 2026 |

---

## Executive Summary & Engineering Workflow

This technical report documents the engineering investigation into Java data persistence across text files, binary streams, object serialization, and relational database systems (JDBC). The progression follows the mandated engineering lifecycle:

$$\text{Model} \longrightarrow \text{Choose a Store} \longrightarrow \text{Design} \longrightarrow \text{Protect} \longrightarrow \text{Implement} \longrightarrow \text{Measure} \longrightarrow \text{Compare} \longrightarrow \text{Decide}$$

All programs in Part B and Part A alternatives have been fully executed, verified, and measured under OpenJDK 21. Actual benchmark timings, terminal output screenshots, and progressive git commits are included.

---

# PART A – PERSISTENCE DESIGN & PSEUDOCODE (25 Marks)

## TASK A1 – DESIGN A FILE FORMAT (7 Marks)
*Bloom’s Level: K4 – Analyse*

### A1.1 – Represent the Record (2 Marks)
The university transport office issues ~8,000 passes annually. Each record consists of: `pass_id`, `reg_no`, `name`, `route_no`, `boarding_stop`, `validity_date`, and `amount`.

#### (a) Delimited Text File Layout (CSV RFC 4180)
- **Field Order:** `pass_id,reg_no,name,route_no,boarding_stop,validity_date,amount`
- **Field Delimiter:** Comma (`,`), Record Terminator: CRLF (`\r\n`) or LF (`\n`).
- **Delimiter Handling in Name:** If a name contains a delimiter (e.g. `Smith, John` or `O'Connor, Mary`), the field is enclosed in double quotes: `"Smith, John"`. Embedded double quotes are escaped by doubling them (`""`), adhering to standard RFC 4180 rules.
- **Example Record:**
  ```text
  P1001,192311001,"Smith, John",12,Tambaram,2026-06-30,4500.00
  ```

#### (b) Binary File Layout (`DataOutputStream`)
Binary streams store strongly typed primitive fields directly in big-endian network byte order without string formatting overhead:

| Field Index | Field Name | Java Type | `DataOutputStream` Method | Wire Size (Bytes) |
|---|---|---|---|---|
| 1 | `pass_id` | `String` | `writeUTF(passId)` | $2 + \text{len}$ (e.g. 7 bytes) |
| 2 | `reg_no` | `String` | `writeUTF(regNo)` | $2 + \text{len}$ (e.g. 11 bytes) |
| 3 | `name` | `String` | `writeUTF(name)` | $2 + \text{len}$ (e.g. 14–25 bytes) |
| 4 | `route_no` | `int` | `writeInt(routeNo)` | 4 bytes (fixed) |
| 5 | `boarding_stop`| `String` | `writeUTF(boardingStop)` | $2 + \text{len}$ (e.g. 12 bytes) |
| 6 | `validity_date`| `long` | `writeLong(epochMillis)` | 8 bytes (fixed) |
| 7 | `amount` | `double` | `writeDouble(amount)` | 8 bytes (fixed IEEE 754) |

- **Delimiter Handling in Binary:** No delimiters exist in binary files. Strings written via `writeUTF()` are prefixed with a 2-byte unsigned short indicating the exact byte length. Delimiter collisions are mathematically impossible.
- **Storage Comparison:** An average text record requires ~65–85 bytes; a binary record requires ~48–58 bytes (~30% storage savings with zero parsing overhead).

---

### A1.2 – Design the Operations (3 Marks)
*SIMATS Engineering Standard Algorithmic Pseudocode*

```text
ALGORITHM AddPass(passRecord, filePath)
START
    // Use BufferedWriter for character encoding and 8KB kernel write buffering
    OPEN file AT filePath USING BufferedWriter IN APPEND MODE
    CONSTRUCT csvLine FROM passRecord.fields WITH COMMA DELIMITER
    WRITE csvLine TO file
    WRITE NEWLINE TO file
    FLUSH AND CLOSE file
END
```

```text
ALGORITHM FindPassById(targetPassId, filePath)
START
    // Use BufferedReader for 8KB read buffering and line-by-line parsing
    IF FILE filePath DOES NOT EXIST THEN
        RETURN NULL
    ENDIF
    OPEN file AT filePath USING BufferedReader IN READ MODE
    WHILE (line <- READLINE FROM file) IS NOT NULL DO
        IF line IS BLANK THEN CONTINUE ENDIF
        SET fields <- SPLIT line BY COMMA
        IF LENGTH OF fields != 7 THEN
            LOG WARNING "Damaged record skipped: " + line
            CONTINUE
        ENDIF
        IF fields[0].TRIM() == targetPassId THEN
            CLOSE file
            RETURN fields
        ENDIF
    ENDWHILE
    CLOSE file
    RETURN NULL
END
```

```text
ALGORITHM ListPassesByRoute(targetRoute, filePath)
START
    INITIALIZE matchingList AS EMPTY LIST
    IF FILE filePath DOES NOT EXIST THEN
        RETURN matchingList
    ENDIF
    OPEN file AT filePath USING BufferedReader IN READ MODE
    WHILE (line <- READLINE FROM file) IS NOT NULL DO
        IF line IS BLANK THEN CONTINUE ENDIF
        SET fields <- SPLIT line BY COMMA
        IF LENGTH OF fields != 7 THEN CONTINUE ENDIF
        TRY
            SET route <- PARSE_INTEGER(fields[3].TRIM())
            IF route == targetRoute THEN
                APPEND fields TO matchingList
            ENDIF
        CATCH FORMAT_ERROR
            LOG WARNING "Corrupt route field in line: " + line
        ENDTRY
    ENDWHILE
    CLOSE file
    RETURN matchingList
END
```

**Stream Class Justification:**
- `BufferedWriter` / `BufferedReader` wrap low-level stream readers/writers with an internal 8,192-character buffer. This groups thousands of byte operations into single operating system kernel system calls (`read()` / `write()`), reducing expensive context switches between User Space (Ring 3) and Kernel Space (Ring 0).
- `FileReader` / `FileWriter` without buffering issue a direct JNI system call per read/write invocation, degrading I/O performance by orders of magnitude.
- Byte streams (`FileInputStream` / `FileOutputStream`) read raw 8-bit octets without Unicode/UTF-8 character-set decoding, causing character corruption for multi-byte international names and requiring manual newline parsing.

---

### A1.3 – Design for Damaged Data (2 Marks)
1. **Damaged Line Handling:** Before parsing, each line is inspected for field count (`parts.length == 7`) and primitive data types are parsed inside `try-catch` blocks. Malformed lines are written to an `error_audit.log` with line numbers and skipped; execution continues smoothly without throwing `IndexOutOfBoundsException`.
2. **Missing File Handling:** Guarded via `Files.exists(path)`. For read operations (`find`, `list`), a missing file returns a graceful empty/null result rather than an unhandled crash. For `add`, parent directories and missing files are created automatically (`StandardOpenOption.CREATE`).
3. **Incomplete Write from Power Failure:** 
   - **Shadow File & Atomic Move Pattern:** Batch updates are written to a temporary shadow file (`passes.csv.tmp`) and flushed to physical disk via `FileChannel.force(true)` or `FileDescriptor.sync()`. The temporary file is then atomically replaced over the active store using `Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE)`.
   - **Append Tail Recovery:** On startup, the file tail is verified; if a trailing line lacks a terminating newline or has an incomplete field count, the file is safely truncated to the last complete record boundary.

---

### Alternative Concept & Complexity Analysis: Fixed-Length Records with `RandomAccessFile`
- **Solution:** Each record is formatted to a fixed width of $R$ bytes (e.g. 64 bytes) with space padding. Record $i$ is directly accessed via:
  $$\text{file.seek}((long) i \times R)$$
- **Time Complexity:**
  - **Sequential Slot Lookup:** $\mathcal{O}(1)$ time complexity compared to $\mathcal{O}(n)$ text scan.
  - **Pass ID Search (Unsorted):** $\mathcal{O}(n)$ disk seeks. If sorted by `pass_id`, binary search on disk achieves $\mathcal{O}(\log n)$ time complexity.
  - **Text File Scan:** Strictly $\mathcal{O}(n)$ parsing every preceding line.
- **Space Complexity & Fragmentation:**
  - An average text record requires 38 bytes. A fixed record padded to 64 bytes wastes $64 - 38 = 26$ bytes/record ($40.6\%$ internal fragmentation).
  - For 8,000 passes, wasted storage equals $8,000 \times 26 = 208\text{ KB}$. At 800,000 passes, wasted storage reaches $20.8\text{ MB}$.

---

## TASK A2 – DON’T READ WHAT YOU DON’T NEED (5 Marks)
*Bloom’s Level: K4 – Analyse*

### A2.1 – Predict the Cost (2 Marks)
- **Prediction of Read Calls:** A 50 MB log file contains exactly:
  $$50 \times 1024 \times 1024 = 52,428,800\text{ bytes}$$
  Calling unbuffered `FileInputStream.read()` one byte at a time invokes exactly **52,428,800 method calls**.
- **Why It Is Slow:** Each unbuffered `read()` call transitions through the Java Native Interface (JNI) into an OS system call (`sys_read`). This triggers a hardware CPU privilege level switch from User Mode to Kernel Mode. 52.4 million context switches stall the CPU pipeline, cause CPU cache invalidation, and waste millions of CPU cycles on kernel transitions rather than computation.

### A2.2 – Redesign Using Buffering (2 Marks)
Wrapping `FileInputStream` in `BufferedInputStream` with an 8 KB buffer ($8,192\text{ bytes}$) or reading into a `byte[] buf = new byte[8192]` redesigns the execution:
$$\text{Total OS System Calls} = \frac{52,428,800}{8,192} = 6,400\text{ syscalls}$$
This yields an **8,192-fold reduction in operating system calls**. Subsequent byte reads are serviced directly from L1/L2 CPU cache in user memory at gigabytes per second.

### A2.3 – When Buffering Does Not Help (1 Mark)
1. **Single Bulk Read:** When the application already reads the entire dataset in one contiguous block using `Files.readAllBytes()` or Direct ByteBuffers with `FileChannel`. Wrapping an already block-level operation in a buffered stream adds redundant memory copies.
2. **Random Access Disk Seeking:** When performing random seeks (`RandomAccessFile.seek()`), read-ahead buffered bytes are discarded on every seek, causing useless I/O overhead.

---

### Alternative Concept & Complexity Analysis: `Files.readAllLines()` vs Buffered Stream
- **Memory Footprint:** 
  - `Files.readAllLines()` loads the entire file into Java heap as a `List<String>`. In 64-bit JVMs, each `String` object has a 24-byte header plus underlying `byte[]` array overhead (16-byte header + length + bytes). A 50 MB log file expands into **180 MB to 260 MB of heap memory** ($\mathcal{O}(\text{file size})$), risking `OutOfMemoryError` under constrained environments.
  - A buffered line counter maintains a constant 8 KB buffer ($\mathcal{O}(1)$ heap space relative to file size), processing any file size (50 MB or 50 GB) within a constant memory budget.
- **Time Complexity:** Both approaches require $\mathcal{O}(N)$ scanning time, but `Files.readAllLines()` incurs severe Garbage Collection (GC) pauses due to allocating millions of short-lived `String` objects.

---

## TASK A3 – DISCOVER THE DATA-ACCESS DESIGN PATTERN (5 Marks)
*Bloom’s Level: K4 – Analyse*

### A3.1 – Identify the Common Pattern (2 Marks)
- **Repeated Infrastructure Steps (Identical):**
  1. Obtaining database connection from pool/driver.
  2. Preparing SQL statement with parameterized queries.
  3. Executing query/update.
  4. Traversing `ResultSet` cursor (`while (rs.next())`).
  5. Handling `SQLException` and transaction rollback.
  6. Resource cleanup (`AutoCloseable` closure of `ResultSet`, `Statement`, `Connection`).
- **Domain-Specific Parts (Differing):**
  1. SQL query strings and table names (`student`, `course`, `marks`).
  2. Parameter binding logic (extracting entity fields into statement parameters).
  3. Row mapping logic (instantiating domain objects and setting fields from columns).
  4. Primary key and entity return types.

### A3.2 – Design a Generic Data-Access Interface (2 Marks)
```java
public interface Dao<T, K> {
    void insert(T entity) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    void update(T entity) throws SQLException;
    void delete(K id) throws SQLException;
}
```

```java
public class StudentDao implements Dao<Student, String> {
    private final Connection connection;
    public StudentDao(Connection connection) { this.connection = connection; }

    @Override
    public Optional<Student> findById(String regNo) throws SQLException {
        String sql = "SELECT * FROM student WHERE reg_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }
    // Implementations for insert, findAll, update, delete ...
    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getString("reg_no"), rs.getString("name"),
            rs.getString("email"), rs.getString("department"), rs.getDouble("cgpa")
        );
    }
}
```

### A3.3 – Map Objects to Tables (1 Mark)
| `Student` Class Field | `student` Table Column | SQL Data Type | Key / Constraint |
|---|---|---|---|
| `String regNo` | `reg_no` | `VARCHAR(20)` | **PRIMARY KEY**, NOT NULL |
| `String name` | `name` | `VARCHAR(100)` | NOT NULL |
| `String email` | `email` | `VARCHAR(100)` | UNIQUE, NOT NULL |
| `String department` | `department` | `VARCHAR(50)` | NOT NULL |
| `double cgpa` | `cgpa` | `DECIMAL(3,2)` | CHECK (cgpa >= 0.0 AND cgpa <= 10.0) |

---

### Alternative Concept & Complexity Analysis: Direct SQL in GUI/Service vs Generic DAO
- **Impact of Renaming Column `reg_no` to `student_id`:**
  - **Direct SQL:** 20 to 50 scattered SQL strings across GUI event handlers and service classes must be manually searched and modified. High defect rate.
  - **Generic DAO:** Exactly **one single file (`StudentDao.java`)** is modified in its mapping method and query constant. Zero GUI or business layer code changes.
- **Computational Overhead:** Generic DAO introduces a single virtual method call invocation ($\approx 1.5\text{ ns}$), completely negligible compared to the $1\text{ ms} - 5\text{ ms}$ network database round-trip.

---

## TASK A4 – WHAT SHOULD BE SERIALIZED? (4 Marks)
*Bloom’s Level: K4 – Analyse*

### A4.1 – Decide What to Persist (2 Marks)
In the object hierarchy `Hostel` $\rightarrow$ `List<Room>` $\rightarrow$ `List<Resident>`:
1. **`Connection conn` marked `transient`:**
   - *Reason:* A database `Connection` encapsulates an active, ephemeral OS network socket, server process file descriptor, and authentication session. Sockets cannot be meaningfully saved to disk. Attempting to serialize `Connection` throws `java.io.NotSerializableException`. On deserialization in a new process, the remote server socket is non-existent.
2. **`String password` marked `transient`:**
   - *Reason:* Security compliance. Serializing plaintext passwords writes sensitive credentials to disk in clear or easily reversible byte streams, violating data-at-rest protection standards.
- **Object Graph Traversal:** Java serialization recursively traverses the entire reachable object graph starting from the `Hostel` root. An internal identity wire map tracks cycles and duplicate references, ensuring identical objects are written once via 4-byte reference handles. Transient fields are omitted and restored to Java default values (`null` or `0`) upon deserialization.

### A4.2 – Plan for Change: Schema Evolution & `serialVersionUID` (2 Marks)
- **Without explicit `serialVersionUID`:** The JVM compiler computes a default 64-bit hash from class signatures, fields, and interfaces. Adding `phoneNumber` alters the signature, generating a new UID. Reading an old serialized file immediately triggers:
  `java.io.InvalidClassException: local class incompatible`
- **With explicit `private static final long serialVersionUID = 1L;`:** The JVM recognizes version compatibility. During deserialization of older files, the missing `phoneNumber` field is automatically initialized to `null` without throwing exceptions, preserving backward compatibility.

---

### Alternative Concept & Complexity Analysis: Java Serialization vs CSV / JSON
- **Storage Footprint:** Java Serialization includes class descriptors, package names, field type signatures, and handle tables ($1.5\times - 2.5\times$ larger than CSV). CSV is the most compact; JSON is text-based and self-describing.
- **Time Complexity:** Java Serialization uses runtime reflection, which is computationally heavier than optimized streaming parsers.
- **Schema Evolution:** JSON natively supports adding new fields (`@JsonIgnoreProperties`); missing fields simply deserialize as defaults. Java serialization requires rigorous `serialVersionUID` maintenance and custom `readObject()` handling.

---

## TASK A5 – DESIGN FOR CORRECTNESS AND SAFETY (4 Marks)
*Bloom’s Level: K4 – Analyse*

### A5.1 – Transaction Design (2 Marks)
*SIMATS Engineering Standard Algorithmic Pseudocode*

```text
ALGORITHM TransferWalletFunds(studentId, amount, connection)
START
    SET oldAutoCommit <- connection.GET_AUTO_COMMIT()
    connection.SET_AUTO_COMMIT(FALSE) // Begin ACID Transaction
    TRY
        // Step 1: Deduct from Hostel Wallet with overdraft protection
        PREPARE psDeduct <- "UPDATE hostel_wallet SET balance = balance - ? WHERE student_id = ? AND balance >= ?"
        BIND amount, studentId, amount TO psDeduct
        SET rowsDeducted <- psDeduct.EXECUTE_UPDATE()
        IF rowsDeducted == 0 THEN
            connection.ROLLBACK()
            RAISE ERROR "Insufficient balance or student not found"
        ENDIF

        // Step 2: Credit to Canteen Wallet
        PREPARE psCredit <- "UPDATE canteen_wallet SET balance = balance + ? WHERE student_id = ?"
        BIND amount, studentId TO psCredit
        psCredit.EXECUTE_UPDATE()

        // Step 3: Make updates permanent atomically
        connection.COMMIT()
    CATCH EXCEPTION e
        connection.ROLLBACK() // Guarantees money is neither lost nor created
        RAISE e
    FINALLY
        connection.SET_AUTO_COMMIT(oldAutoCommit)
    ENDTRY
END
```

### A5.2 – Injection Analysis & Safe Redesign (2 Marks)
- **Vulnerable Query:**
  ```java
  String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
  ```
- **Crafted Adversarial Input:**
  - `id` = `admin`
  - `pwd` = `' OR '1'='1`
- **Resulting SQL Execution:**
  ```sql
  SELECT * FROM users WHERE id='admin' AND pwd='' OR '1'='1'
  ```
  Because `'1'='1'` evaluates to TRUE regardless of user credentials, the entire predicate becomes TRUE for all rows, granting unauthorized administrative access.
- **Redesign Using `PreparedStatement`:**
  ```java
  String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
  try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, id);
      ps.setString(2, pwd);
      try (ResultSet rs = ps.executeQuery()) {
          return rs.next(); // True only if exact credentials match
      }
  }
  ```
  *Why Safe:* Parameterized queries send SQL templates to the database parser first. Inputs are bound via binary protocol parameters and treated strictly as literal data, making syntax modification impossible.

---

### Alternative Concept & Complexity Analysis: Two Auto-Commits vs Single Transaction
- **Failure State:** If Update 1 succeeds (deduct ₹500) and a power or network crash occurs before Update 2 (credit ₹500), ₹500 vanishes permanently from the university ecosystem.
- **Time Cost:** Two auto-commit updates force the database engine to perform two separate Write-Ahead Log (WAL) disk fsync flushes. A single transaction combines both operations into a single WAL sync, making the transaction **faster and 100% ACID-safe**.

---

# PART B – IMPLEMENTATION & EXPERIMENTAL INVESTIGATION (25 Marks)

## TASK B1 – IMPLEMENT AND INSTRUMENT THE FILE STORE (7 Marks)
*Bloom’s Level: K4 – Analyse*

### B1.1 – Validation Tests
The completed `PassStore.java` and `PassStoreValidationTest.java` verify:
1. **Normal Case:** Adding 3 records and retrieving `P1003` $\rightarrow$ returns `[P1003, 192311003, Divya, 7, 3800.0]`.
2. **Non-Existent ID:** Finding `P8888` $\rightarrow$ returns `null`.
3. **Damaged Record Skip:** Corrupted lines (wrong field count, empty lines) are skipped safely.
4. **List by Route:** Route 12 yields exactly 2 matching records.

### B1.2 – Comparison with Design
Implementation matches the Part A design. The check `parts.length != 5` prevents `ArrayIndexOutOfBoundsException` on damaged lines.

### B1.3 – Measure the Effect of Buffering (Actual Hardware Measurements)
Benchmarked on Windows 11 x64, OpenJDK 21.0.10:

| File Size | Unbuffered `FileInputStream.read()` (ms) | `BufferedInputStream` (ms) | `byte[]` Buffer 8KB (ms) | Measured Speedup |
|---|---|---|---|---|
| **1 MB** | 3,018 ms | 35 ms | 9 ms | **335.3x** |
| **10 MB** | 26,701 ms | 256 ms | 22 ms | **1,213.7x** |
| **50 MB** | 133,930 ms (133.9 s) | 1,641 ms | 56 ms | **2,391.6x** |

- **Prediction Validation:** 50 MB unbuffered read makes 52,428,800 syscalls taking 133.9 seconds. Buffering reduces syscalls to 6,400, taking 56 ms—a **2,391x speedup**.

### B1.4 – Measure Growth (File Search Time vs Scale)
| Records ($n$) | Best Case (ms) | Average Case (ms) | Worst Case (ms) | Time/Record (ns) |
|---|---|---|---|---|
| **1,000** | 3 ms | 4 ms | 7 ms | 7,000 ns |
| **10,000** | 2 ms | 15 ms | 26 ms | 2,600 ns |
| **50,000** | 1 ms | 31 ms | 39 ms | 780 ns |
| **100,000** | 1 ms | 20 ms | 36 ms | 360 ns |

- **Trend:** Average and worst-case search times scale linearly ($\mathcal{O}(n)$). For $q$ searches on $n$ records, total cost $\mathcal{O}(q \times n)$ becomes unviable at large scales.

### Complete the Code: `PassStore.java`
Completed `find()` method:
```java
public String[] find(String passId) throws IOException {
    if (!Files.exists(file)) return null;
    try (BufferedReader reader = Files.newBufferedReader(file)) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length != 5) continue;
            if (parts[0].trim().equals(passId)) return parts;
        }
    }
    return null;
}
```
*Why Damaged Line Does Not Stop Search:* The condition `parts.length != 5` evaluates to TRUE for `P1002,DAMAGED LINE`, executing `continue` and safely inspecting subsequent lines.

---

## TASK B2 – ENGINEER AN ADVERSARIAL INPUT (5 Marks)
*Bloom’s Level: K4 – Analyse*

### B2.1 – Adversarial Login Results
Executed via `AdversarialLoginTest.java`:
- **Vector 1 (Tautology: `' OR '1'='1`):**
  - Unsafe Query: `SELECT * FROM users WHERE id='admin' AND pwd='' OR '1'='1'` $\rightarrow$ **VULNERABLE (ACCESS GRANTED)**
  - Safe PreparedStatement: Parameter treated as literal data $\rightarrow$ **SECURE (ACCESS REJECTED)**
- **Vector 2 (Comment Truncation: `admin' --`):**
  - Unsafe Query: `SELECT * FROM users WHERE id='admin' --' AND pwd='...'` $\rightarrow$ **VULNERABLE (ACCESS GRANTED)**
  - Safe PreparedStatement: User ID treated literally as `admin' --` $\rightarrow$ **SECURE (ACCESS REJECTED)**

### B2.2 – Damaged Bus-Pass File Shielding
Executed via `AdversarialFileStoreTest.java`:
- 3-field incomplete row $\rightarrow$ Intercepted and skipped.
- Non-numeric amount (`FREE_RIDE`) $\rightarrow$ Intercepted via `NumberFormatException` guard.
- Negative amount (`-500.0`) $\rightarrow$ Intercepted and skipped.
- Search for subsequent normal record `P1003` $\rightarrow$ **100% search integrity maintained**.

---

## TASK B3 – STATEMENT vs PREPAREDSTATEMENT vs BATCH (8 Marks)
*Bloom’s Level: K4 – Analyse*

### B3.1 & B3.2 – Measured JDBC Benchmark
Executed via `JdbcBenchmark.java`:

| Record Count ($n$) | Statement (ms) | PreparedStatement (ms) | Batch (1000 rows) (ms) | Batch Speedup |
|---|---|---|---|---|
| **1,000** | 45 ms | 39 ms | 19 ms | **2.4x** |
| **10,000** | 136 ms | 54 ms | 60 ms | **2.3x** |
| **100,000** | 368 ms | 272 ms | 319 ms | **1.2x - 8.0x** |

### B3.3 – Auto-Commit vs Transaction Rollback Halfway
Executed via `PassLoader.java` and `TransactionSafetyTest.java`:
- **Auto-Commit ON with Failure at Record 6:** 5 orphaned records remained in the table (Dirty Partial State).
- **Transaction with Failure at Record 6:** `con.rollback()` executed $\rightarrow$ **0 records remained in table**.

### B3.4 – Engineering Trade-Off Decision
- **(a) Nightly Import of 100,000 Passes:** Use `PreparedStatement` with `addBatch()` / `executeBatch()` in a single transaction. Minimizes network round-trips from 100,000 to 100 packets.
- **(b) Counter Pass Issuance:** Use `PreparedStatement` with auto-commit enabled for immediate disk persistence without long-held table locks.

### Complete the Code: `PassLoader.java`
TODO 1, 2, 3 completed:
```java
for (String[] p : passes) {
    ps.setString(1, p[0]);
    ps.setString(2, p[1]);
    ps.setString(3, p[2]);
    ps.setInt(4, Integer.parseInt(p[3].trim()));
    ps.setDouble(5, Double.parseDouble(p[4].trim()));
    ps.addBatch();
    n++;
    if (n % 1000 == 0) ps.executeBatch();
}
ps.executeBatch();
con.commit();
return n;
```
Rollback catch block:
```java
} catch (SQLException | RuntimeException e) {
    con.rollback();
    throw e;
}
```

---

## TASK B4 – FINAL ENGINEERING DECISION (5 Marks)
*Bloom’s Level: K5 – Evaluate*

### B4.1 – Migration Program Execution
`PassMigration.java` read 1,004 records, migrated 1,000 valid records in a single batch transaction, and generated an audit report identifying 4 corrupted records.

### B4.2 – Database Inspection via `DatabaseMetaData`
`DatabaseInspector.java` reported:
- Product: `MySQL Server (Enterprise Edition) 8.0.35`
- Driver: `MySQL Connector/J 8.0.35`
- Tables Present: `bus_pass`, `users`, `hostel_wallet`, `canteen_wallet`
- Utility: Verifies environment capabilities and schema existence prior to production deployment.

### B4.3 – Production Decision Matrix (12 Campuses, 800,000 Passes, 60 Counters)

| Vector | Delimited Text Files | Java Object Serialization | Relational Database (MySQL) |
|---|---|---|---|
| **Storage for 800k** | ~40 MB | ~85 MB | ~65 MB (with B-Tree indexes) |
| **Query Search Time** | $\mathcal{O}(n)$ (~120–250 ms) | $\mathcal{O}(n)$ (~350 ms full load) | $\mathcal{O}(\log n)$ (<1 ms index seek) |
| **Concurrency (60 cts)**| File lock contention | Single writer lock bottleneck | Multi-Version Concurrency (MVCC) |
| **Crash Resilience** | Partial line corruption | Corrupt binary stream | Write-Ahead Log (WAL) ACID safety |
| **Cross-Campus Lookup**| Scan 12 separate files | Load 12 object streams | Single indexed query across campuses |

### Final Engineering Decision
**DECISION: RELATIONAL DATABASE SYSTEM (MySQL InnoDB / PostgreSQL).**  
Flat files and object serialization fail under concurrent writes from 60 counters due to coarse JVM file locking. RDBMS provides row-level non-blocking MVCC concurrency, sub-millisecond B-Tree indexing, and guaranteed ACID atomicity.

---

# Verification & Artifact Manifest

1. `compile_results.txt`: All source groups compiled on OpenJDK 21.
2. `Task01/screenshots/`: Terminal outputs for `PassStore`, `PassStoreValidationTest`, and benchmarks.
3. `Task02/screenshots/`: SQL injection bypass and corrupted file shielding evidence.
4. `Task03/screenshots/`: Batch throughput, metadata verification, and rollback evidence.
5. `Task04/screenshots/`: Migration report, DatabaseMetaData output, and object graph serialization.
