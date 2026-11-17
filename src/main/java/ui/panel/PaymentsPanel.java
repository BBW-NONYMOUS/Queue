package ui.panel;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import model.*;
import service.*;
import ui.dialog.*;
import ui.model.PaymentTableModel;
import ui.util.*;

public final class PaymentsPanel extends javax.swing.JPanel {
    private final PaymentTableModel rows = new PaymentTableModel();
    private PaymentService service; private Session session; private Consumer<Boolean> activity = value -> {}; private Runnable findFine;
    private boolean busy; private boolean loaded; private int page; private boolean hasNext;
    public PaymentsPanel() {
        initComponents(); TableSupport.configure(tblPayments, rows, "Payment ledger", 4); tblPayments.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        TableSupport.widths(tblPayments, 224, 112, 112, 168, 88, 80, 136, 180); tblPayments.setDefaultRenderer(java.math.BigDecimal.class, new MoneyCellRenderer());
        cboState.setModel(new DefaultComboBoxModel<>(new String[]{"All payments", "Recorded", "Voided"}));
        txtSearch.addActionListener(event -> refresh()); txtFrom.addActionListener(event -> search(0)); txtThrough.addActionListener(event -> search(0));
        lblSearch.setLabelFor(txtSearch); lblOffice.setLabelFor(cboOffice); lblTerm.setLabelFor(cboTerm); lblState.setLabelFor(cboState); lblFrom.setLabelFor(txtFrom); lblThrough.setLabelFor(txtThrough);
        tblPayments.getSelectionModel().addListSelectionListener(event -> actions()); UiStyle.primary(btnFindFine); actions();
    }
    public void configure(PaymentService service, Session session, Consumer<Boolean> activity, Runnable findFine) {
        this.service = service; this.session = session; this.activity = activity; this.findFine = findFine; btnVoid.setVisible(session.user().role() == Role.ADMIN); actions();
    }
    public void focusSearch() { SwingUtilities.getRootPane(this).setDefaultButton(btnSearch); txtSearch.requestFocusInWindow(); }
    private void setBusy(boolean value) { busy = value; activity.accept(value); }
    public void reload() { refresh(); }
    private void refresh() {
        if (busy || service == null) { return; }
        Long office = FinanceUi.id(cboOffice), term = FinanceUi.id(cboTerm); setBusy(true); UiStyle.status(lblStatus, "Loading payment filters...", false);
        UiTasks.run(this, () -> service.references(session), refs -> {
            loaded = true; FinanceUi.offices(cboOffice, refs.offices(), true); FinanceUi.terms(cboTerm, refs.terms(), true);
            FinanceUi.select(cboOffice, office); FinanceUi.select(cboTerm, term); setBusy(false); search(0);
        }, error -> { loaded = false; rows.setRows(List.of()); setBusy(false); actions(); UiStyle.status(lblStatus, error.getMessage(), true); },
                txtSearch, cboOffice, cboTerm, cboState, txtFrom, txtThrough, btnSearch, btnPrevious, btnNext, btnReceipt, btnVoid, btnFindFine, tblPayments);
    }
    private void search(int requested) {
        if (busy || !loaded) { return; }
        PaymentFilter filter;
        try { filter = new PaymentFilter(txtSearch.getText(), FinanceUi.id(cboOffice), FinanceUi.id(cboTerm), cboState.getSelectedIndex() == 0 ? null : cboState.getSelectedIndex() == 2,
                FinanceUi.date(txtFrom.getText()), FinanceUi.date(txtThrough.getText()), requested, 25); }
        catch (ServiceException ex) { UiStyle.status(lblStatus, ex.getMessage(), true); return; }
        setBusy(true); UiStyle.status(lblStatus, "Loading payments...", false);
        UiTasks.run(this, () -> service.search(session, filter), result -> {
            rows.setRows(result.items()); page = result.number(); hasNext = result.hasNext(); setBusy(false); actions(); lblPage.setText("Page " + (page + 1)); UiStyle.status(lblStatus, result.items().size() + " payments shown", false);
        }, error -> { rows.setRows(List.of()); hasNext = false; setBusy(false); actions(); UiStyle.status(lblStatus, error.getMessage(), true); },
                txtSearch, cboOffice, cboTerm, cboState, txtFrom, txtThrough, btnSearch, btnPrevious, btnNext, btnReceipt, btnVoid, btnFindFine, tblPayments);
    }
    private PaymentRow selected() { int i = tblPayments.getSelectedRow(); return i < 0 ? null : rows.rowAt(tblPayments.convertRowIndexToModel(i)); }
    private void actions() {
        PaymentRow row = selected(); btnSearch.setEnabled(!busy && service != null); btnPrevious.setEnabled(!busy && page > 0); btnNext.setEnabled(!busy && hasNext);
        btnReceipt.setEnabled(!busy && row != null && row.archivedReceipt());
        btnVoid.setEnabled(!busy && row != null && row.payment().voidedAt() == null && session != null && session.user().role() == Role.ADMIN);
        btnFindFine.setEnabled(!busy && findFine != null);
    }
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        refresh();
    }//GEN-LAST:event_btnSearchActionPerformed
    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        search(page - 1);
    }//GEN-LAST:event_btnPreviousActionPerformed
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        search(page + 1);
    }//GEN-LAST:event_btnNextActionPerformed
    private void btnReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceiptActionPerformed
        PaymentRow row = selected(); if (!busy && row != null && service != null) { new ReceiptDialog(SwingUtilities.getWindowAncestor(this), service, session, row.payment().id()).setVisible(true); }
    }//GEN-LAST:event_btnReceiptActionPerformed
    private void btnVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoidActionPerformed
        PaymentRow row = selected(); if (busy || row == null || service == null) { return; }
        ReasonActionDialog dialog = new ReasonActionDialog(SwingUtilities.getWindowAncestor(this), "Void payment", "Receipt: " + row.payment().receiptReference() + "\n" + row.studentNumber() + " - " + row.studentName() + "\nAmount: " + UiStyle.pesos(row.payment().amount()) + "\nThis removes the payment from balance calculations.", "Void payment", reason -> service.voidPayment(session, row.payment().id(), reason));
        dialog.setVisible(true); if (dialog.completed()) { search(page); }
    }//GEN-LAST:event_btnVoidActionPerformed
    private void btnFindFineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindFineActionPerformed
        if (!busy && findFine != null) { findFine.run(); }
    }//GEN-LAST:event_btnFindFineActionPerformed
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblState = new javax.swing.JLabel();
        cboState = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        lblOffice = new javax.swing.JLabel();
        cboOffice = new javax.swing.JComboBox<>();
        lblTerm = new javax.swing.JLabel();
        cboTerm = new javax.swing.JComboBox<>();
        lblFrom = new javax.swing.JLabel();
        txtFrom = new javax.swing.JTextField();
        lblThrough = new javax.swing.JLabel();
        txtThrough = new javax.swing.JTextField();
        pnlTable = new javax.swing.JScrollPane();
        tblPayments = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnReceipt = new javax.swing.JButton();
        btnVoid = new javax.swing.JButton();
        btnFindFine = new javax.swing.JButton();

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
            .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboState, 136, 136, 136))
            .addGap(8)
            .addComponent(btnSearch, 96, 96, 96))
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtFrom, 140, 140, 140))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblThrough, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtThrough, 140, 140, 140)))
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnVoid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnFindFine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtSearch, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboState, 36, 36, 36))
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
            .addComponent(cboTerm, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtFrom, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblThrough, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtThrough, 36, 36, 36)))
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnPrevious, 40, 40, 40)
            .addComponent(btnNext, 40, 40, 40)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnReceipt, 40, 40, 40)
            .addComponent(btnVoid, 40, 40, 40)
            .addComponent(btnFindFine, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("Payments");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSearch.setText("Receipt reference or student number");
        lblSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setColumns(24);
        lblState.setText("Status");
        lblState.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboState.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboState.setMinimumSize(new java.awt.Dimension(0, 36));
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
        lblFrom.setText("From (YYYY-MM-DD)");
        lblFrom.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFrom.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFrom.setColumns(24);
        lblThrough.setText("To (YYYY-MM-DD)");
        lblThrough.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtThrough.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtThrough.setColumns(24);
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 140));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblPayments);
        tblPayments.setModel(new ui.model.PaymentTableModel());
        tblPayments.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblPayments.setRowHeight(32);
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
        btnReceipt.setText("View receipt");
        btnReceipt.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnReceipt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceiptActionPerformed(evt);
            }
        });
        btnVoid.setText("Void payment");
        btnVoid.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoidActionPerformed(evt);
            }
        });
        btnFindFine.setText("Find fine to pay");
        btnFindFine.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnFindFine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindFineActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JLabel lblState;
    private javax.swing.JComboBox<java.lang.String> cboState;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JComboBox<ui.model.IdChoice> cboOffice;
    private javax.swing.JLabel lblTerm;
    private javax.swing.JComboBox<ui.model.IdChoice> cboTerm;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JTextField txtFrom;
    private javax.swing.JLabel lblThrough;
    private javax.swing.JTextField txtThrough;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblPayments;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblPage;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnReceipt;
    private javax.swing.JButton btnVoid;
    private javax.swing.JButton btnFindFine;
    // End of variables declaration//GEN-END:variables
}
