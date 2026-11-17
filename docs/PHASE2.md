# Phase 2 — database infrastructure, authentication and desktop shell

Design: one JDBC transaction boundary, immutable domain records, checked service
failures, BCrypt authentication and a Swing shell with a role-specific workspace
and a working account card. Login is a separate frame, disposed when MainFrame
opens. Every new visual class has its matching NetBeans `.form` file.

Assumptions: feature destinations appear as their real panels are implemented in
Phases 3–6. This phase exposes the workspace and account pages, with a sidebar
title selected from the user's current database role. It supplies no dummy
student, payment or queue screens. Password changes are included now so accounts
marked `must_change_password` can finish signing in safely.

## Implemented files

Paths below are relative to `C:/Users/User/Documents/NetBeansProjects/Queue/`.

| Area | Files |
| --- | --- |
| Startup | `src/main/java/app/Application.java`, existing `PasswordHashTool.java` |
| Configuration | `src/main/java/config/AppConfig.java`, `ConfigurationException.java`, `DatabasePool.java` |
| Transaction boundary | `src/main/java/dao/UnitOfWork.java`, `DaoContext.java`, `JdbcUnitOfWork.java` |
| JDBC infrastructure | `src/main/java/dao/BaseDao.java`, `SqlErrors.java` |
| Authentication queries | `src/main/java/dao/UserRepository.java`, `UserDao.java`, `LoginAccount.java` |
| Services | `src/main/java/service/AuthService.java`, `NavigationService.java` |
| Typed failures | `src/main/java/service/ServiceException.java`, `ValidationException.java`, `AuthenticationException.java`, `AuthorizationException.java`, `DataAccessException.java`, `ConflictException.java`, `TransactionConflictException.java`, `CommitUncertainException.java` |
| Frames | `src/main/java/ui/LoginFrame.java` + `LoginFrame.form`; `MainFrame.java` + `MainFrame.form` |
| Panels | `src/main/java/ui/panel/DashboardPanel.java` + `DashboardPanel.form`; `AccountPanel.java` + `AccountPanel.form` |
| Dialog | `src/main/java/ui/dialog/ChangePasswordDialog.java` + `ChangePasswordDialog.form` |
| Swing helpers | `src/main/java/ui/util/UiTasks.java`, `OrderedFocusTraversalPolicy.java`, `UiStyle.java` |

`src/main/java/model/` now contains:

- Records: `User`, `Student`, `Organization`, `Term`, `Fine`, `FineBalance`,
  `Payment`, `QueueCounter`, `QueueTicket`, `ClearanceRecord`, `AuditLog`,
  `AccountProfile`, `Session`.
- Enums: `Role`, `OfficeCode`, `FineStatus`, `QueueStatus`, `ClearanceStatus`,
  `AuditAction`.

All record files have the corresponding `.java` extension. Records use
`BigDecimal`, `LocalDate` and `LocalDateTime`; JDBC types stay in `dao`. Password
hashes are absent from the domain user, session display profile and form fields.
Credential projections and session tokens have redacted string representations.

## Behavior and transaction rules

Authentication reads the candidate account and runs BCrypt outside a row lock.
A successful match is followed by one write transaction: lock the account,
recheck its hash, role/linked-record eligibility and active flags, then record
`last_login_at` with the database clock. Phase 1 triggers audit that update using
the authenticated user's ID. A concurrent password reset or deactivation before
the lock prevents the login. Unknown accounts receive the same message and a
dummy BCrypt check. Password input arrays are erased on success and failure.

The opaque local session registry determines identity; services ignore any role
supplied in a caller's display profile. `AuthService.require(context, session,
roles...)` is the authorization entry point for later services, inside their own
transaction callback. It reloads current role and assignment eligibility and
detects password changes made on another workstation. Logout revokes the local
token immediately. Logout itself has no database write, so it creates no
synthetic audit row; a dedicated session/event audit model would be a separate
schema decision.

Password changes require the current password and a new, nonblank password of at
least 12 characters, within BCrypt's 72 UTF-8 byte limit. They recheck the old
credential under the row lock, update the hash, and clear `must_change_password`
atomically. The service blocks ordinary protected operations until a mandatory
change is complete. Other sessions detect the changed hash on their next
protected operation. A cancelled mandatory dialog revokes the new local session.

`JdbcUnitOfWork` rejects database access on the EDT. It verifies MySQL 8.4,
sets strict SQL mode and `+08:00`, uses READ COMMITTED, and binds audit context
through prepared statements. Work uses one connection until commit or rollback.
The base DAO refuses writes and row locks in read callbacks. Audit variables
are reset in `finally`; a connection whose rollback/reset fails is evicted.
Cleanup does not turn a confirmed successful commit into an apparent failed
write. A failed commit returns `CommitUncertainException` and is never retried
automatically. Deadlock and lock-timeout failures have a distinct typed failure;
future services may retry their entire rolled-back operation deliberately.

Pool startup, database calls and password hashing execute in background workers.
Forms disable their controls until each operation finishes. A busy login or
password dialog cannot be closed mid-request. UI messages contain no stack
traces; diagnostic exceptions go to the rolling application log.

The shell uses GroupLayout with a CardLayout content panel. The forms supply
label associations, keyboard traversal, default buttons, masked passwords,
Escape-to-cancel, status text plus color, resize constraints and bottom-right
primary actions. User-provided label text is rendered as plain text rather than
interpreted as Swing HTML. There are no JTables in Phase 2; custom table models
start with the actual management screens in Phase 3.

## Run

1. Complete the Phase 1 database installation. **No new SQL migration is needed.**
2. Fill the external `app.properties` with the actual database host, restricted
   application user and password. The checked-in example has a deliberately
   empty password; startup rejects it until configured. The JDBC URL must retain
   `sslMode=VERIFY_IDENTITY`; configure the JVM truststore for the host certificate.
3. In a terminal using **JDK 21** and Maven **3.9.6+ (3.x)**, from the project root:

   ```powershell
   java -version
   mvn -version
   mvn clean verify
   java -Dqueueclearance.config=app.properties -jar target/queue-clearance.jar
   ```

The JAR manifest uses `app.Application` and loads dependencies from the adjacent
`target/lib/` directory. Keep that directory beside the JAR. Configuration is
resolved relative to the process working directory unless an absolute path is
passed through `-Dqueueclearance.config`. Credentials are not packaged in the JAR.

In NetBeans, open the Maven project, select JDK 21 and run `app.Application`.
Open each of the five `.java` files listed above and select **Design**. Modify
layout in the GUI Builder; retain generated `initComponents`/variables guards
and put application behavior outside those blocks. The no-argument designer
constructors build views without database calls; their action buttons remain
disabled until the runtime constructor/configuration injects services.

Sign in as `admin` with the password chosen during Phase 1 installation. The
seed does not provide a shared default password. Other real accounts can be
created through the management screens in Phase 3.

## Verification performed here

- Java 21 compatibility compilation passed for 58 project source files using
  the available NetBeans JDK 26 compiler with `--release 21`. Local cached APIs
  were jBCrypt 0.4, FlatLaf 3.5.4 and SLF4J 1.7.36. This is a partial compatibility
  check, not a build against the pinned dependency set. `DatabasePool`,
  `JdbcUnitOfWork` and `Application` require uncached HikariCP and were excluded.
- 57 in-memory smoke assertions passed for all four roles, credential rejection,
  password-array erasure, forged profile rejection, logout, mandatory password
  change, password-reset session invalidation, a deactivation race and external
  configuration. These checks use a repository test double, not a MySQL server.
- All five XML/source pairs passed structural checks: matching component
  declarations, both layout dimensions, event handlers and generated guards.
- The installed NetBeans layout parser loaded all **six** GroupLayout layouts
  in those five `.form` files, including the nested sidebar layout.
- Offscreen rendering and visual review covered login, error state, account,
  password dialog and shell at regular and wider sizes.
- Source inspection verified that `java.sql` imports occur only in `dao` and
  there are no raw `Statement` calls.

Full Maven verification was attempted offline and stopped at an uncached
`maven-enforcer-plugin:3.6.2`. Network downloads are restricted here, and the
available bundled JDK is 26 rather than 21. MySQL integration and a live
NetBeans Design-view interaction remain to be checked in the deployment
environment. The existing XAMPP client is MariaDB 10.4, not the required MySQL 8.4.
The scheduled JUnit suite and concurrency integration tests remain in Phase 7.

## Manual checklist

- [ ] `mvn clean verify` succeeds with JDK 21 and the pinned dependencies.
- [ ] The packaged JAR launches and reads the intended external properties file.
- [ ] All five forms open in NetBeans Design view and resize correctly.
- [ ] Valid admin login opens Administration; a bad password shows a plain error.
- [ ] The UI remains responsive during a database outage; controls recover after failure.
- [ ] Successful login updates database `last_login_at` and creates its audit entry.
- [ ] Workspace/account navigation works with Tab, Shift+Tab and Enter.
- [ ] Password confirmation mismatch is rejected, and Escape cancels safely.
- [ ] Changing a password rejects the old password and accepts the new one.
- [ ] An account with `must_change_password = TRUE` cannot bypass its dialog.
- [ ] Logout returns to a cleared login form and invalidates the old local session.
- [ ] On a second LAN PC, changed account roles, deactivation or password reset
  take effect at the next protected request.
- [ ] No password hash is present in audit snapshots, UI messages or exported profiles.

Phase 2 stops here. Say **next** for Phase 3: students, offices and users.

References: [FlatLaf customization](https://www.formdev.com/flatlaf/how-to-customize/),
[HikariCP](https://github.com/brettwooldridge/HikariCP),
[MySQL locking reads](https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html).
