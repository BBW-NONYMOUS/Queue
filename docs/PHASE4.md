# Phase 4 — fines, payments and printable receipts

Design: financial services own the transactions and authorization; one ledger
DAO supplies prepared statements on the existing transaction connection.
Fines/payments are JPanel cards in MainFrame. Editors, confirmations, archived
receipt preview and term settings are owned JDialog forms. All seven new visual
classes have matching NetBeans `.java` and `.form` files with guarded generated
code and GroupLayout. No new JFrame is introduced.

Assumptions: one payment pays one fine and has one globally unique receipt
reference. Administrators and officers maintain fines; officers are restricted
to their assigned office. Administrators/cashiers receive payments, and only
administrators void them. A waiver permanently forgives that fine's remaining
obligation; the application does not undo waivers or refund previous payments.

## Files

Paths are relative to `C:/Users/User/Documents/NetBeansProjects/Queue/`:

| Screen/dialog | Java and matching form |
| --- | --- |
| Fines | `src/main/java/ui/panel/FinesPanel.java`, `FinesPanel.form` |
| Payments | `src/main/java/ui/panel/PaymentsPanel.java`, `PaymentsPanel.form` |
| Assess/edit fine | `src/main/java/ui/dialog/FineEditorDialog.java`, `FineEditorDialog.form` |
| Record payment | `src/main/java/ui/dialog/RecordPaymentDialog.java`, `RecordPaymentDialog.form` |
| Waiver/void confirmation | `src/main/java/ui/dialog/ReasonActionDialog.java`, `ReasonActionDialog.form` |
| Archived receipt | `src/main/java/ui/dialog/ReceiptDialog.java`, `ReceiptDialog.form` |
| Term settings | `src/main/java/ui/dialog/TermSettingsDialog.java`, `TermSettingsDialog.form` |

Supporting implementation:

- `src/main/java/service/FineService.java`, `PaymentService.java`,
  `FinancialRules.java`, `Money.java`, `TermService.java`.
- `src/main/java/dao/FinanceRepository.java`, `FinanceDao.java`; extended
  `DaoContext.java` and `JdbcUnitOfWork.java` expose the ledger in one transaction.
- `src/main/java/model/FineDraft.java`, `FineRow.java`, `FineFilter.java`,
  `PaymentFilter.java`, `PaymentRow.java`, `ReceiptData.java`,
  `FinanceReferences.java`, `TermDraft.java`.
- `src/main/java/ui/model/FineTableModel.java`, `PaymentTableModel.java`,
  `IdChoice.java`; `src/main/java/ui/util/FinanceUi.java`, `MoneyCellRenderer.java`.
- `src/main/java/report/ReceiptPrintable.java` supplies native Java printing.
- Updated `Application.java`, `NavigationService.java`, MainFrame's Java/form
  pair, and shared plain-text/status table renderers wire the real screens in.
- `database/migrations/V004__archived_payment_receipts.sql` and
  `database/verify_phase4.sql`.

## Financial behavior

The Fines screen filters by exact student number, office, term and computed
status. Cashiers see balances and can record a payment; they cannot assess,
amend or waive fines. Officers see only their office. Administrators see all
offices and can open **Term settings**. Both financial tables fetch pages of 25,
sort typed decimal amounts numerically, and show right-aligned peso values with
two decimals. Balances/status are projections of non-voided payments, never
stored running totals. Secondary identity/detail columns remain horizontally
scrollable; full cell text is available through escaped tooltips.

New fines require an existing active student, active office and active term.
Student/office/term cannot be reassigned after assessment. An edit checks the
original server `updated_at` under the fine lock and cannot reduce the amount
below its non-voided payments. A waived fine cannot be edited. A waiver requires
a reason, explicit confirmation, a fresh reviewed balance and the appropriate
office permission. Existing payments remain in the ledger after waiver; waiver
does not return money. A later void on a waived fine does not reinstate the fine.

Money uses BigDecimal only. Entries must be positive, at most
9,999,999,999.99 PHP, and have at most two decimal places. Commas, currency
symbols and scientific notation are rejected in entry fields. No fractional
cent is silently rounded during a save.

From Fines, select the student's fine and choose **Record payment**. The dialog
shows the identity, reason and balance for review. Its default reference is a
globally unique `RCPT-<UUID>`; an operator may replace it with a unique school
receipt reference. References are normalized to uppercase and retained even
after voiding. Payments take their amount/date/receiving user from the validated
entry, database clock and authenticated session respectively.

Each payment write uses one transaction in this order:

1. Authorize admin/cashier and resolve the fine's immutable student ID.
2. Lock the student row, then the fine with `SELECT ... FOR UPDATE`.
3. Recheck authorization and look up the receipt reference.
4. Sum non-voided payments **after** acquiring the locks; reject overpayment or
   a waived fine.
5. Insert the payment and its archived receipt details, read its server-generated
   timestamps, and commit with its trigger-generated audit entry.

An exact repeat of the same reference/fine/amount/receiving user returns the
existing non-voided payment without charging again. A different use of that
reference is rejected. The database's unique key protects simultaneous uses
across different students/workstations. The UI never automatically retries an
uncertain commit: Save is disabled and the operator is told to search Payments
for that reference before taking further action.

Payments can settle historical debts for an inactive student/office/term.
They do not automatically clear an office, even when a fine becomes paid.
The Payments screen searches current or archived student numbers and receipt
references, with office/term, recorded/voided status and inclusive date filters.
**Find fine to pay** navigates back to the fine-selection workflow.

Voiding requires an administrator, reason and confirmation. It locks the
student, fine and payment, rejects a second void, and updates only void metadata.
Payments are never deleted or rewritten. If a void or a fine assessment/amendment
creates an outstanding balance, an ordinary cleared office record is locked
and reopened to `on_hold` in the same transaction. An explicit existing balance
exception remains intact. A failure in that clearance update rolls back the
financial change and its audit rows.

## Receipts and printing

V004 archives student identity, office/term labels, fine reason/amount, receiving
user identity and the fine balance immediately after the payment. Those fields
are built from database rows inside the payment insert; decimal snapshots are
stored as exact strings. The payment's actual amount/reference/paid timestamp
remain immutable ledger fields. Later name, term-label or fine edits cannot
rewrite an earlier receipt. Receipt snapshots are immutable and included in
payment audit snapshots.

After a confirmed save, the application opens the archived receipt dialog.
The same dialog reprints an existing payment from Payments. Native printing
runs after commit, outside the database transaction. Printer cancellation or
failure does not reverse the payment; reprint its receipt instead of recording
another payment. The latest void state is fetched again immediately before
printing, and voided receipts show a clear VOID heading, date and reason.

The printable uses Segoe UI, wraps long text, right-aligns decimal amounts and
paginates for the printer's imageable area. Every continuation page repeats the
receipt reference/status and includes a page number. Paid/void dates come from
the database and are labelled +08:00. The balance on a receipt is explicitly the
balance **after that payment**, rather than a later current balance.

Phase 4 supplies native printing; PDF/Excel report export remains in Phase 6.
No automatic physical print job is sent during testing.

Existing pre-V004 payments are not changed or given invented historical details.
They remain searchable and voidable, but an archived receipt is unavailable for
a legacy row with no original snapshot. All new payment inserts must provide a
snapshot, enforced by the replacement insert trigger.

## Term setup

The Phase 1 seed term was intentionally inactive. In Fines, an administrator
can open **Term settings**, select it, confirm the registrar's dates, and mark it
Active/Current; alternatively create the correct term. Dates are entered as
YYYY-MM-DD. Active/current flags are operator-controlled, not automatically
derived from dates, so a clearance period can remain open after classes end.

Term settings lock the catalogue, maintain at most one current term, reject
duplicate codes and stale edits, and prevent closing/switching a term with active
tickets. A term with fine/queue/clearance records keeps its code and date range;
create a different term instead of changing those historical identifiers.
Future queue issuance must lock/revalidate its current term within the issue
transaction so a concurrent term switch cannot issue against a stale period.

## Migration and run

Apply V004 **once**, after V003, as the permanent schema-owner/DBA account, with
all desktop clients stopped. MySQL DDL implicitly commits; keep clients stopped
until the column and all four complete replacement triggers are installed.
Take a verified backup first. This migration leaves the ten tables and the total
of 47 triggers intact and requires no new runtime account privileges.

At a MySQL **8.4** client prompt:

```sql
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/migrations/V004__archived_payment_receipts.sql;
SOURCE C:/Users/User/Documents/NetBeansProjects/Queue/database/verify_phase4.sql;
```

Verification columns should say PASS; the legacy payment count is informational.
The old Phase 1 transactional smoke script predates the required receipt archive;
use the Phase 4 verification script after V004 instead of rerunning that old
payment-insert fixture.

With JDK **21** and Maven **3.9.6+ (3.x)**, from the project root:

```powershell
mvn clean verify
java -Dqueueclearance.config=app.properties -jar target/queue-clearance.jar
```

Keep `target/lib/` next to the JAR and retain the external TLS/database settings.
Log in as admin, confirm the term settings, assess a fine, then collect it as
admin/cashier. New account passwords still require their first-login change.

## Verification and limits

The Java 21 compatibility check passed for **118 project source files** using
the cached library APIs. **51 financial smoke assertions**, **44 financial UI
flow assertions**, and the **96 earlier regression assertions** passed. A
prepared-statement test double also checked all **27 FinanceDao executions**
for complete, in-range parameter bindings; it does not parse or execute MySQL.
NetBeans' layout parser accepted **19** Free Design layouts across **18** paired
forms, and offscreen visual review covered the new screens and receipt pages.

HikariCP-dependent `DatabasePool`, `JdbcUnitOfWork` and
`Application` remain excluded because that dependency is not cached here.
The environment has the bundled JDK 26 and XAMPP MariaDB, rather than the required
JDK 21/MySQL 8.4 setup; full Maven verification, V004 execution and actual MySQL
isolation/locking behavior remain to be verified on the target setup.

Financial smoke checks use in-memory repositories, assert student-before-fine
locking, simulate competing payments and exercise rollback after a clearance
failure. UI-flow checks exercise role routes, background saves, confirmation,
uncertain-commit handling, term setup and receipt preview. Receipt pages were
rendered offscreen at A4 and narrow printer widths. NetBeans' installed layout
parser and XML/source pairing checks validate the forms. These checks do not
replace the MySQL concurrency/JUnit suite scheduled for Phase 7, a live Design
view edit, or a physical printer test.

## Manual checklist

- [ ] Back up, stop clients, apply V004 once and confirm verification PASS results.
- [ ] Build on JDK 21 with all pinned dependencies; open the seven new pairs in Design view.
- [ ] Confirm/activate a term; assess and edit a fine in the permitted office.
- [ ] Test partial, exact-final and rejected excessive payments.
- [ ] Retry an identical receipt reference; verify no duplicate payment is created.
- [ ] On two PCs, submit competing payments and verify the fine is never overpaid.
- [ ] Check waiver reason/confirmation and rejection when its reviewed balance changed.
- [ ] Verify only administrators can void and that a receipt reference cannot be reused afterward.
- [ ] Confirm debt changes reopen ordinary clearances and preserve recorded balance exceptions.
- [ ] Change a student's name/fine reason and verify old receipt details remain unchanged.
- [ ] Reprint active/voided receipts, including a long reason on narrow paper.
- [ ] Cancel/fail a print, then reprint the original payment rather than charging again.
- [ ] Check audit actor/request/timestamps for fine, waiver, payment, void and term writes.
- [ ] Confirm officers cannot collect payments and cashiers cannot amend/waive fines.

Phase 4 stops here. Say **next** for Phase 5: queue engine and screens, student
ticket flow, polling alerts and the TV display board.

Reference: [MySQL 8.4 JSON functions](https://dev.mysql.com/doc/refman/8.4/en/json-function-reference.html).
