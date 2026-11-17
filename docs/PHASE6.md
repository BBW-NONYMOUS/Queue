# Phase 6 — clearance, reports, audit and backup

Clearance writes lock office (shared) → term (shared) → student → clearance record. The
student lock is the same lock used by fine/payment services, so a new fine or
payment void cannot race an ordinary cleared decision. Balances stay computed.
Assumption: only administrators approve balance exceptions; officers decide
clearance for their assigned office, and backups run on a configured host/admin PC.

## Delivered behaviour

### Clearance

The Clearance card shows a selected student's office decisions and computed
balances for a selected term. Missing decisions read as Pending without creating
rows. Students see only their own seven offices; cashiers can look up student
clearance but cannot edit. Officers see only their assigned office. Admins see all.
The student dashboard now includes current-term balance and clearance columns.

A saved decision supports Pending, Cleared or On hold. Holds require a reason.
Cleared requires a zero balance or an administrator-approved balance exception
with a reason, actor and database timestamp. The dialog requires an explicit
review checkbox. The service rechecks role, active student/office/term, reviewed
record timestamp and balance under locks. Changing the student or term invalidates
old results in the card. An uncertain commit disables another save: close, refresh
and inspect the recorded decision first.

Returning a decision to Pending/On hold removes exception and clearing metadata.
The existing ledger behaviour is preserved: new/amended fines and payment voids
reopen ordinary clearance when debt remains, while an explicit exception stays
recorded. Payments do not automatically clear an office. Repeating an already
approved exception still requires administrator permission.

### Reports

PDF and Excel reports cover students, queue, fines, payments and clearance.
Filters include current student number, program, year, section, term, office and
inclusive dates where applicable. Student roster reports use roster filters;
clearance reports require one term and include pending offices. Date filters
mean queue date, fine assessment date or payment date, respectively.

| Role | Available reports |
| --- | --- |
| Admin | All five reports, all offices |
| Officer | Students; own-office queue, fines and clearance |
| Cashier | Students, fines, payments and clearance |
| Student | Own clearance only |

Role/identity/office rules run inside services even if callers supply different
filter values. The student's linked identity replaces any supplied student number.
Output is checked again for authorization before the final file is published.

Exports use one REPEATABLE READ database snapshot across all report queries.
Normal application operations retain READ COMMITTED. The repository limit is
10,001 rows: more than 10,000 is rejected with a request to narrow filters, never
silently truncated. Preview shows only the first 100 of the full matching result.
Export takes a fresh snapshot, so it may differ from an older preview.

PDF uses OpenPDF 3's `org.openpdf` API, embedded Unicode TrueType font, landscape
A4, repeated table headers, wrapped reasons, right-aligned peso values and page
numbers. Excel uses Apache POI with typed numbers/currency/dates, two-decimal peso
formats, wrapped rows, frozen headers, filters and landscape print settings.
Formula-like strings are written as literal text. An amount beyond Excel's
15-digit precision is rejected for Excel; use PDF or narrow the report.
Report metadata uses the server snapshot timestamp (+08:00), including PDF/XLSX
creation/modification properties. Identifiers remain text, preserving leading zeros.

Payment report identity is read from the immutable receipt snapshot when present;
legacy records fall back to current roster data. Roster filters use the current
student record. A separate Net received column is zero for voided payments; the
original amount/status/reason remain visible. Reports for old terms show current
ledger balances rather than reconstructing a past-day accounting snapshot.

### Audit viewer

The administrator-only Audit log card is read-only, with table, exact actor
username, exact record key and date filters, 50-row pages and before/after JSON
details. Blank dates cover the last 30 calendar days including today, using the
server clock. A query may cover at most 366 calendar days. Actor usernames shown
are current user names; actor ID/database account/request ID remain in details.
Password hashes remain excluded by existing audit triggers.

V008 adds a table/time index. No audit update/delete grant is added. Domain writes
continue to be recorded by database triggers in their transaction. Report reads
and exports do not create domain rows. Backup filesystem writes are tracked by
the backup manifest described below; the app's audit_logs grant remains SELECT.

### Backup

The administrator-only Backup card starts Oracle MySQL 8.4 `mysqldump` through
ProcessBuilder with an argument list, without a shell or command-line password.
The XAMPP/MariaDB executable is rejected. Backup is disabled by default on all PCs.
A separately configured protected option file supplies the backup credential.
The normal application account still has only SELECT/INSERT/UPDATE on domain
records and SELECT on audit/views; no backup privilege is added to it.

The command requests a single-transaction, quick, TLS-verified dump of
`queue_clearance`, including views/triggers and binary-safe values, with no
TABLESPACE export, GTID-purged output or table locks. Pause all DDL while it runs;
normal InnoDB application transactions can continue. Backups capture the database
state at the dump's own transaction start, not the earlier task-start timestamp.

Each run has a unique output folder. `backup.sql.partial` is renamed after a
successful exit, then a manifest records server task/completion times, actor,
byte count, SHA-256 and expected client schema V008. `COMPLETE` is written last.
No marker means incomplete: do not restore that folder. Process failure/timeout
leaves diagnostics for the host administrator; UI messages do not expose them.
Authorization is rechecked before declaring completion. One backup can run per
application process. Use only the designated host, and copy completed backups to
protected offline storage. A checksum confirms file integrity, not restorability.

## Complete source files

Base path: `C:/Users/User/Documents/NetBeansProjects/Queue`.
Every screen has a complete guarded Java/.form pair with GroupLayout; no snippets
or placeholder implementations are required.

| Screen | Source | GUI Builder metadata |
| --- | --- | --- |
| ClearancePanel | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/ClearancePanel.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/ClearancePanel.form) |
| ReportsPanel | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/ReportsPanel.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/ReportsPanel.form) |
| AuditPanel | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/AuditPanel.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/AuditPanel.form) |
| BackupPanel | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/BackupPanel.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/BackupPanel.form) |
| ClearanceDecisionDialog | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/dialog/ClearanceDecisionDialog.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/dialog/ClearanceDecisionDialog.form) |
| AuditDetailsDialog | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/dialog/AuditDetailsDialog.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/dialog/AuditDetailsDialog.form) |
| MainFrame | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/MainFrame.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/MainFrame.form) |
| StudentDashboardPanel | [Java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/StudentDashboardPanel.java) | [NetBeans form](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/StudentDashboardPanel.form) |

New supporting files, relative to `src/main/java`:

- [`model/ClearanceRow.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ClearanceRow.java)
- [`model/ClearanceDraft.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ClearanceDraft.java)
- [`model/AuditFilter.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/AuditFilter.java)
- [`model/AuditRow.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/AuditRow.java)
- [`model/ReportType.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ReportType.java)
- [`model/ReportFilter.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ReportFilter.java)
- [`model/ReportColumn.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ReportColumn.java)
- [`model/ReportValueType.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ReportValueType.java)
- [`model/ReportData.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ReportData.java)
- [`model/ExportFormat.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/ExportFormat.java)
- [`model/BackupResult.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/BackupResult.java)
- [`config/OutputSettings.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/config/OutputSettings.java)
- [`dao/ClearanceRepository.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/ClearanceRepository.java)
- [`dao/ClearanceDao.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/ClearanceDao.java)
- [`dao/OversightRepository.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/OversightRepository.java)
- [`dao/OversightDao.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/OversightDao.java)
- [`service/ClearanceService.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/ClearanceService.java)
- [`service/AuditService.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/AuditService.java)
- [`service/ReportService.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/ReportService.java)
- [`service/BackupService.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/BackupService.java)
- [`report/ReportWriter.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/report/ReportWriter.java)
- [`ui/model/ClearanceTableModel.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/model/ClearanceTableModel.java)
- [`ui/model/AuditTableModel.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/model/AuditTableModel.java)
- [`ui/model/ReportTableModel.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/model/ReportTableModel.java)
- [`ui/model/StudentOfficeTableModel.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/model/StudentOfficeTableModel.java)

Updated integration files:

- [`config/AppConfig.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/config/AppConfig.java)
- [`dao/OrganizationRepository.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/OrganizationRepository.java)
- [`dao/OrganizationDao.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/OrganizationDao.java)
- [`dao/DaoContext.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/DaoContext.java)
- [`dao/UnitOfWork.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/UnitOfWork.java)
- [`dao/JdbcUnitOfWork.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/JdbcUnitOfWork.java)
- [`dao/FinanceRepository.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/FinanceRepository.java)
- [`dao/FinanceDao.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/FinanceDao.java)
- [`model/StudentQueueSnapshot.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/StudentQueueSnapshot.java)
- [`service/QueueService.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/QueueService.java)
- [`service/NavigationService.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/NavigationService.java)
- [`app/Application.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/app/Application.java)
- [`ui/util/StatusCellRenderer.java`](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/util/StatusCellRenderer.java)

Also updated: `pom.xml` (POI Log4j-API to SLF4J bridge), `app.properties.example`, `.gitignore`, `README.md`. Added database
files: V008, `verify_phase6.sql`, `admin/002__backup_user.sql` and
`admin/backup-client.example.cnf`. The live `app.properties` was not modified.
The schema remains ten tables and 47 triggers.

## Migration and run

Stop clients and take the existing DBA backup first. V001–V007 must already be
applied. Run as DBA in the MySQL 8.4 client, once:

```sql
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V008__audit_table_time_index.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase6.sql;
```

V008 is a forward-only index migration. MySQL DDL implicitly commits. On failure,
keep clients stopped and investigate the exact failed statement; do not rerun
an entire partially applied migration. Restore the DBA backup if necessary.

Use JDK 21 and Maven 3.9.6+ with the pinned dependencies:

```powershell
mvn clean verify
java -Dqueueclearance.config=app.properties -jar target/queue-clearance.jar
```

Keep `target/lib` beside the JAR. Existing external configuration works with
backups disabled. To choose report output/font, add these non-secret settings:

```properties
report.output.directory=reports
report.font.file=C:/Windows/Fonts/segoeui.ttf
backup.enabled=false
```

Relative paths resolve beside `app.properties`. For PDFs on another OS, configure
an installed Unicode TrueType font supporting peso and campus names. Protect the
report folder because exports contain student/financial information.

## Configure host backup

1. Install the Oracle MySQL 8.4 client on the designated host/admin workstation.
   The bundled XAMPP/MariaDB mysqldump is not a replacement.
2. Edit the exact allowed workstation IP in `database/admin/002__backup_user.sql`.
   Execute that separate DBA setup script manually. It creates a randomly
   generated backup password with SELECT, SHOW VIEW and TRIGGER on this schema.
   Save that password in the protected host option file, not in app.properties.
   TRIGGER is a privileged metadata grant required by mysqldump, so this credential
   must not be distributed to ordinary clients.
3. Copy `database/admin/backup-client.example.cnf` outside the project, such as
   `C:/QueueClearanceSecrets/backup-client.cnf`. Set the real host, password and
   trusted CA path. The host must match the server certificate. Restrict the
   option file and backup folder with Windows ACLs to the designated operator;
   disable inherited broad access before adding a password.
4. On that workstation only, add to its external app.properties:

```properties
backup.enabled=true
backup.mysqldump.path=C:/Program Files/MySQL/MySQL Server 8.4/bin/mysqldump.exe
backup.options.file=C:/QueueClearanceSecrets/backup-client.cnf
backup.output.directory=C:/QueueClearanceBackups
backup.timeout.seconds=600
```

The option file must target the same college database host. Backup does not copy
MySQL accounts/grants, TLS keys, application configuration or report font files;
retain those separately in protected deployment records. Keep every other client
at `backup.enabled=false`. Sign in as admin, open Backup, confirm DDL is paused
and the folder is protected, then Create backup.

## Restore drill (DBA, separate test server only)

The application does not expose a restore button. A dump includes database/table
creation and DROP statements; never test it against the live college database.

1. Copy a COMPLETE backup folder to a separate, disposable MySQL 8.4 server and
   verify its SHA-256 against `manifest.properties`/`COMPLETE`. Keep the original.
2. Inspect the dump's trigger/view DEFINER accounts; create or deliberately remap
   required definers as DBA on that isolated server before testing the application.
3. In the test server's MySQL client, as DBA, load the copied dump:

```sql
SOURCE C:/RestoreDrill/backup.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase5.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase6.sql;
```

4. Recreate the restricted application user for the test network, configure TLS
   and point a separate application copy to the test server. Check counts, login,
   balances, clearance, audits, queue counters and an isolated payment/void cycle.
   Confirm inserts still create audit records and deletes remain forbidden.
5. Record the backup identity and restore outcome in the host's operational log.
   A successful mysqldump/checksum alone is not a passed restoration test.

## Verification completed here

All 171 production Java sources compile with `--release 21` using the actual
pinned direct libraries, including Application, Hikari wiring, OpenPDF and POI.
The available JDK runtime is 26; this is a Java 21 compatibility compile, not a
JDK 21 runtime test. Offline Maven verify was attempted but cannot resolve the
uncached `maven-jar-plugin:3.4.2`; no packaged build is claimed. The new
runtime logging bridge is also not cached here. Direct export tests therefore
show POI's missing-Log4j-provider diagnostic; the declared bridge routes those
messages to the requested SLF4J/Logback backend in a provisioned Maven build.

Temporary harnesses pass 43 new service/export assertions, 39 asynchronous UI
assertions, 42 new real-DAO prepared-statement binding executions and 13 backup
process/manifest assertions using a synthetic executable. Real OpenPDF/POI files
were reopened; long and empty PDFs rendered, Unicode/peso text extracted, and
Excel types/number formats/formula safety checked. Native Excel printing and
live MySQL query/migration execution remain deployment checks.

All 28 Java/.form pairs pass source/XML structure checks, and the installed
NetBeans layout parser accepts all 30 Free Design layouts. New screens/dialogs
were rendered offscreen and visually inspected. Existing service/UI checks were
rerun for financial, administration and queue regressions. The formal JUnit and
real MySQL concurrency suite is Phase 7.

## Verification checklist

- [ ] Apply V008 once with clients stopped; inspect every verification result.
- [ ] Build and run with JDK 21 and all pinned Maven dependencies available.
- [ ] Open six new pairs and updated MainFrame/StudentDashboard in Design view;
      resize and tab through every control, including the scrollable sidebar.
- [ ] Check Pending defaults for all seven offices and term/student filters.
- [ ] Block unpaid clearance; allow zero-balance clearance; require admin reason
      for exceptions and reject officer/cashier/student attempts through services.
- [ ] On two PCs, race a new fine/payment void against clearing; verify ordinary
      clearance never remains cleared with debt. Refresh stale review dialogs.
- [ ] Verify holds remove exception metadata and new debt reopens ordinary
      clearance while preserving a recorded exception.
- [ ] Verify students see only their own balances/clearance, including dashboard.
- [ ] Export all five report types to PDF/XLSX with dates and roster filters;
      check scope, totals, voided net receipts, Unicode, long reasons and empty data.
- [ ] Confirm formula-like names remain text, dates/numbers are typed, and reports
      over 10,000 rows fail explicitly. Inspect native Excel/PDF print output.
- [ ] Verify only admins can open audit/backup, and audit before/after values match
      decisions, fine changes and payment voids without password hashes.
- [ ] Protect the backup secret/output with OS ACLs; enable only the host; test
      failures, credentials/TLS errors and a completed checksum/manifest.
- [ ] Complete the separate-server restore drill and retain its recorded result.

References: [MySQL 8.4 mysqldump](https://dev.mysql.com/doc/refman/8.4/en/mysqldump.html),
[OpenPDF releases and package migration](https://github.com/LibrePDF/OpenPDF/releases),
[Apache POI spreadsheet guide](https://poi.apache.org/components/spreadsheet/quick-guide.html),
[Log4j API bridge installation](https://logging.apache.org/log4j/2.x/manual/installation.html).

Phase 6 stops here. Say **next** for Phase 7: JUnit tests, real ticket-issue and
call-next concurrency tests, and jpackage packaging.
