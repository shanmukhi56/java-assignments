# CSA09 Assignment 4 - Event-driven GUI Design

SIMATS Engineering | Programming in Java | CO4 | 50 marks

**Name:** FILL IN YOUR NAME  
**Register number:** FILL IN YOUR REGISTER NUMBER  
**Repository:** FILL IN YOUR GITHUB LINK  
**Verification environment:** OpenJDK 21.0.13. Sources use Java 8-compatible constructs, but only Java 21 compilation has been checked here.

## Start here (Windows)
1. Extract this entire ZIP. Install a full JDK 21 if `java -version` or `javac -version` fails.
2. Double-click `compile_all.bat`. The script compiles every task, both converter revisions, and the completed TODO program.
3. Double-click `run_task.bat`. Choose a task from its menu. For Task 5, it runs the updated lambda version; the other commands are below.
4. Read `Assignment4_Report.pdf`. Replace the three identity placeholders above and on the report cover (editable content is in `Report.md`).
5. Follow each task's `test_cases.csv` and `screenshots/README.md`. These are test inputs and capture instructions, NOT claimed GUI test results.
6. Configure MySQL for Task 6 using the instructions below. `--demo` works without a database but does NOT satisfy the JDBC evidence requirement.
7. Execute each task yourself, capture evidence, and commit each task as you genuinely complete it. Do not fabricate commit dates or personal execution evidence.

## Contents and commands
Run commands from the named folder after compiling there with `javac *.java`.

| Folder | Command | Result/purpose |
|---|---|---|
| Task01 | `java FeeWindow` | Separate fee validation/service, independent listeners, worker and receipts (demo only). |
| Task02 | `java AppointmentDesk` | Dependent department/doctor choices, today-only slots and unsaved close confirmation. |
| Task03 | `java LibrarySearch` | Search/filter, 3-result pages, reserve feedback, responsive nested layout. |
| Task04 | `java DrawPad` | Count clicks, preserve dots in a point list, repaint from model. |
| Task05/CompleteCode | `java Converter` | Supplied INR-to-USD/EUR TODO program, button/Enter listener. |
| Task05/Baseline | `java ConverterFrame` (or Named, Anonymous, Lambda) | All four original handler styles. |
| Task05/Updated | `java ConverterFrame` (or Named, Anonymous, Lambda) | All four revised styles: Enter trigger and dialog. |
| Task06 | `java CampusEvents --demo` | Temporary memory mode for interface demonstration. |
| Task06 | `java -cp ".;../lib/*" CampusEvents` | Windows MySQL mode. Linux/macOS classpath uses `:` instead of `;`. |

Currency rates are illustrative assignment constants, never live rates. Appointments and fees are in-memory demonstrations, not production payment/clinical systems.

## Task 6 - MySQL setup
1. Install/start MySQL Server. Execute `Task06/schema.sql` in MySQL Workbench or run `mysql -u root -p < Task06/schema.sql` from this root folder.
2. In an administrator SQL session create a local account, replacing the password yourself:
   ```sql
   CREATE USER 'campus_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_OWN_STRONG_PASSWORD';
   GRANT SELECT, INSERT ON campus_events.* TO 'campus_app'@'localhost';
   ```
3. Download MySQL Connector/J from Oracle's official MySQL downloads site. Put its `.jar` in `lib/` (create the folder). It is a runtime dependency and is not bundled.
4. In Windows Command Prompt, from the project root:
   ```bat
   set DB_URL=jdbc:mysql://localhost:3306/campus_events?connectTimeout=5000&socketTimeout=10000
   set DB_USER=campus_app
   set DB_PASSWORD=YOUR_LOCAL_PASSWORD
   cd Task06
   java -cp ".;../lib/*" CampusEvents
   ```
   In bash use `export DB_USER=campus_app`, `export DB_PASSWORD='YOUR_LOCAL_PASSWORD'` and `java -cp '.:../lib/*' CampusEvents`.
5. Register a new ID, repeat it for the same event, then try empty ID. Open Committee and press Refresh; filter, page, export. Close and reopen the app and Refresh to demonstrate persistence.
6. Run `java -cp ".;../lib/*" JdbcIntegrationTest` for a real database check. It inserts a uniquely named test student (two rows), so run it only against your assignment database.

Do not put passwords in GitHub. Export covers all rows matching the chosen filter, in batches of 500. Paging shows 50 rows and fetches one look-ahead row. A UNIQUE constraint resolves duplicate races at the database. There is no automatic fallback from MySQL to memory mode.

## Actual verification included
- `compile_results.txt`: all eight source groups compiled successfully on Java 21.
- `Task01/test_results.txt`: valid fee and invalid amount service tests.
- `Task04/compiler_experiment.txt`: actual misspelling results, with and without `@Override`.
- `Task06/test_results.txt`: memory-backend service tests including duplicate, filter, pagination and CSV export.
- Graphical interaction screenshots and real MySQL integration have **not** been executed in the build environment. The environment could compile/run Java but did not provide a usable graphical display or MySQL server. Do not describe the automated memory tests as JDBC validation.

## Required GitHub submission
Create `CSA09-<RegisterNumber>-Assignment4`. Set this README's name/register/repository fields, and the cover page of your report. For example, after you execute and capture Task 1:
```text
git init
git add README.md .gitignore Task01
git commit -m "Complete and verify Task 1 fee design"
```
Continue with one genuine commit for each completed task; add the report and scripts in a final documentation commit. Then:
```text
git branch -M main
git remote add origin YOUR_REPOSITORY_URL
git push -u origin main
```
Make it public or explicitly share the private repository with faculty yourself. A working GitHub link, your personal execution, screenshots and progressive commits are mandatory; these files alone do not fulfil those requirements.
