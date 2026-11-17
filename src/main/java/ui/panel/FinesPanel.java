package ui.panel;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import model.*;
import service.*;
import ui.dialog.*;
import ui.model.FineTableModel;
import ui.util.*;

public final class FinesPanel extends javax.swing.JPanel {
    private final FineTableModel rows = new FineTableModel();
    private FineService service; private PaymentService payments; private TermService terms; private Session session;
    private FinanceReferences references = new FinanceReferences(List.of(), List.of());
    private Consumer<Boolean> activity = value -> {}; private boolean busy; private boolean loaded; private int page; private boolean hasNext;
    public FinesPanel() {
        initComponents(); TableSupport.configure(tblFines, rows, "Student fines", 5);
        tblFines.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        TableSupport.widths(tblFines, 128, 72, 112, 112, 112, 96, 240, 136, 180);
        tblFines.setDefaultRenderer(java.math.BigDecimal.class, new MoneyCellRenderer());
        DefaultComboBoxModel<String> statuses = new DefaultComboBoxModel<>(); statuses.addElement("All statuses");
        for (FineStatus status : FineStatus.values()) { statuses.addElement(status.displayName()); } cboStatus.setModel(statuses);
        txtStudentNumber.addActionListener(event -> refresh()); tblFines.getSelectionModel().addListSelectionListener(event -> actions());
        lblStudentNumber.setLabelFor(txtStudentNumber); lblOffice.setLabelFor(cboOffice); lblTerm.setLabelFor(cboTerm); lblFineStatus.setLabelFor(cboStatus);
        UiStyle.primary(btnPrimary); actions();
    }
    public void configure(FineService service, PaymentService payments, TermService terms, Session session, Consumer<Boolean> activity) {
        this.service = service; this.payments = payments; this.terms = terms; this.session = session; this.activity = activity;
        Role role = session.user().role(); btnTerms.setVisible(role == Role.ADMIN); btnEdit.setVisible(role != Role.CASHIER);
        btnWaive.setVisible(role != Role.CASHIER); btnPay.setVisible(role == Role.ADMIN); btnPrimary.setText(role == Role.CASHIER ? "Record payment" : "Assess fine"); actions();
    }
    public void focusSearch() { SwingUtilities.getRootPane(this).setDefaultButton(btnSearch); txtStudentNumber.requestFocusInWindow(); }
    public void reload() { refresh(); }
    private void setBusy(boolean value) { busy = value; activity.accept(value); }
    private void refresh() {
        if (busy || service == null) { return; }
        Long office = FinanceUi.id(cboOffice), term = FinanceUi.id(cboTerm); setBusy(true); UiStyle.status(lblStatus, "Loading offices and terms...", false);
        UiTasks.run(this, () -> service.references(session), data -> {
            references = data; loaded = true; FinanceUi.offices(cboOffice, data.offices(), true); FinanceUi.terms(cboTerm, data.terms(), true);
            FinanceUi.select(cboOffice, office); FinanceUi.select(cboTerm, term); setBusy(false); query(0);
        }, error -> { loaded = false; rows.setRows(List.of()); setBusy(false); actions(); UiStyle.status(lblStatus, error.getMessage(), true); },
                txtStudentNumber, cboOffice, cboTerm, cboStatus, btnSearch, btnTerms, btnPrevious, btnNext, btnEdit, btnWaive, btnPay, btnPrimary, tblFines);
    }
    private void query(int requested) {
        if (busy || !loaded) { return; }
        FineFilter filter = new FineFilter(txtStudentNumber.getText(), FinanceUi.id(cboOffice), FinanceUi.id(cboTerm),
                cboStatus.getSelectedIndex() <= 0 ? null : FineStatus.values()[cboStatus.getSelectedIndex() - 1], requested, 25);
        setBusy(true); UiStyle.status(lblStatus, "Loading fines...", false);
        UiTasks.run(this, () -> service.search(session, filter), result -> {
            rows.setRows(result.items()); page = result.number(); hasNext = result.hasNext(); setBusy(false); actions(); lblPage.setText("Page " + (page + 1)); UiStyle.status(lblStatus, result.items().size() + " fines shown", false);
        }, error -> { rows.setRows(List.of()); hasNext = false; setBusy(false); actions(); UiStyle.status(lblStatus, error.getMessage(), true); },
                txtStudentNumber, cboOffice, cboTerm, cboStatus, btnSearch, btnTerms, btnPrevious, btnNext, btnEdit, btnWaive, btnPay, btnPrimary, tblFines);
    }
    private FineRow selected() { int i = tblFines.getSelectedRow(); return i < 0 ? null : rows.rowAt(tblFines.convertRowIndexToModel(i)); }
    private void actions() {
        FineRow row = selected(); boolean writable = session != null && session.user().role() != Role.CASHIER;
        boolean payable = row != null && row.fine().waivedAt() == null && row.balance().signum() > 0;
        btnSearch.setEnabled(!busy && service != null); btnTerms.setEnabled(!busy && terms != null && session != null && session.user().role() == Role.ADMIN);
        btnPrevious.setEnabled(!busy && page > 0); btnNext.setEnabled(!busy && hasNext);
        btnEdit.setEnabled(!busy && loaded && writable && row != null && row.fine().waivedAt() == null);
        btnWaive.setEnabled(!busy && loaded && writable && payable); btnPay.setEnabled(!busy && loaded && payable);
        btnPrimary.setEnabled(!busy && loaded && (writable || payable));
    }
    private void edit(boolean create) {
        if (busy || service == null || (!create && selected() == null)) { return; }
        FineEditorDialog dialog = new FineEditorDialog(SwingUtilities.getWindowAncestor(this), service, session, create ? null : selected(), references);
        if (create) { dialog.selectContext(FinanceUi.id(cboOffice), FinanceUi.id(cboTerm)); }
        dialog.setVisible(true); if (dialog.savedFine().isPresent()) { query(0); }
    }
    private void pay() {
        FineRow row = selected(); if (busy || row == null || payments == null) { return; }
        RecordPaymentDialog dialog = new RecordPaymentDialog(SwingUtilities.getWindowAncestor(this), payments, session, row);
        dialog.setVisible(true);
        if (dialog.savedPayment().isPresent()) {
            new ReceiptDialog(SwingUtilities.getWindowAncestor(this), payments, session, dialog.savedPayment().orElseThrow().id()).setVisible(true); query(page);
        }
    }
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        refresh();
    }//GEN-LAST:event_btnSearchActionPerformed
    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        query(page - 1);
    }//GEN-LAST:event_btnPreviousActionPerformed
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        query(page + 1);
    }//GEN-LAST:event_btnNextActionPerformed
    private void btnPrimaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrimaryActionPerformed
        if (session != null && session.user().role() == Role.CASHIER) { pay(); } else { edit(true); }
    }//GEN-LAST:event_btnPrimaryActionPerformed
    private void btnPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayActionPerformed
        pay();
    }//GEN-LAST:event_btnPayActionPerformed
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        edit(false);
    }//GEN-LAST:event_btnEditActionPerformed
    private void btnWaiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWaiveActionPerformed
        FineRow row = selected(); if (busy || row == null) { return; }
        ReasonActionDialog dialog = new ReasonActionDialog(SwingUtilities.getWindowAncestor(this), "Waive fine", row.studentNumber() + " - " + row.studentName() + "\n" + row.officeCode() + " / " + row.termCode() + "\nRemaining balance: " + UiStyle.pesos(row.balance()) + "\nWaivers are final in this application.", "Waive balance", reason -> service.waive(session, row, reason));
        dialog.setVisible(true); if (dialog.completed()) { query(page); }
    }//GEN-LAST:event_btnWaiveActionPerformed
    private void btnTermsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTermsActionPerformed
        if (busy || terms == null) { return; }
        setBusy(true);
        UiTasks.run(this, () -> terms.list(session), data -> {
            setBusy(false); actions(); TermSettingsDialog dialog = new TermSettingsDialog(SwingUtilities.getWindowAncestor(this), terms, session, data);
            dialog.setVisible(true); if (dialog.savedTerm().isPresent()) { refresh(); }
        }, error -> { setBusy(false); actions(); UiStyle.status(lblStatus, error.getMessage(), true); },
                txtStudentNumber, cboOffice, cboTerm, cboStatus, btnSearch, btnTerms, btnPrevious, btnNext, btnEdit, btnWaive, btnPay, btnPrimary, tblFines);
    }//GEN-LAST:event_btnTermsActionPerformed
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblStudentNumber = new javax.swing.JLabel();
        txtStudentNumber = new javax.swing.JTextField();
        lblFineStatus = new javax.swing.JLabel();
        cboStatus = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        lblOffice = new javax.swing.JLabel();
        cboOffice = new javax.swing.JComboBox<>();
        lblTerm = new javax.swing.JLabel();
        cboTerm = new javax.swing.JComboBox<>();
        pnlTable = new javax.swing.JScrollPane();
        tblFines = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnTerms = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnWaive = new javax.swing.JButton();
        btnPay = new javax.swing.JButton();
        btnPrimary = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(672, 600));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFineStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboStatus, 136, 136, 136))
            .addGap(8)
            .addComponent(btnSearch, 96, 96, 96))
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnTerms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnWaive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnPay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnPrimary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudentNumber, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblFineStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboStatus, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addGap(28)
            .addComponent(btnSearch, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboOffice, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboTerm, 36, 36, 36)))
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnPrevious, 40, 40, 40)
            .addComponent(btnNext, 40, 40, 40)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnTerms, 40, 40, 40))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnEdit, 40, 40, 40)
            .addComponent(btnWaive, 40, 40, 40)
            .addComponent(btnPay, 40, 40, 40)
            .addComponent(btnPrimary, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("Fines");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblStudentNumber.setText("Student number (exact)");
        lblStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setColumns(24);
        lblFineStatus.setText("Status");
        lblFineStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboStatus.setMinimumSize(new java.awt.Dimension(0, 36));
        btnSearch.setText("Search");
        btnSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        lblOffice.setText("Office");
        lblOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setMinimumSize(new java.awt.Dimension(0, 36));
        lblTerm.setText("Term");
        lblTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setMinimumSize(new java.awt.Dimension(0, 36));
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 140));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblFines);
        tblFines.setModel(new ui.model.FineTableModel());
        tblFines.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblFines.setRowHeight(32);
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblPage.setText("Page 1");
        lblPage.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrevious.setText("Previous");
        btnPrevious.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });
        btnNext.setText("Next");
        btnNext.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        btnTerms.setText("Term settings");
        btnTerms.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnTerms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTermsActionPerformed(evt);
            }
        });
        btnEdit.setText("Edit");
        btnEdit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        btnWaive.setText("Waive balance");
        btnWaive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnWaive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWaiveActionPerformed(evt);
            }
        });
        btnPay.setText("Record payment");
        btnPay.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPayActionPerformed(evt);
            }
        });
        btnPrimary.setText("Assess fine");
        btnPrimary.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrimary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrimaryActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblStudentNumber;
    private javax.swing.JTextField txtStudentNumber;
    private javax.swing.JLabel lblFineStatus;
    private javax.swing.JComboBox<java.lang.String> cboStatus;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JComboBox<ui.model.IdChoice> cboOffice;
    private javax.swing.JLabel lblTerm;
    private javax.swing.JComboBox<ui.model.IdChoice> cboTerm;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblFines;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblPage;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnTerms;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnWaive;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnPrimary;
    // End of variables declaration//GEN-END:variables
}
