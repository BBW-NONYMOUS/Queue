# Phase 7 — JUnit, real MySQL concurrency and native packaging

Fast JUnit tests run without a database. Real concurrency tests use two independent
Hikari pools and production services/DAOs against a separately provisioned Oracle
MySQL 8.4 server. Native packages bundle Java 21 and keep operator credentials and
writable data outside the installation.
Assumption: Windows campus clients, one designated disposable integration schema,
and a JDK 21 build workstation. Tests never fall back to campus app.properties.

## Files

All files are complete in `C:/Users/User/Documents/NetBeansProjects/Queue`.
No new Swing screens were required; all existing Java/.form pairs are preserved.

- [pom.xml](/C:/Users/User/Documents/NetBeansProjects/Queue/pom.xml): safe template resource, fail-on-empty unit tests,
  opt-in Failsafe `mysql-it` profile. Existing application/library versions remain pinned.
- [PackagedLauncher.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/app/PackagedLauncher.java): writable
  user configuration/log bootstrap for the native launcher.
- [InstallationCheck.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/app/InstallationCheck.java): checks
  native Java 21/dependencies/template without opening UI or database connections.
- [ReportWriter.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/main/java/report/ReportWriter.java): regression fix
  for a one-column workbook, which must not merge a header cell with itself.
- [Windows packaging script](/C:/Users/User/Documents/NetBeansProjects/Queue/packaging/windows/package.ps1).
- [Packaged first-run instructions](/C:/Users/User/Documents/NetBeansProjects/Queue/packaging/windows/README-FIRST.txt).
- [Isolated test-account setup](/C:/Users/User/Documents/NetBeansProjects/Queue/database/test/001__isolated_server_accounts.sql).
- [Integration configuration template](/C:/Users/User/Documents/NetBeansProjects/Queue/database/test/mysql-it.properties.example).
- `src/test/resources/junit-platform.properties` and `logback-test.xml` keep tests
  sequential and test logging separate from application logs.
- `.gitignore` excludes release products and local test configuration.

Complete JUnit/support source files:

- [src/test/java/app/InstallationCheckTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/app/InstallationCheckTest.java)
- [src/test/java/app/PackagedLauncherTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/app/PackagedLauncherTest.java)
- [src/test/java/architecture/FormContractTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/architecture/FormContractTest.java)
- [src/test/java/config/AppConfigTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/config/AppConfigTest.java)
- [src/test/java/dao/DaoSafetyTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/DaoSafetyTest.java)
- [src/test/java/dao/integration/FinanceClearanceIT.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/integration/FinanceClearanceIT.java)
- [src/test/java/dao/integration/QueueConcurrencyIT.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/integration/QueueConcurrencyIT.java)
- [src/test/java/dao/MySqlSettingsTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/MySqlSettingsTest.java)
- [src/test/java/dao/SqlScriptTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/SqlScriptTest.java)
- [src/test/java/dao/support/ConcurrentCalls.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/support/ConcurrentCalls.java)
- [src/test/java/dao/support/MySqlFixture.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/support/MySqlFixture.java)
- [src/test/java/dao/support/MySqlSettings.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/support/MySqlSettings.java)
- [src/test/java/dao/support/ServiceFixture.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/support/ServiceFixture.java)
- [src/test/java/dao/support/SqlScript.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/dao/support/SqlScript.java)
- [src/test/java/report/ReportWriterTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/report/ReportWriterTest.java)
- [src/test/java/service/AuthServiceTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/service/AuthServiceTest.java)
- [src/test/java/service/ClearanceServiceTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/service/ClearanceServiceTest.java)
- [src/test/java/service/MoneyTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/service/MoneyTest.java)
- [src/test/java/service/QueueServiceTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/service/QueueServiceTest.java)
- [src/test/java/ui/util/StudentCallTrackerTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/ui/util/StudentCallTrackerTest.java)
- [src/test/java/ui/util/SwingPollerTest.java](/C:/Users/User/Documents/NetBeansProjects/Queue/src/test/java/ui/util/SwingPollerTest.java)

## Unit coverage

The suite contains 70 invocations across 14 unit-test classes. Parameterized money
cases count as separate invocations. It exercises:

- Exact PHP decimal validation, boundaries and rejection of fractional cents.
- BCrypt login/password change, password-array erasure, opaque session authority,
  forged display-role rejection, logout/revocation and inactive linked students.
- Queue identity/scope, closed offices, idempotent requests, rollback-only retries,
  uncertain-commit recovery, FIFO, stale selections, defer/requeue and cancellation.
- Clearance debt blocking, admin-only reasoned exceptions, stale record/balance
  rejection and removal of exception metadata on a hold.
- Call-alert deduplication by individual audit ID, delayed lower IDs, completed
  calls between polls, initial login history and day rollover.
- SwingWorker background work, EDT delivery, non-overlapping polling and discarding
  results after pause.
- DAO rejection of EDT access/read-only writes and safe SQL exception translation.
- Real OpenPDF/POI round trips: typed currency/date values, literal formula-like
  student data, blank nullable dates, server metadata and Excel precision limits.
- External configuration/TLS/pool invariants, per-user bootstrap, preservation of
  edited configuration, safe templates and refusal of non-Java-21 packages.
- Every NetBeans form/source/layout pair, handler names and JDBC layering.

Run from the project root using **JDK 21 and Maven 3.9.6+**:

```powershell
mvn clean verify
```

This runs unit tests and builds the JAR plus `target/lib`. It does not connect to a
database. Unit results are in `target/surefire-reports`. PDF tests use Segoe UI on
Windows; on another build OS configure a Unicode TrueType font, for example:

```powershell
mvn '-Dqueueclearance.test.font=/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf' verify
```

Without an available font only that PDF round-trip case is skipped. Excel tests
and the rest still run. Normal JUnit temporary-directory cleanup remains enabled.
Do not release a package based on a run with skipped financial/export checks.

## Isolated MySQL integration setup

Use a **separate disposable Oracle MySQL 8.4 instance**, with verified TLS. XAMPP's
MariaDB server is not compatible. Do not use the campus host or copy its database
password into the test configuration.

1. On that isolated server, run `database/test/001__isolated_server_accounts.sql`
   manually as its DBA. Adjust the exact account hosts if the build workstation
   connects from another address. Store the generated passwords securely.
2. The owner account has DDL/data privileges with GRANT OPTION only for
   `queue_clearance_test.*`. The client account starts without application grants;
   the fixture grants it SELECT/INSERT/UPDATE on the nine domain tables and SELECT
   on audit/views. It never receives DELETE/DDL or audit INSERT permissions.
3. Binary-logged servers may require the isolated DBA to configure
   `log_bin_trust_function_creators` for trigger installation, or use an isolated
   DBA owner account. Never change the campus server's global settings for tests.
4. Copy `database/test/mysql-it.properties.example` to an external protected file,
   such as `C:/QueueClearanceTestSecrets/mysql-it.properties`. Fill both test
   passwords and the UUID returned by `SELECT @@server_uuid` on the isolated server.
   The JDBC URL must contain no database or embedded credentials and must use
   `sslMode=VERIFY_IDENTITY`. Configure the build JVM truststore for that server's
   CA. Keep the explicit disposable-schema confirmation unchanged.

```powershell
mvn -Pmysql-it '-Dqueueclearance.it.config=C:/QueueClearanceTestSecrets/mysql-it.properties' clean verify
```

The profile runs unit tests first, then the real integration tests during Failsafe's
integration-test/verify lifecycle. Reports go to `target/failsafe-reports`. Missing
or invalid configuration is a failure, not a silently skipped integration run.

Before changing any schema the fixture verifies Oracle MySQL 8.4 and the expected
server UUID, then takes a MySQL advisory lock so two runs cannot collide. It drops
and recreates **only the compiled-in `queue_clearance_test` schema**, loads V001–V008,
creates synthetic students/users and checks ten tables/47 triggers. Production
schema CREATE/USE preambles are omitted; unexpected production-schema references
are rejected. It never runs production seed or application-user provisioning.
The schema is retained afterward for inspection and recreated on the next run.
All SQL execution uses PreparedStatement inside test DAO support code.

### Real concurrency assertions

`QueueConcurrencyIT` contains eight database tests:

1. Eight simultaneous issue requests through two independent pools create unique,
   contiguous per-office sequences and their same-transaction audit records.
2. Eight requests for the same student create exactly one active ticket and consume
   one sequence; other requests receive the business validation error.
3. Simultaneous replays of one request return one ticket, including after completion.
4. Concurrent Call next requests at the single desk produce one winner, choose the
   FIFO head and record exactly one call event; other calls are rejected as occupied.
5. Defer/requeue preserves the number and returns behind existing waiting tickets.
6. An injected service failure rolls back ticket, counter and audit writes together.
7. The application credential cannot delete, create tables or directly append audits.
8. Pooled connections do not retain the previous audit actor/request variables.

`FinanceClearanceIT` contains five additional database tests: competing payments
cannot overpay; receipt replay/void preserves the ledger and reopens clearance;
a fine racing clearance cannot leave ordinary clearance with debt; exceptions
require admin approval; and all five report queries execute against the real schema.

Workers use a start barrier and bounded waits. Every future/error is inspected;
a lock conflict, unexpected SQL error or timeout fails the assertion. Queue tests
also detect a server-date rollover during execution; rerun away from midnight.
They model independent client connections, not two separate physical LAN machines.
Physical two-PC/network interruption testing remains part of the deployment check.

## Windows jpackage build

Install a **Windows JDK 21** matching the campus CPU architecture and put Maven
3.9.6+ on PATH (or pass `-MavenPath`). Set `JAVA_HOME` to that JDK. The script checks
both the JDK release file and actual `java --version`; it rejects JDK 26.

From the project root:

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\packaging\windows\package.ps1 -Type app-image
```

The execution-policy option applies to that process only. Follow campus signing
policy for scripts; the build does not change the machine's policy. An app image
needs no WiX and can be copied as a complete directory to a test workstation.
Do not copy only the executable: keep its `app` and `runtime` directories.

For installable packages, install WiX **3.x** (`candle.exe`/`light.exe` on PATH):

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\packaging\windows\package.ps1 -Type exe
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\packaging\windows\package.ps1 -Type msi
```

Each invocation builds a fresh package and runs unit verification. To require the
integration suite in that same build:

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\packaging\windows\package.ps1 -Type app-image -RunMySqlIntegration -IntegrationConfig C:/QueueClearanceTestSecrets/mysql-it.properties
```

Additional options:

- `-JdkHome` / `-MavenPath`: full paths when the tools are not on PATH.
- `-CheckOnly`: validates prerequisites without starting a build or database test.
- `-AppVersion`: numeric `major.minor.patch`; default is the POM version without
  `-SNAPSHOT`. Bump release versions before upgrading clients.
- `-Destination`: a new destination directory. Existing directories are refused;
  the script never deletes/overwrites an earlier release.

By default outputs are under a unique `dist/Queue-Clearance-<version>-<id>` folder.
The script builds with Maven, stages only the main JAR/runtime libraries, rejects
an included live `app.properties`, links the Java SE/Swing/JDBC/XML/crypto/charset
modules, creates an app image and runs `app.InstallationCheck` inside its runtime.
That check opens no UI or database connection. Installer creation then uses that
same image with a stable upgrade UUID, Start Menu entry, shortcut and per-user
installation. WiX checks apply only to EXE/MSI builds.

`manifest.json` records the release/runtime and SHA-256 of every distribution file.
Retain it with the release. Packages are unsigned by default; apply the college's
normal code-signing and release process before distribution. There is no automatic
updater or database migration hidden inside the installer.

## First run, configuration and TLS

The native launcher uses:

```text
%LOCALAPPDATA%/Queue-Clearance/app.properties
%LOCALAPPDATA%/Queue-Clearance/logs/
%LOCALAPPDATA%/Queue-Clearance/reports/
```

It copies only the non-secret example on first launch and preserves an existing
configuration on upgrades. The template has an empty database password and disables
backup, so first run stops with a configuration message until campus IT fills it.
A separately installed Java is not required on client PCs.

The template still needs the actual campus host, restricted app credential and
trusted TLS configuration. The host must match the server certificate. To configure
a custom JVM truststore, add the required Java options to the installed launcher's
`app/Queue-Clearance.cfg` `[JavaOptions]` section, for example:

```text
java-options=-Djavax.net.ssl.trustStore=C:/CampusTLS/campus-truststore.p12
java-options=-Djavax.net.ssl.trustStoreType=PKCS12
```

Use the campus IT policy for any truststore password, never a database password in
launcher options. Keep the truststore outside the installation and recheck launcher
options after upgrades. Alternatively campus IT can configure the JDK's supported
Windows certificate-store integration; this script does not silently select it.
Do not turn off VERIFY_IDENTITY to work around certificate errors.

The existing `java -jar target/queue-clearance.jar` entry point retains its external
working-directory `app.properties` behaviour, including `-Dqueueclearance.config`.
Only the native launcher defaults to the per-user folder. `queueclearance.config`
and `queueclearance.logDir` JVM properties can explicitly override those paths.
DBA schema setup/grants, backup secrets and database restoration stay separate.
See Phase 6 for protected host backup and restore procedures.

## Verification completed in this environment

- All **173 production** and **21 test/support** sources compile with `--release 21`
  using the actual pinned APIs, including the MySQL integration source.
- The real **JUnit Jupiter 5.14.2 engine ran 70 unit cases: 70 passed, none skipped**.
  Maven plugins are incomplete offline, so a temporary local engine harness was used.
  This Windows sandbox denies Java `toRealPath` during temp cleanup; the local
  harness retained temporary files without changing assertions. The committed
  JUnit tests keep normal cleanup. This is a JDK 26 compatibility run, not a JDK 21
  runtime result.
- Maven `verify` is blocked by uncached `maven-jar-plugin:3.4.2`; Maven `test` also
  encounters uncached `commons-lang3:3.12.0` for its resources plugin. The runtime
  Log4j-to-SLF4J bridge is not cached either. Resolve pinned dependencies on the
  build workstation; no full Maven/package success is claimed here.
- Both integration classes fail at their missing-configuration gate before any
  connection. The **13 real database tests have not run against MySQL 8.4** here.
  No integration configuration, MySQL 8.4 test server or JDK 21 was available.
- The packaging script parses and its real prerequisite check rejects installed
  JDK 26 before building/staging. No app image, EXE or MSI was generated here.
- Existing 28 Java/.form pairs and 30 GroupLayout definitions pass the contract
  checks. No UI forms were regenerated or removed for Phase 7.

## Release verification checklist

- [ ] Run `mvn clean verify` on JDK 21 with all pinned dependencies; retain reports.
- [ ] Provision the isolated MySQL 8.4/TLS server and run all 13 integration tests.
      Confirm the expected schema/UUID and restricted client grants.
- [ ] Review queue numbering/FIFO/audits and payment/clearance race results, not just
      a green Maven exit. Repeat on two physical LAN PCs and with network interruptions.
- [ ] Build the app image, pass its bundled-runtime installation check, then test
      the native launcher on a clean campus workstation with no Java installed.
- [ ] Confirm first-run template location, edit preservation, writable logs/reports,
      verified campus TLS, and absence of real config/secrets in the distribution.
- [ ] Check all roles, student records, queue alerts, defer/requeue, TV full screen,
      clearance exceptions, PDF/Excel and physical receipt printing.
- [ ] Test EXE/MSI installation, shortcut/menu, increasing-version upgrade and
      uninstall. Configuration/backups must remain available outside the install.
- [ ] Verify release hashes, apply required signing, and complete a separate-server
      backup/restore drill before campus rollout.

References: [JUnit 5.14.2](https://docs.junit.org/5.14.2/release-notes.html),
[Maven Failsafe lifecycle](https://maven.apache.org/surefire/maven-failsafe-plugin/index.html),
[Oracle JDK 21 packaging](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html),
[jpackage options](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html).

Phase 7 implementation ends here. All seven phases now have source deliverables;
release readiness still requires the environment-dependent checks above.
