# ASSIGNMENT 4 | CO4

EVENT-DRIVEN GUI DESIGN

Programming in Java (CSA09)
SIMATS Engineering

| Submission field | Complete before submission |

| --- | --- |

| Student name | ________________________________ |

| Register number | ________________________________ |

| GitHub repository link | ________________________________ |

| Submission date | ________________________________ |

Six tasks covering event delegation, layout design, repainting, listener styles and layered Swing-JDBC integration. The complete Java sources and SQL schema accompany this report.

## What is complete

All requested implementation variants are included. All source groups compiled on OpenJDK 21.0.13. The fee and registration service tests passed. The drawing-pad compiler experiment was executed and its actual diagnostic is included.

## Evidence boundary

**GUI screenshots and live MySQL integration remain to be completed locally.** This environment did not provide a usable display or MySQL server. The Task 6 memory tests are explicitly identified as tests of a demonstration repository. They are not evidence of a successful JDBC connection.

The assignment also requires your own execution, a working GitHub link and progressive per-task commits. No repository link or personal execution history has been invented.

## Report map

Task 1: diagnosis and repair (page 2). Task 2: events, hierarchy and trace (pages 3-4). Task 3: layout (page 5). Task 4: drawing forensics (page 6). Task 5: style comparison (page 7). Task 6: requirements, design and implementation (pages 8-10). Verification and submission (page 11).

# Task 1 | GUI design review

## 1.1 Diagnosis

| Deficiency | User experience or failure |

| --- | --- |

| Mixed responsibilities | The window validates, constructs SQL and performs payment in one callback. Business rules cannot be tested without GUI/database dependencies. |

| String identity comparison | == compares String references, not text; a logically correct Pay command may enter the clearing branch. Use equals or separate listeners. |

| Unchecked input | Empty, nonnumeric, negative or non-finite amounts fail or become invalid payments. Missing register numbers are accepted. |

| EDT blocking | Database I/O and sleep(3000) prevent event processing: the window freezes and may not repaint. |

| Unsafe SQL | Concatenated register text permits SQL injection and breaks quoting. Bind values with PreparedStatement. |

| Wrong Receipt behaviour | Every non-Pay command clears fields; the Receipt button cannot retrieve a receipt. |

| Weak feedback and resource handling | Console-only Paid feedback is easy to miss; connection/statements leak, close handling is absent, and checked exceptions have no useful recovery. |

## 1.2 Repaired design

**FeeWindow** collects strings and displays feedback. **FeeService** validates register numbers and BigDecimal amounts and returns a receipt. Each button has its own lambda. Payment runs in a SwingWorker; done() updates Swing controls on the EDT. Pay/Clear are disabled during payment and restored even after failure. Closing disposes the window.

The delivered fee service is deliberately an in-memory payment demonstration. It never charges a bank account. A persistent implementation would inject a repository and use the parameterized insert below inside try-with-resources, off the EDT.

```text

try (Connection c = dataSource.getConnection();
     PreparedStatement p = c.prepareStatement(
       "INSERT INTO fees(reg_no, amount) VALUES (?, ?)")) {
  p.setString(1, regNo); p.setBigDecimal(2, amount);
  p.executeUpdate();
}

```

## 1.3 Design defence (57 words)

The window handles presentation while FeeService owns validation and payment rules. Service tests do not need a graphical display. Independent listeners make each button explicit and prevent Receipt from falling into Clear behaviour. Background work keeps the window responsive. The service can later use a database repository without rewriting the form or duplicating business rules across handlers.

**Complexity:** a chain can perform k command checks, O(k) worst case (assuming bounded command length), with one listener, O(1) listener storage. Directly registered handlers dispatch in O(1) with O(k) listener objects. Both still store O(k) buttons. Payment/database time is separate from dispatch.

# Task 2 | Appointment events and components

## 2.1 Event model

| Source | Event / listener | Action |

| --- | --- | --- |

| Department JComboBox | ActionEvent / ActionListener | Repopulate doctor list; mark form dirty. |

| Doctor / slot JComboBox | ActionEvent / ActionListener | Record an in-progress selection. Suppress dirty flag during internal refresh. |

| Patient document | DocumentEvent / DocumentListener | Track text changes as unsaved edits. |

| Book JButton | ActionEvent / ActionListener | Validate ID, doctor and slot; reject occupied slot; confirm booking. |

| Window opens | WindowEvent / WindowAdapter.windowOpened | Log opening; initial data already loaded in constructor. |

| Window close request | WindowEvent / WindowAdapter.windowClosing | Ask before discarding dirty data; dispose only if allowed. |

| Window disposed | WindowEvent / WindowAdapter.windowClosed | Record final closure. |

## 2.2 Containment hierarchy

Diagram: AppointmentDesk : JFrame; form : JPanel / GridLayout(4,2) / patient, department, doctor, slot; bottom : JPanel / BorderLayout / Book button, status label; JTextField + three JComboBox / with four JLabel captions; JButton + JLabel / Confirmation via JOptionPane

The outer frame uses BorderLayout. The form occupies CENTER; the bottom panel occupies SOUTH. The prototype contains two departments with two doctors each and three slots for today. A set of doctor/slot keys prevents double booking within the current run. No clinical records are stored.

## Validation rules

Patient ID: 3-20 letters, digits or hyphens. A doctor must be selected. The doctor/slot combination must be free. A failed booking preserves the form and dirty state; success clears the dirty flag. Editing any field after success makes the next close request confirm again.

# Task 2 | Pseudocode, trace and alternative

## 2.3 Handler algorithms

The assignment refers to a SIMATS pseudocode standard but does not include its syntax. The following uses explicit START/END, IF/ENDIF and FOR/ENDFOR notation; adapt formatting if your faculty supplies a separate template.

```text

ALGORITHM RefreshDoctors
START
  SET updating TO true
  READ selectedDepartment
  CLEAR doctor choices
  FOR EACH doctor IN doctorsByDepartment[selectedDepartment]
    ADD doctor TO doctor choices
  ENDFOR
  SET updating TO false
  SET dirty TO true IF change was initiated by the user
END

ALGORITHM BookAppointment
START
  READ and TRIM patientID
  IF patientID format is invalid OR no doctor is selected THEN
    DISPLAY validation error
    RETURN
  ENDIF
  SET key TO selectedDoctor + selectedSlot
  IF key exists in bookings THEN
    DISPLAY slot unavailable
    RETURN
  ENDIF
  INSERT key INTO bookings
  SET dirty TO false
  DISPLAY booking confirmation
END

```

## 2.4 Event trace

main() schedules construction using invokeLater. On the EDT the constructor creates controls, registers listeners, calls refreshDoctors() and clears the initial dirty flag. setVisible(true) causes windowOpened; activation/focus and painting may also occur in platform-dependent order. A department ActionEvent calls refreshDoctors. A Book ActionEvent calls book. A close request calls windowClosing. If the user cancels, the window stays open. If accepted, dispose causes windowClosed. Painting can occur many times, not just at startup.

A historical applet uses init once, start when activated, paint whenever redraw is required, stop when inactive and destroy on final removal. stop/start may repeat. These are container-controlled lifecycle callbacks; a desktop frame uses main, an EDT and window events instead. This project does not require running an applet.

## Alternative: database lookup on every department change

Let d be departments, M the total doctors, and m the doctors in the selected department (if each department has m doctors, M = d x m). The cache loads O(M) data once and stores O(d + M). A map lookup is expected O(1); clearing/rebuilding the combo is O(m_old + m_new). An indexed database query costs approximately O(log M + m), plus network latency and the same UI population cost; without an index it may scan O(M). The client stores O(d + m), while the database still stores O(M).

Use SwingWorker for a database fetch. Capture the department on the EDT, query in doInBackground, and apply rows in done only if that department is still selected (or use a request sequence number). Otherwise a slower earlier request can overwrite a newer choice. A cache saves repeated latency but needs refresh/invalidation when doctor data changes.

# Task 3 | Campus library layout

## 3.1 Layout decisions

| Container | Layout | Reason |

| --- | --- | --- |

| Frame | BorderLayout | CENTER absorbs extra space; NORTH and SOUTH keep preferred heights. |

| Top panel | BorderLayout | Title above the search row without forcing results to equal height. |

| Search row | FlowLayout | Text field, category combo and button use preferred sizes. |

| Results | JScrollPane in CENTER | List viewport expands and scrolls when data exceeds available space. |

| Bottom panel | BorderLayout | Navigation row above status line. |

| Navigation row | FlowLayout | Prev/Next/Reserve keep natural button sizes. |

## 3.2 Nested containers

Diagram: LibrarySearch : JFrame / BorderLayout; NORTH: top / BorderLayout / Title + search / FlowLayout; CENTER: JScrollPane / JList results; SOUTH: bottom / BorderLayout / Navigation + status; JTextField query / JComboBox category / JButton Search; FlowLayout: Prev/Next/Reserve / JLabel status

## 3.3 Implementation and 3.4 resize protocol

LibrarySearch.java implements case-insensitive title search, category filtering, three-result pages and selection-based reservation feedback. The JComboBox is the Swing equivalent of the AWT Choice. A minimum width of 520 avoids FlowLayout clipping the search controls at very narrow sizes.

| Requested size | Expected behaviour / evidence to capture |

| --- | --- |

| 520 x 300 | Compact centre viewport. Search and navigation remain visible. |

| 700 x 430 | Results get additional width and height; controls retain preferred sizes. |

| 1000 x 650 | Centre grows further; title/status span width. Capture this as the third actual run image. |

**Observed status:** compilation passed; resize expectations above have not been visually verified in this environment. Add T3-01 screenshots after local execution.

## Alternative layout analysis

GridLayout divides equal cells, so it is unsuitable for the outer screen. CardLayout is useful for switching screens, not simultaneously showing these regions. GridBagLayout can express a weighted, filling results row with fixed other rows in one main panel. A typical pass scans components and grid constraints, O(n + r x c); internal grid/constraint processing is implementation-dependent. Nested simple layouts visit O(n) components; they add O(p) panel objects. GridBag uses fewer panels but O(n) constraint metadata. Absolute positioning applies O(n) bounds with little layout logic, but without explicit resize calculations controls neither grow nor adapt to fonts/DPI. All approaches retain O(n) component storage.

# Task 4 | Event-handling forensics

## 4.1 Locate the defects

mouseclicked is not mouseClicked: Java method names are case-sensitive. The anonymous adapter creates a different method, so the inherited empty mouseClicked runs. Separately, count++ never updates status; the Label remains at Clicks: 0. Drawing with getGraphics is transient, may return null, and bypasses the normal repaint model.

## 4.2 Failing interaction

Correct only the method name, click three positions, then minimize and restore (or cover/uncover) the window. The original implementation has no persistent model of the dots. When AWT clears/redraws the window, those pixels are not recreated. This is a reproducible test procedure; a local before/after screenshot is still required.

## 4.3 Completed code

```text

@Override public void mouseClicked(MouseEvent e) {
    points.add(e.getPoint());
    status.setText("Clicks: " + points.size());
    repaint();
}

@Override public void paint(Graphics g) {
    super.paint(g);
    for (Point p : points)
        g.fillOval(p.x - 3, p.y - 3, 6, 6);
}

```

The delivered DrawPad.java retains the supplied frame structure and main method. Clicks on the blank frame drawing region are stored in frame coordinates; clicks on the Label are not drawing clicks. For a larger application, a dedicated Canvas or JPanel would isolate the drawing area and its coordinate system.

## 4.4 Actual compiler experiment

The assignment asks to remove @Override, misspell the method and record the compiler message. **There is no error in that case.** The experiment compiled successfully with exit code 0. With @Override retained, the same misspelling produced exit code 1 and this diagnostic:

```text

error: method does not override or implement a method
from a supertype
    @Override public void mouseclicked(MouseEvent e) {
    ^
1 error

```

The exact terminal output is in Task04/compiler_experiment.txt. @Override converts a silent behavioural bug into a compile-time error.

## Alternative: off-screen image

Create a BufferedImage of width W and height H. On each click, obtain its Graphics2D, draw the dot, dispose that context, increment the count, update status and repaint. In paint, drawImage copies the retained buffer. Appending a fixed-size dot costs O(1); repainting a point list is O(n) for n dots, whereas copying the buffer costs O(W x H) pixel work (not universally O(1)). Image copying is independent of n at fixed dimensions. Storage is O(n) points versus O(W x H) pixels, commonly about 4WH bytes for ARGB. On enlargement allocate a new buffer and copy the old one; shrinking/clipping can lose off-screen data unless the larger image or point model is retained.

# Task 5 | Four listener styles

## 5.1 Implementations and 5.2 changed requirement

Task05/Baseline and Task05/Updated each contain four runnable programs. All include amount, From/To choices, Convert, result label and WindowAdapter closing. The completed supplied INR-only skeleton is separately retained in Task05/CompleteCode/Converter.java.

| Style | Handler registration | Assessment |

| --- | --- | --- |

| Frame implements interface | ActionListener handler = this | Direct field access; frame mixes UI and listener contract. Many commands need routing. |

| Named inner class | new ConvertHandler() | Readable name, direct outer-field access. Reuse same instance locally; external reuse needs extraction. |

| Anonymous inner class | new ActionListener() {...} | Local explicit override; more syntax and harder to name or reuse across screens. |

| Lambda | e -> convert() | Compact for a small action. Captures enclosing fields; keep long logic in a separate method. |

In every updated version one stored handler is shared by the button and amount field. The action calls convert(), which formats the result and then displays a dialog. A bad, negative, infinite or NaN amount produces Invalid amount. Currency rates are fixed illustrative constants from the assignment: 1 INR = 0.012 USD or 0.011 EUR.

```text

button.addActionListener(handler); // already in baseline
amount.addActionListener(handler); // new trigger
// At the end of convert():
JOptionPane.showMessageDialog(this, result.getText(),
    "Conversion result", JOptionPane.INFORMATION_MESSAGE);

```

**Measured source change:** each style requires two added executable statements: field listener registration and dialog display. No handler body/signature changes are needed because all versions already delegate to convert(). The window title also changes from Baseline to Updated for identification; that is not a behavioural requirement change. This demonstrates that factoring the conversion logic matters more than choosing lambda syntax alone.

## 5.3 Choice for a large application

Use a named Action/controller backed by a separately testable conversion service. It gives the operation a reusable identity and centralizes name, enabled state, keyboard accelerator and menu/button behaviour. Lambdas remain suitable as short local adapters; embedding validation and business rules in many lambdas would still be hard to maintain.

## Method reference / shared Action alternative

```text

// ActionListener needs an ActionEvent parameter.
private void convert(ActionEvent event) { convert(); }
ActionListener listener = this::convert;
// Reuse listener for both triggers.

Action action = new AbstractAction("Convert") {
  public void actionPerformed(ActionEvent e) { convert(); }
};
JButton button = new JButton(action);
JMenuItem menu = new JMenuItem(action);
amount.addActionListener(action);

```

A zero-argument convert() cannot directly satisfy ActionListener via this::convert; the overload above adapts the signature. One shared handler/Action gives O(1) handler objects and O(t) registrations for t triggers. The frame style creates no additional listener object; the other delivered versions each create one handler. Lambda allocation is JVM-dependent. Every new trigger needs one hookup; a shared Action also propagates enabled state and labels. Conversion is O(L) in input length for parsing and O(1) arithmetic; fixed GUI/handler state is O(1).

# Task 6 | Requirements and screen flow

## Stage 1: interpret the problem

| Requirement | Testable acceptance condition |

| --- | --- |

| Users | Students/reception staff register; committee views and exports. This classroom prototype has no login or role enforcement. |

| Form data | Student ID, student name and one event from Music/Dance/Quiz; generated numeric row ID and timestamp in database. |

| Validation | Reject blank/malformed ID, name outside 2-80 characters, control characters in name or unknown event. Preserve form and display reason. |

| Uniqueness | A normalized student ID can register once per event; a second event is allowed. Concurrent duplicates are rejected by UNIQUE(student_id,event_name). |

| Persistence | MySQL records remain after app restart. Demo memory mode explicitly does not persist. |

| Responsiveness | Database loading, insert and CSV writing execute in SwingWorker, not on the EDT. Controls recover after errors. |

| Viewing/export | Filter by event, display 50 rows per page, export every matching row with header and safe CSV quoting. |

## Stage 2: screens and dialogs

Diagram: JFrame + JMenuBar / File: Export / Exit; Help: About; Register tab: JPanel / GridLayout / ID, name, event, Register / Shared status message; Committee tab: JPanel / Filter, Refresh, Export / JTable, Prev, Next; Dialogs / CSV destination / overwrite / About / wait-before-close

A JTabbedPane provides both screens. After successful registration the status directs the user to Committee and Refresh. Validation and database failures appear in the shared status line. Export opens a file chooser and confirms before replacement. Closing during a background operation asks the user to wait, avoiding an ambiguous abandoned insert.

The report diagrams are design illustrations, not execution screenshots. They map directly to the classes and Swing components included in the source.

# Task 6 | Layered classes and handler design

## Stages 3 and 4: responsibilities

Diagram: CampusEvents : JFrame / Capture input, SwingWorker, display results; RegistrationService / Validate, normalize, register, page, export; RegistrationRepository interface / insert(...), list(event, limit, offset); JdbcRegistrationDao / PreparedStatement + try-with-resources; MemoryRegistrationDao / Explicit demo/test repository

Registration is a read-only row object (id, studentId, name, event). CampusEvents depends on RegistrationService; the service depends on RegistrationRepository. JdbcRegistrationDao opens one connection per operation, binds SQL values, and closes connections, statements and result sets. No credentials are embedded in source. JDBC configuration comes from environment variables.

Validation occurs in the service and is also invoked before starting the worker for immediate feedback. There is no unsafe check-then-insert duplicate sequence. The database unique constraint is authoritative; MySQL duplicate error 1062 becomes a clear service error. Other database errors propagate to the GUI. All widget reads happen before background execution; all widget writes happen in done().

## Register action pseudocode

```text

START
  READ studentID, name, event ON EDT
  VALIDATE using RegistrationService
  IF invalid THEN DISPLAY reason; RETURN ENDIF
  DISABLE operation controls; SET busy TO true
  IN BACKGROUND:
    NORMALIZE ID; INSERT with prepared SQL
    MAP duplicate-key error to meaningful message
  ON COMPLETION ON EDT:
    DISPLAY success OR error
    SET busy TO false; ENABLE controls
END

```

## Filter action pseudocode

```text

START
  CAPTURE selectedEvent; SET page TO 0 ON EDT
  DISABLE operation controls
  IN BACKGROUND:
    FETCH 51 rows WHERE event matches (unless All)
    ORDER BY id LIMIT 51 OFFSET page * 50
  ON COMPLETION ON EDT:
    REPLACE table with first 50 rows
    ENABLE Next only IF 51st row exists
    ENABLE Prev only IF page > 0
    DISPLAY status; ENABLE controls
END

```

# Task 6 | Database, implementation and scale

## Stage 5: implementation and demonstration plan

Task06 contains the complete Swing application, service, repository interface, two repository implementations, SQL schema, ServiceTest and JdbcIntegrationTest. Use --demo to inspect the UI without database setup. Run without --demo to use JDBC; a database failure never silently changes the backend.

| Case | Input / expected result | Actual evidence status |

| --- | --- | --- |

| Normal | S001 / Student One / Music accepted | Memory service test passed; MySQL run pending. |

| Invalid | Empty ID rejected with reason | Service test passed; GUI image pending. |

| Duplicate | Repeat S001 + Music rejected | Memory service test passed; real unique constraint check pending. |

| Allowed variant | S001 + Quiz accepted | Memory service test passed. |

| Filter/page/export | 55 Music rows; 50-row page plus look-ahead; 55-row CSV | Memory service test passed. |

| Restart persistence | MySQL rows remain after reopening | Requires local MySQL execution. |

## Schema and security decisions

The registrations table uses a BIGINT AUTO_INCREMENT primary key, VARCHAR student ID/name/event, timestamp, UNIQUE(student_id,event_name) and an event/id index. IDs are trimmed and uppercased. The application user needs only SELECT and INSERT. Prepared statements separate data from SQL. Database credentials stay in environment variables and must not be committed.

CSV export quotes fields, escapes embedded quotes, and prefixes formula-leading text to reduce spreadsheet formula interpretation. It writes a temporary file then replaces the destination after successful completion; failure removes the temporary file. Batches hold at most 500 rows. Export is not a transactional snapshot: registrations added during export may appear in later batches. The prototype has no edit/delete, login or authorization layer.

## Alternative: load all rows versus page them

| Approach | Time / memory / responsiveness |

| --- | --- |

| All 100,000 rows | Database transfer and table construction are O(N); client holds O(N) rows. An assumed 500 bytes per row gives roughly 50 MB before additional UI/JVM overhead. If done on EDT the window freezes; a worker avoids I/O blocking but attaching a huge model can still stall the EDT. |

| LIMIT/OFFSET, page size 50 | First page can be approximately O(50) with suitable ordering/indexes. Late pages may scan O(offset + 50), so OFFSET is not constant-time. Client table holds O(50), plus one look-ahead row. At 500 bytes/row the visible row estimate is about 25 KB, excluding Swing overhead. |

| Loading every page sequentially | Total transferred rows are O(N), but repeated OFFSET traversal can approach O(N squared / pageSize). Keyset pagination WHERE id > lastId avoids increasing skip costs, though random page access becomes harder. |

The byte estimates are illustrative assumptions, not measurements. A proper benchmark would seed a disposable database, record row count and schema, warm up queries, measure median elapsed query/model-update times, track heap and observe EDT responsiveness. No unsupported 100,000-row benchmark is claimed.

# Verification and submission checklist

## Observed automated checks

All eight source groups compile successfully with the available OpenJDK 21 compiler. Task 1 validates a positive decimal fee and rejects six invalid amount inputs. Task 6 checks normal registration, duplicate rejection, invalid ID, another event, filtering, pagination and CSV export using the explicitly selected memory repository. Task 4 verifies the actual compiler behaviour for a misspelled override. These logs are included beside the relevant code.

## Local execution sequence

| Step | Action |

| --- | --- |

| 1 | Fill in your name, register number and repository link. Extract the ZIP and install JDK 21. |

| 2 | Run compile_all.bat on Windows. Use run_task.bat for the basic demonstrations. |

| 3 | Execute each folder's test_cases.csv and capture matching screenshots in its screenshots folder. Task 3 needs three sizes; Task 4 needs before/after minimize. |

| 4 | Run all four Baseline and Updated converters, and CompleteCode/Converter. Demonstrate both button and Enter actions and invalid input. |

| 5 | Install/start MySQL and add Connector/J in lib. Run schema.sql, set DB variables and run CampusEvents without --demo. |

| 6 | Capture normal, invalid and duplicate MySQL registrations, filtered table, CSV export and data persistence after restart. |

| 7 | Create CSA09-<RegisterNumber>-Assignment4. Commit each task progressively after genuinely executing it. Submit the working link and completed report. |

## Files supplied

Task01 through Task06 contain Java source code, input/test case CSVs and screenshot capture instructions. Task05 includes all eight style/revision combinations plus the completed skeleton. SQL setup and a runnable integration test are in Task06. The root includes run scripts, README, compilation log, this PDF and an editable Markdown report.

## Limitations to explain in a viva

The supplied report cannot replace your individual execution evidence. Fee payments are simulated. Appointment bookings and library reservations are classroom demonstrations. Currency rates are fixed examples. The event app has an actual JDBC path but database integration is not verified in this environment. Names, register number, repository link and screenshot evidence remain for the student to supply.

## Source and reading context

Primary specification: the supplied ASSIGNMENT4.pdf, SIMATS Engineering, Programming in Java (CSA09), Assignment 4 - CO4. Implementation uses standard Java AWT, Swing, JDBC, collections and I/O APIs. No third-party GUI framework is required. Oracle MySQL Connector/J is the external runtime driver for MySQL; it is not bundled with the project.
