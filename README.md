# Queue-Clearance

Phase 7 adds JUnit tests, opt-in real MySQL concurrency tests and Windows
jpackage scripts. See [Phase 7 files, test setup and packaging](docs/PHASE7.md).
The source implementation covers all seven phases. The native launcher uses
per-user external configuration; no credentials are bundled. JDK 21/MySQL 8.4
integration and installer checks remain required before campus rollout.

## Design decisions and assumptions

- One payment belongs to one fine and has one globally unique receipt reference.
  A receipt covering multiple fines would need a separate allocation table; do
  not duplicate the reference or concatenate fine IDs into a column.
- Each office has one desk. A unique generated key permits only one `called` or
  `serving` ticket per office, including across midnight. A second desk requires
  an explicit workstation model before this constraint can change.
- School time is Asia/Manila (`+08:00`), and currency is PHP. Every database
  session must use `+08:00`. Database defaults/triggers generate event timestamps;
  clients never supply their local clock for queue dates or financial events.
- Fine status is calculated by `v_fine_balances`: unpaid, partial, paid, waived.
  Neither balances nor payment-derived statuses are stored. A waiver forgives
  the remaining amount; it does not reverse previously received money.
- Students have a current program/year/section. Historical roster snapshots are
  outside the supplied ten-table model; old reports use current student details.
- Office codes use the seven supplied abbreviations, without inventing their
  expanded names. Queues start closed. The example term starts inactive and is
  not current until the registrar confirms its dates.
- Audit triggers record all domain inserts/updates in the same transaction,
  including queue counter writes and login metadata writes. Password hashes are
  excluded; a boolean records password changes. Audit entries are append-only.
  The initial administrator's insert has a null actor and a database identity;
  following seed writes are attributed to that administrator.

## Files and package structure

```text
Queue/
  pom.xml
  app.properties                 local credentials; excluded from Git and JAR
  app.properties.example         tracked configuration template
  database/
    migrations/V001__schema.sql
    migrations/V002__audit_and_integrity_triggers.sql
    seeds/001__initial_data.sql
    admin/001__application_user.sql
    verify_phase1.sql
  src/main/java/
    app/                         entry points and PasswordHashTool.java
    ui/                          LoginFrame and MainFrame in Phase 2
      panel/                     JPanel + .form pairs
      dialog/                    JDialog + .form pairs
      model/                     AbstractTableModel implementations
      util/                      SwingWorker, focus and formatting helpers
    service/                     authorization, rules and transactions
    dao/                         sole java.sql boundary
    model/                       domain classes and enums
    config/                      external config and HikariCP setup
    report/                      PDF, Excel and receipt output
  src/main/resources/logback.xml
  src/test/java/                 JUnit suites in Phase 7
```

Each Java package includes a compilable `package-info.java` describing its role.
The runtime libraries are copied to `target/lib/`; configuration remains external.
OpenPDF 3 uses `org.openpdf` imports when reports are implemented.

## Build and generate the administrator hash

Install JDK **21 or newer**, Maven **3.9.6+ (3.x)** and MySQL Community Server **8.4**.
Development builds target Java 21 even when Maven runs on a newer JDK, including
NetBeans' bundled JDK 26. Windows runtime packaging still requires JDK **21**.
Set `JAVA_HOME` and add Java/Maven to PATH. In PowerShell from this directory:

```powershell
java -version
mvn -version
mvn clean verify
java -cp "target/classes;target/lib/*" app.PasswordHashTool
```

The helper requests and confirms the new administrator password without echoing
it. Use 12 or more characters, within BCrypt's 72 UTF-8 byte limit. It prints a
`$2a$12$...` hash. Keep the password private and paste only the hash into the SQL
session below. No default password or real student data is shipped.

`mvn clean verify` compiles and packages the application. There are no JUnit tests yet;
their absence must not be reported as passing business or concurrency tests.
Open the directory as a Maven project in NetBeans and select JDK 21 or newer.

## Install the database

Use the **MySQL 8.4 client**, not the MariaDB client bundled with XAMPP. The
following is a new-database installation. Run as an installation DBA with
database, table, view, trigger and account creation privileges. Replace the
executable path with the actual MySQL 8.4 installation path if different:

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe' --host=127.0.0.1 --user=root --password --default-character-set=utf8mb4 --abort-source-on-error
```

At the `mysql>` prompt, use forward slashes in file paths:

```sql
SELECT VERSION();
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V001__schema.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V002__audit_and_integrity_triggers.sql;
```

Set `@bootstrap_admin_hash` to the **complete actual hash** printed by the helper
in this same MySQL session, using single quotes, then run:

```sql
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/seeds/001__initial_data.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase1.sql;
```

For example, the assignment takes the form `SET @bootstrap_admin_hash = 'hash';`,
where `hash` must be replaced with the helper's output. A missing/invalid hash
fails the first seed insert. On any seed error execute `ROLLBACK` and correct the
cause; never continue to commit a partially executed seed. The client option
`--abort-source-on-error` stops sourced scripts on errors. Do not use `--force`.
Run the seed once; duplicates deliberately fail instead of resetting credentials.

All verification columns should say `PASS`. The test exercises partial payment,
void, waiver, daily counter, ticket creation, clearance creation and audit
rollback. It rolls back its fixtures and audit rows; auto-increment gaps remain.
It does not verify the future services' authorization or concurrent transactions.

DDL implicitly commits in MySQL. Treat V001/V002 as a forward-only baseline and
never edit them after deployment. On an interrupted first installation, inspect
the failure and recover the new database with the DBA; do not rerun blindly.
For an existing installation, take a verified backup before applying any new,
numbered forward migration. No automatic destructive down script is supplied.

## Provision LAN access and external configuration

1. In `database/admin/001__application_user.sql`, change its explicit
   `192.168.1.0/255.255.255.0` host restriction to the real campus subnet. This
   subnet must also include the host PC if it runs the desktop app via its LAN IP.
2. Configure MySQL's LAN bind address, firewall access from that subnet and TLS
   with a server certificate valid for the database hostname. Keep the trigger
   installation-definer account present and privileged for trigger execution.
3. Source the account script as DBA:

   ```sql
   SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/admin/001__application_user.sql;
   ```

4. Save MySQL's generated application password in the external `app.properties`.
   Set `db.url` to the host PC's certificate-matching LAN hostname and set
   `db.username=queue_app`. Configure the JVM truststore to trust that certificate.
   If restoring the template is needed: `Copy-Item app.properties.example app.properties`.
5. The connection loader in Phase 2 will require a nonempty password and read
   `-Dqueueclearance.config=<absolute-path>`, defaulting to `./app.properties`.
   Java properties use backslash escaping: use `/` in Windows directory values.

The account receives SELECT/INSERT/UPDATE on domain tables and SELECT on audit
and report views. It has no DELETE, DDL, GRANT, TRIGGER or routine privileges.
Only definer triggers insert audit rows. No `FLUSH PRIVILEGES` is necessary.
Backups in Phase 6 must run on the host with separate operator credentials;
granting database administration privileges to desktop clients is inappropriate.

A direct JDBC desktop deployment cannot hide its database credential from a
person controlling the client PC. Service role checks enforce the application
workflow, but the shared database account does not provide per-student database
isolation against someone bypassing the app. Deploy only on managed campus PCs;
stronger hostile-client isolation requires a server boundary, which would change
the specified architecture.

## Transaction contract for later phases

Only DAOs import `java.sql`. The DAO transaction runner borrows one connection,
sets READ COMMITTED, disables autocommit, establishes `@audit_user_id` and a new
`@audit_request_id` through prepared statements, and invokes the service-owned
transaction. It commits only after the whole operation succeeds, otherwise rolls
back. It must clear **both session variables in a finally block** before the
connection returns to HikariCP; evict the connection if reset fails. SQL session
variables are not reset by transaction rollback. Never log passwords or hashes.

- **Issue ticket:** lock the office using `SELECT ... FOR UPDATE`, validate
  active/open office and current term; get database `CURRENT_DATE` after the
  office lock, upsert that day's counter, lock the counter with `FOR UPDATE`,
  increment and insert the ticket. Obtain its sequence from the locked row,
  never `MAX(seq) + 1`. Lock student eligibility as necessary after the office.
  If the date rolls over mid-operation, retry the entire transaction so counter
  and ticket dates stay aligned. No counter increment survives a failed issue.
- **Call next:** lock the same office first; reject an occupied desk. Lock the
  first waiting ticket for database `CURRENT_DATE`, ordered by `sort_at, id`,
  with `LIMIT 1 FOR UPDATE`, and update its status, caller and called time in
  the same transaction. Every officer ticket transition takes the office lock
  first. Do not use `SKIP LOCKED` to silently jump ahead of an earlier ticket.
  Old-day active tickets must be cancelled by an audited service operation
  before a new day's first call; they are never silently carried forward.
- **Payments, fines, waivers and clearance:** serialize all balance-affecting
  writes and clearance decisions by locking the **student row first**, then
  fine rows in ascending ID order, then the affected clearance row. Record a
  payment only after locking its fine with `SELECT ... FOR UPDATE`, recomputing
  non-voided payments and rejecting an amount greater than the remaining balance.
  The common student lock prevents clearance/payment and fine/clearance races.
  Do not compute a payment limit from a view before acquiring the row locks.
- **Clearance:** pending is the default. A cleared record with a positive balance
  requires an explicit exception reason and authorizer. New fines, payment voids,
  fine increases or undoing waivers must reopen ordinary cleared records to
  `on_hold` in the same transaction, or record an authorized balance exception.
  These cross-table rules belong in services; Phase 1 CHECK constraints alone
  cannot enforce a balance across multiple tables.
- **Defer/requeue:** defer removes a ticket from selection and increments its
  defer count. Requeue changes it to waiting and sets `sort_at` to database now.
  The printed sequence never changes, and sorting uses `sort_at, id`.
- **Retries:** retry the complete transaction for bounded deadlock/lock-timeout
  failures only after rollback. A lost connection during commit has an uncertain
  outcome: reconcile by unique receipt reference or ticket identity before any
  resubmission, rather than risk charging or issuing twice.

All service calls from forms will use SwingWorker, with disabled action buttons,
typed failures and readable UI messages. GUI forms will use NetBeans guarded
`initComponents()` blocks and GroupLayout; the shell sidebar/content routing will
use one MainFrame and CardLayout.

## Phase 1 verification checklist

- [ ] JDK 21 and Maven 3.9.6+ build with `mvn clean verify`.
- [ ] MySQL server reports 8.4, not MariaDB or an older MySQL release.
- [ ] Both migrations and the seed run once without errors.
- [ ] Verification SQL reports PASS for all checks and leaves no fixture rows.
- [ ] Seven offices exist, all queues closed; registrar confirms term dates.
- [ ] Admin hash was generated locally; no shared password was committed.
- [ ] Application account grants contain no DELETE or DDL, and audit is read-only.
- [ ] External properties contain the actual host/password and trusted TLS setup.
- [ ] A second LAN PC can connect using the restricted account over TLS.
- [ ] Future phases retain the documented locking order and pooled audit reset.

Phase 1 database foundation is complete. Current phase status and the next
phase are documented in [Phase 4](docs/PHASE4.md). Student/user management remains
documented in [Phase 3](docs/PHASE3.md). Authentication setup remains
documented in [Phase 2](docs/PHASE2.md).

## References checked for this foundation

- [MySQL 8.4 locking reads](https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html)
- [MySQL 8.4 CREATE USER and random passwords](https://dev.mysql.com/doc/refman/8.4/en/create-user.html)
- [HikariCP supported runtime and configuration](https://github.com/brettwooldridge/HikariCP)
- [OpenPDF releases and package naming](https://github.com/LibrePDF/OpenPDF/releases)
- [JUnit 5.14.2 documentation](https://docs.junit.org/5.14.2/release-notes.html)
- [Apache POI release artifacts](https://poi.apache.org/download.html)
- [Logback release notes](https://logback.qos.ch/news.html)
