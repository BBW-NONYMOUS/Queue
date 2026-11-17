# Phase 5 — queues, student tickets and Now Serving

The office row is the shared transaction lock for queue mutations. Daily ticket
counters and selected tickets also use row locks; issuance holds a shared lock
on the current term and a write lock on the student. This keeps numbering, desk
occupancy, term changes and student deactivation consistent across clients.
Assumption: one serving desk per office; deferred tickets return only through
an explicit requeue, and closing new admissions does not abandon existing tickets.

## What is implemented

- Admin/officer office queue: issue for an exact student number, FIFO Call next,
  start, complete, defer, requeue, cancel with a reason, status filters and 25-row
  pages. The current desk stays visible independently of the results page.
- Officers are restricted to their assigned office. Students issue for their
  linked identity and can requeue their deferred tickets or cancel their waiting
  and deferred tickets. Cashiers can view the public board.
- Seven-office student dashboard: availability, public serving numbers, waiting
  counts, own latest 200 tickets today, ticket issuance, requeue and cancellation.
  A current active term is required for issuance. Completing a queue ticket does
  not clear an office or change a balance; clearance decisions belong to Phase 6.
- Student call notifications poll every three seconds throughout the signed-in
  session, including My account and modal actions. Optional sound accompanies a
  visible, accessible notification. Calls are read from committed audit events,
  so a call can still alert after the ticket has already become serving/completed.
  The message asks the student to check current status instead of implying that
  an old call is still active. Initial historical calls before login are suppressed
  unless the ticket is currently called/serving. Events deduplicate by individual
  audit ID; a later commit with a lower ID is not skipped.
- Three-second officer and board polling uses SwingWorker with no overlapping
  reads per poller. Mutations disable controls. Poll generations discard late
  results on navigation, refresh replacement and logout. Student monitoring stays
  active during student actions; the final refresh invalidates pre-action results.
- TV board exposes office codes, numbers, status, waiting counts and queue-open
  state; it never displays student identities. Full screen uses the same MainFrame.
  Exit display or Escape restores the normal shell. A failed refresh clears live
  numbers and shows OFFLINE. A slow network can delay a refresh beyond three seconds;
  no second poll is started while the previous one is pending.
- All queue mutations retain the original ticket number. Requeue moves sort_at to
  database now and clears the previous call/serving fields; audit entries preserve
  history. Sequence formatting uses a minimum of three digits without truncation.
- Old active tickets are cancelled with reason “Daily queue rollover” under the
  office lock during issue, Call next or Close previous day. Read-only polling does
  not write. A new day starts a new counter for each office.

## Retry and recovery behaviour

Each Issue ticket dialog creates one UUID request key, persisted with a globally
unique, immutable constraint. Replaying the key returns that exact ticket even
if it has completed, been cancelled, or belongs to yesterday. A different request
for a student with an active same-office/day ticket is rejected with its number.

An uncertain commit disables Issue ticket and offers Check request. Recovery
locks the same office row before looking up the key, waiting out any original
transaction. If no ticket exists, the same dialog can retry using its original
key. Closing the dialog loses its recovery key; inspect the student queue before
starting a fresh issue. The UUID is an internal recovery detail, not a receipt
or user-facing ticket number.

Only a confirmed lock conflict or database-day change retries the whole
transaction (at most three attempts). CommitUncertainException is never retried
automatically. Other actions revalidate the selected ticket's updated_at; after
an uncertain action, refresh and inspect its status before acting again.

## Complete source files

All files are implemented in the workspace; none are snippets. Base directory:
`C:/Users/User/Documents/NetBeansProjects/Queue`.

| Screen | Matching source | GUI Builder metadata |
| --- | --- | --- |
| OfficerQueuePanel | [Java](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/OfficerQueuePanel.java) | [NetBeans form](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/OfficerQueuePanel.form) |
| StudentDashboardPanel | [Java](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/StudentDashboardPanel.java) | [NetBeans form](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/StudentDashboardPanel.form) |
| DisplayBoardPanel | [Java](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/DisplayBoardPanel.java) | [NetBeans form](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/panel/DisplayBoardPanel.form) |
| TicketRequestDialog | [Java](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/dialog/TicketRequestDialog.java) | [NetBeans form](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/dialog/TicketRequestDialog.form) |
| MainFrame | [Java](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/MainFrame.java) | [NetBeans form](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/MainFrame.form) |

Each pair contains guarded initComponents/variables, GroupLayout resize rules,
component prefixes, named handlers and custom AbstractTableModel data. MainFrame
now has a scrollable role-based sidebar and CardLayout. New panels have a default
primary action; the ticket dialog supports Enter to issue/check and Escape to close.
Screens use Segoe UI 14, 8-pixel spacing, textual coloured statuses and table
selection preserved by ticket/office ID during refresh.

Supporting files (paths relative to `src/main/java`):

- [`model/QueueAction.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/QueueAction.java)
- [`model/QueueClock.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/QueueClock.java)
- [`model/QueueRow.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/QueueRow.java)
- [`model/BoardRow.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/BoardRow.java)
- [`model/QueueSnapshot.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/QueueSnapshot.java)
- [`model/StudentQueueSnapshot.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/StudentQueueSnapshot.java)
- [`model/QueueCallEvent.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/QueueCallEvent.java)
- [`model/QueueTicket.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/model/QueueTicket.java)
- [`dao/QueueRepository.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/QueueRepository.java)
- [`dao/QueueDao.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/QueueDao.java)
- [`dao/DaoContext.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/DaoContext.java)
- [`dao/JdbcUnitOfWork.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/JdbcUnitOfWork.java)
- [`dao/SqlErrors.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/dao/SqlErrors.java)
- [`service/QueueService.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/QueueService.java)
- [`service/QueueDateChangedException.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/QueueDateChangedException.java)
- [`service/NavigationService.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/service/NavigationService.java)
- [`ui/model/QueueTableModel.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/model/QueueTableModel.java)
- [`ui/model/BoardTableModel.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/model/BoardTableModel.java)
- [`ui/util/SwingPoller.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/util/SwingPoller.java)
- [`ui/util/StudentCallTracker.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/util/StudentCallTracker.java)
- [`ui/util/StatusCellRenderer.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/util/StatusCellRenderer.java)
- [`ui/util/UiStyle.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/ui/util/UiStyle.java)
- [`app/Application.java`](C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/app/Application.java)

Database files:

- `database/migrations/V005__queue_requests_and_calls.sql`: nullable request key,
  updated integrity/audit triggers, indexed virtual audit call-recipient column.
- `database/migrations/V006__backfill_queue_requests.sql`: audited request-key
  backfill in its own data transaction.
- `database/migrations/V007__require_queue_requests.sql`: NOT NULL and unique key.
- `database/verify_phase5.sql`: read-only post-migration checks.

The schema retains ten tables and 47 triggers. There are no added runtime grants;
the application account still uses SELECT/INSERT/UPDATE on domain data and SELECT
on audit data. Admin migration credentials are never used by the desktop app.

## Apply and run

1. Stop every application client, including kiosk and TV sessions. Take a DBA
   backup first. V001–V004 must already have been applied in order. For a fresh
   installation, follow the earlier phase setup first and continue through V007.
2. Connect as the database owner using the MySQL 8.4 client. Run each script once;
   stop on the first error. Do not use `--force` or restart clients between scripts.

```sql
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V005__queue_requests_and_calls.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V006__backfill_queue_requests.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V007__require_queue_requests.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase5.sql;
```

MySQL DDL commits implicitly. This maintenance upgrade is forward-only; if a
script fails, keep clients stopped, investigate the failed statement and restore
the DBA backup if necessary. Do not rerun the whole sequence over partial DDL.
The V006 transaction preserves old queue history and audits the new request keys.

3. Use JDK 21 and Maven 3.9.6+ with the pinned dependencies available:

```powershell
mvn clean verify
java -Dqueueclearance.config=app.properties -jar target/queue-clearance.jar
```

Keep `target/lib` beside the JAR. Each campus PC uses its own external
`app.properties` pointing to the host, with the existing verified TLS settings.
Do not copy a DBA account to clients. The configured polling interval remains
3000 ms. An administrator must confirm the current term through Fines → Term
settings and open new admissions through Offices before students can issue.

4. Log in as an officer and open Office queue. Log in as a student on a second
   client and choose an open office, then Issue ticket. Call/start/complete it at
   the officer desk. Open Now serving as staff on the TV PC and choose Full screen.

## Verification completed in this environment

The bundled JDK is 26, not 21, and the complete pinned Maven dependencies/plugins
are unavailable locally. A JavaCompiler `--release 21` compatibility compile passed
for 137 production files against cached compatible FlatLaf/jBCrypt/SLF4J APIs.
DatabasePool, JdbcUnitOfWork and Application require the unavailable HikariCP
library and were reviewed but excluded from that compatibility compile. A full
Maven/JDK 21 build remains required on a provisioned machine.

Temporary local harnesses passed 44 new service/call-tracker assertions, 19 new
asynchronous UI assertions and 36 actual QueueDao parameter-binding executions.
Earlier checks passed 59 student/admin service assertions, 37 administration UI
assertions, 51 financial assertions, 44 financial UI assertions and 27 FinanceDao
binding executions. Offscreen renders were reviewed at normal and TV sizes.
NetBeans' installed layout parser accepted all 24 Free Design layouts; source/XML
pair checks also passed. These are not live MySQL execution, actual two-PC
concurrency tests, native full-screen testing, or a full Maven build. The formal
JUnit/MySQL concurrency suite remains Phase 7.

## Campus verification checklist

- [ ] Back up, stop all clients, apply V005–V007 and inspect every verification result.
- [ ] Build with JDK 21; open all four new screen pairs and MainFrame in Design view.
- [ ] Confirm a current term, active student/officer assignments and open office queues.
- [ ] Issue concurrently from two PCs; verify distinct numbers and no duplicate active
      ticket for the same student/office/day. Two offices have independent counters.
- [ ] Call next concurrently from two officer PCs; verify exactly one called ticket
      and FIFO order. Complete/defer the current desk before calling another.
- [ ] Defer/requeue: preserve ticket number, increment defer count and move to the back.
- [ ] Verify cancellation reason, stale-selection rejection and officer/student scopes.
- [ ] Simulate a lost issue response; Check request must recover exactly the original
      ticket, including after completion, without consuming another ticket number.
- [ ] Close new admissions: block fresh tickets while existing tickets can still finish.
- [ ] Call while the student is on My account or in a dialog; verify visible alert and
      optional sound. Call then quickly complete between polls; verify one notification.
- [ ] Disconnect/reconnect the network: clear the TV's stale numbers; resume polling
      without duplicate alerts or actions. Log out and verify polling stops.
- [ ] Test midnight using a separate test server; verify a fresh per-office counter and
      prior-day cancellation. Never alter the production clock for this check.
- [ ] On the actual TV, check all seven rows, long ticket numbers, Full screen and Escape
      restoring the original window bounds/sidebar; confirm no student names appear.
- [ ] Check same-transaction audits, server timestamps and unchanged application grants.

References: [MySQL locking reads](https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html)
and [indexed virtual generated columns](https://dev.mysql.com/doc/refman/8.4/en/create-table-secondary-indexes.html).

Phase 5 stops here. Say **next** for Phase 6: clearance decisions, reports,
audit-log viewer and backup.
