# Phase 3 — students, offices and users

Design: three management panels in the existing MainFrame, three owned modal
edit dialogs, typed read-only table models, paged JDBC queries, service-level
authorization and row-locked writes. Every visual class has a matching NetBeans
`.java`/`.form` pair with GroupLayout and guarded generated code.

Assumptions: administrators maintain students and user accounts. Officers and
cashiers can search students. An officer can see and change queue availability
only for their own office. The seven office codes remain fixed; administrators
can edit office names and active status. No office is deleted or replaced.

## Permissions

| Action | Administrator | Officer | Cashier | Student |
| --- | --- | --- | --- | --- |
| Search/filter students | Yes | Yes | Yes | No |
| Add/edit/reactivate/deactivate students | Yes | No | No | No |
| View offices | All seven | Assigned office | No | No |
| Open/close an office queue | All seven | Assigned office | No | No |
| Edit office name/active status | Yes | No | No | No |
| Manage users, roles, assignments and password resets | Yes | No | No | No |
| Workspace/My account | Yes | Yes | Yes | Yes |

Students' own ticket/clearance screens remain scheduled for Phase 5/6. These
management APIs do not expose a directory of other students to student accounts.
Sidebar visibility follows this matrix, and each service independently checks
the current session and database role. Knowing a record ID or calling a service
directly does not grant another role's permissions.

## Screen files

All paths are relative to `C:/Users/User/Documents/NetBeansProjects/Queue/`:

| Screen | Java | NetBeans form |
| --- | --- | --- |
| Student search | `src/main/java/ui/panel/StudentsPanel.java` | `src/main/java/ui/panel/StudentsPanel.form` |
| Student editor | `src/main/java/ui/dialog/StudentEditorDialog.java` | `src/main/java/ui/dialog/StudentEditorDialog.form` |
| Offices | `src/main/java/ui/panel/OfficesPanel.java` | `src/main/java/ui/panel/OfficesPanel.form` |
| Office editor | `src/main/java/ui/dialog/OfficeEditorDialog.java` | `src/main/java/ui/dialog/OfficeEditorDialog.form` |
| User search | `src/main/java/ui/panel/UsersPanel.java` | `src/main/java/ui/panel/UsersPanel.form` |
| User editor | `src/main/java/ui/dialog/UserEditorDialog.java` | `src/main/java/ui/dialog/UserEditorDialog.form` |
| Updated shell | `src/main/java/ui/MainFrame.java` | `src/main/java/ui/MainFrame.form` |

Supporting files:

- `src/main/java/service/StudentService.java`, `OfficeService.java`,
  `UserService.java`, `Validation.java`, `RecordChangedException.java`,
  `NotFoundException.java`; updated `AuthService.java` and `NavigationService.java`.
- `src/main/java/dao/StudentRepository.java`, `StudentDao.java`,
  `OrganizationRepository.java`, `OrganizationDao.java`, `SearchSql.java`;
  updated `BaseDao.java`, `UserRepository.java`, `UserDao.java`, `DaoContext.java`
  and `JdbcUnitOfWork.java`.
- `src/main/java/model/Page.java`, `StudentFilter.java`, `StudentDraft.java`,
  `OfficeDraft.java`, `UserFilter.java`, `UserDraft.java`, `UserAccount.java`.
- `src/main/java/ui/model/RecordTableModel.java`, `StudentTableModel.java`,
  `OfficeTableModel.java`, `UserTableModel.java`.
- `src/main/java/ui/util/PlainTextCellRenderer.java`, `StatusCellRenderer.java`,
  `TableSupport.java`, `DialogSupport.java`.
- Updated `src/main/java/app/Application.java` wires all three services into the
  application shell using the existing shared transaction infrastructure.
- `database/migrations/V003__user_administration_index.sql` and
  `database/verify_phase3.sql`.

## Behavior

Student search accepts a student number or name substring, exact program and
section filters, year 1–6, and Active/Inactive/All status. Inputs are trimmed.
Search uses the database's case/accent-insensitive collation. `%`, `_` and `!`
in search text are literal characters, escaped for SQL LIKE matching. Results
are fetched in pages of 25 with one extra row to determine whether Next is
available. No unbounded student/user directory is loaded into Swing.

Students have required number, first/last name, program, year and section.
Middle name and suffix are optional. Duplicate student numbers are rejected.
Uncheck **Active student** to deactivate, and find inactive records through the
status filter to reactivate them. Deactivation retains all related history and
blocks linked student logins; it does not rewrite the separate user's active
flag. A student with waiting/called/serving/deferred tickets must have those
tickets completed or cancelled first.

Office codes cannot be changed, added or deleted through the application.
An administrator can rename an office, change queue availability, deactivate
it or reactivate it. An officer sees only their assigned office and can change
only **Open for new tickets**. Closing a queue stops future issuance; it does
not remove waiting tickets. Deactivation requires no active tickets and a closed
queue. The editor closes the queue checkbox when Active office is unchecked.

User search supports username, display name, student number or office code,
plus role and active-status filters. An administrator can create, edit,
deactivate or reactivate an account, change its role/assignment, require a
password change, and set a temporary password. Usernames use 3–64 ASCII letters,
digits, dots, underscores or hyphens, beginning with a letter or digit.

Student accounts need an existing student number and reserve that record even
when the account is inactive. Reuse/reactivate an existing account rather than
creating a second login for that student. Officer accounts need one office.
Administrators and cashiers have no student/office binding. New or newly active
assignments must reference an active student/office. Existing inactive accounts
may retain their old inactive assignment without being enabled for login.

Initial/reset passwords require at least 12 characters within 72 UTF-8 bytes,
are hashed at the configured BCrypt cost, and always require a change at next
sign-in. Password arrays and password fields are cleared after submission.
Leave both password fields empty when editing to keep the existing password.
An existing mandatory change cannot be cleared through metadata editing.
Password hashes are not selected by user-list queries and never enter table
models or display profiles.

An administrator cannot deactivate/demote their own account or reset their own
password through user management; **My account** supplies the password-change
workflow. Removing the last active administrator is rejected. A role/assignment
change or password reset invalidates old sessions on their next protected
request, including on another workstation. An officer with called/serving
tickets cannot be deactivated, demoted or reassigned until those tickets are
finished or requeued through the queue workflow.

## Concurrency and audit

Editors submit the database `updated_at` value they originally loaded. Services
lock the current target row and compare its server-generated timestamp before
saving. A stale editor receives a refresh message instead of overwriting a
newer change. A concurrent login can also change a user's timestamp; refresh the
user editor in that case. Database unique keys remain the final protection
against simultaneous duplicate numbers/usernames/student-account assignments.

Every write obtains its audit actor from live service authorization, and all
domain updates and trigger-generated audit rows share one transaction.
Deactivation is an UPDATE; nothing is deleted. A metadata update plus password
reset creates two audit rows under the same request ID and commits atomically.

User administration first locks active administrator rows in ascending ID
order, then rechecks the actor. It locks affected old/new offices in ascending
ID order, the new student assignment if applicable, and the target user row.
This serializes competing administrator-removal requests. BCrypt hashing takes
place before these locks, with authorization checked again for the final write.
Migration V003 adds the covering index for the administrator-lock query.

Login and password change lock the linked office/student before the user row,
then recheck the credential and assignment. The user-row lock no longer
implicitly locks joined assignment tables. Office edits authorize again after
acquiring the office lock so an officer cannot update a former office after a
concurrent reassignment. Called/serving-ticket checks for officer reassignment
run while holding the affected office locks, matching Phase 5's queue contract.

All queries, saves and password hashing run in SwingWorker. Search/edit/add/page
controls and navigation are disabled while work runs. An unsuccessful search
clears stale rows and restores controls. Selection is converted from sorted
view rows to model rows before opening an editor. Tables use custom
AbstractTableModel classes, readable status words plus color, and plain-text
renderers that disable Swing HTML interpretation for user-provided data.

## Apply and run

Do not rerun the original schema or seed. Using the **MySQL 8.4** client in a DBA
session, apply the new forward migration once:

```sql
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V003__user_administration_index.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase3.sql;
```

Verification columns should say PASS. V003 adds one online secondary index;
the ten tables, existing data, grants and 47 triggers remain in place. Runtime
application credentials still have no DDL or DELETE privileges.

With **JDK 21** and Maven **3.9.6+ (3.x)**, from the project root:

```powershell
mvn clean verify
java -Dqueueclearance.config=app.properties -jar target/queue-clearance.jar
```

Keep `target/lib/` beside the JAR. Use the configured TLS certificate/truststore
and external database credentials from Phase 2. Log in with the installation
administrator; create the real officer, cashier and student accounts through
**Users**. New accounts must complete the password-change dialog at first login.

Open each Java/form pair in NetBeans Design view after building. Retain
generated-code guards, and change layout through the GUI Builder. Business
logic, listeners and service calls remain outside `initComponents()`.

## Validation performed

- Java 21 compatibility compilation passed for **90 project source files**
  using `--release 21` and the available cached jBCrypt/FlatLaf/SLF4J APIs.
  The HikariCP-dependent `DatabasePool`, `JdbcUnitOfWork` and `Application`
  remain excluded from that partial check because HikariCP is not cached.
- **59** smoke assertions passed for normalization, duplicate rejection,
  read/write role permissions, paged filters, stale edits, deactivation guards,
  officer scope, account binding, password reset/erasure, administrator
  protection, assignment changes and table/editor behavior.
- **37** asynchronous UI flow assertions passed for all four role sidebars,
  real service calls through SwingWorker, navigation locking, background search,
  failure recovery and a complete background user-save flow. The repository
  test double rejects callbacks on the EDT.
- Offscreen renders were reviewed for all six new screens/dialogs and student
  search at multiple sizes. A visual check prompted explicit table-header
  setup and improved column sizing.
- The installed NetBeans layout parser accepted all **12** Free Design layouts
  across the **11** form files, including the updated sidebar. XML/source
  pairing, handlers, generated guards and both layout dimensions were checked.

These checks use in-memory repositories, not MySQL. Full Maven verification,
V003 execution, MySQL locking/integration behavior and a live Design-view edit
remain unverified here: the environment has JDK 26, restricted downloads and an
XAMPP MariaDB client rather than the required JDK 21/MySQL 8.4 setup. The full
JUnit and concurrency integration suite remains scheduled for Phase 7.

## Manual checklist

- [ ] Apply V003 once and confirm `verify_phase3.sql` reports PASS.
- [ ] Build/package on JDK 21 with the pinned dependencies.
- [ ] Open all six new pairs and MainFrame in NetBeans Design view.
- [ ] Add, search, edit, deactivate and reactivate a student; verify all filters.
- [ ] Enter in search fields triggers Search; Previous/Next fetch the next page.
- [ ] Sort a table and confirm Edit opens the selected record, not its old row index.
- [ ] Two editors of the same record reject the stale save.
- [ ] An officer sees only their office and cannot edit its code/name/active flag.
- [ ] A cashier can search students but cannot open Users or Offices.
- [ ] Student logins expose neither student-directory nor administration routes.
- [ ] Create each account role and verify its required/forbidden assignments.
- [ ] Duplicate usernames/student numbers/account bindings are rejected.
- [ ] New/reset passwords require a change; blank edit password fields preserve it.
- [ ] Self-deactivation/demotion/reset through Users is rejected.
- [ ] On two PCs, competing administrator removals leave an active administrator.
- [ ] Existing active ticket fixtures block student/office deactivation and
  called/serving tickets block officer reassignment/deactivation.
- [ ] Edits produce the correct audit actor/request, without password hashes.
- [ ] Tab/Shift+Tab reach controls; Ctrl+Tab exits a JTable; Escape cancels editors.
- [ ] A database outage shows a plain error and restores buttons/navigation.

Phase 3 stops here. Say **next** for Phase 4: fines, payments and receipts.
