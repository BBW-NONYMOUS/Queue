package ui.dialog;

import java.awt.Window;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import model.*;
import service.*;
import ui.util.*;

public final class RecordPaymentDialog extends javax.swing.JDialog {
    private PaymentService service; private Session session; private FineRow fine; private Payment saved; private boolean busy;
    public RecordPaymentDialog() { this(null, null, null, null); }
    public RecordPaymentDialog(Window owner, PaymentService service, Session session, FineRow fine) {
        super(owner, ModalityType.APPLICATION_MODAL); initComponents(); this.service = service; this.session = session; this.fine = fine;
        if (fine != null) {
            txtStudent.setText(fine.studentNumber() + " - " + fine.studentName()); txtOfficeTerm.setText(fine.officeCode() + " / " + fine.termCode());
            txtFineReason.setText(fine.fine().reason()); txtBalance.setText(UiStyle.pesos(fine.balance())); txtAmount.setText(fine.balance().toPlainString());
        }
        txtReference.setText("RCPT-" + UUID.randomUUID().toString().toUpperCase(java.util.Locale.ROOT));
        lblStudent.setLabelFor(txtStudent); lblOfficeTerm.setLabelFor(txtOfficeTerm); lblFineReason.setLabelFor(txtFineReason);
        lblBalance.setLabelFor(txtBalance); lblAmount.setLabelFor(txtAmount); lblReference.setLabelFor(txtReference);
        getRootPane().setDefaultButton(btnSave); UiStyle.primary(btnSave); DialogSupport.escape(this, this::cancel);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtFineReason, txtAmount, txtReference, btnCancel, btnSave));
        btnSave.setEnabled(service != null && fine != null); setLocationRelativeTo(owner);
    }
    public Optional<Payment> savedPayment() { return Optional.ofNullable(saved); }
    private void save() {
        if (busy || service == null || fine == null) { return; }
        BigDecimal amount;
        try { amount = Money.parse(txtAmount.getText()); } catch (ServiceException ex) { UiStyle.status(lblStatus, ex.getMessage(), true); return; }
        String reference = txtReference.getText(); busy = true; UiStyle.status(lblStatus, "Recording payment...", false);
        UiTasks.run(this, () -> service.record(session, fine.fine().id(), amount, reference), result -> { busy = false; saved = result; dispose(); }, error -> {
            busy = false; UiStyle.status(lblStatus, error.getMessage(), true);
            if (error instanceof CommitUncertainException) {
                btnSave.setEnabled(false); txtAmount.setEnabled(false); txtReference.setEnabled(false);
                UiStyle.status(lblStatus, "Result unconfirmed. Close this dialog and search Payments for " + reference + " before recording anything else.", true);
            }
        }, txtAmount, txtReference, btnSave, btnCancel);
    }
    private void cancel() { if (busy) { UiStyle.status(lblStatus, "Please wait for the payment result.", true); } else { dispose(); } }
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        save();
    }//GEN-LAST:event_btnSaveActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        cancel();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancel();
    }//GEN-LAST:event_formWindowClosing
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblStudent = new javax.swing.JLabel();
        txtStudent = new javax.swing.JTextField();
        lblOfficeTerm = new javax.swing.JLabel();
        txtOfficeTerm = new javax.swing.JTextField();
        lblFineReason = new javax.swing.JLabel();
        pnlReason = new javax.swing.JScrollPane();
        txtFineReason = new javax.swing.JTextArea();
        lblBalance = new javax.swing.JLabel();
        txtBalance = new javax.swing.JTextField();
        lblAmount = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        lblReference = new javax.swing.JLabel();
        txtReference = new javax.swing.JTextField();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Record payment");
        setMinimumSize(new java.awt.Dimension(760, 700));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtBalance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblOfficeTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtOfficeTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(lblFineReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(pnlReason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtReference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudent, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOfficeTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtOfficeTerm, 36, 36, 36)))
            .addGap(8)
            .addComponent(lblFineReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlReason, 64, 64, 64)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtBalance, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtAmount, 36, 36, 36)))
            .addGap(8)
            .addComponent(lblReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtReference, 36, 36, 36)
            .addGap(8)
            .addComponent(lblHint, 40, 40, 40)
            .addGap(8)
            .addComponent(lblStatus, 80, 80, 80)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("Record payment");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblStudent.setText("Student");
        lblStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setColumns(24);
        txtStudent.setEditable(false);
        txtStudent.setFocusable(false);
        lblOfficeTerm.setText("Office / term");
        lblOfficeTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOfficeTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOfficeTerm.setColumns(24);
        txtOfficeTerm.setEditable(false);
        txtOfficeTerm.setFocusable(false);
        lblFineReason.setText("Fine reason");
        lblFineReason.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlReason.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlReason.setPreferredSize(new java.awt.Dimension(500, 64));
        pnlReason.setViewportView(txtFineReason);
        txtFineReason.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFineReason.setEditable(false);
        txtFineReason.setLineWrap(true);
        txtFineReason.setWrapStyleWord(true);
        lblBalance.setText("Current fine balance");
        lblBalance.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtBalance.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtBalance.setColumns(24);
        txtBalance.setEditable(false);
        txtBalance.setFocusable(false);
        txtBalance.setHorizontalAlignment(4);
        lblAmount.setText("Amount received (PHP) *");
        lblAmount.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtAmount.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtAmount.setColumns(24);
        txtAmount.setHorizontalAlignment(4);
        lblReference.setText("Unique receipt reference *");
        lblReference.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtReference.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtReference.setColumns(24);
        lblHint.setText("<html>Confirm the student and amount. The balance is checked again when the payment is saved.</html>");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnSave.setText("Record payment");
        btnSave.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblStudent;
    private javax.swing.JTextField txtStudent;
    private javax.swing.JLabel lblOfficeTerm;
    private javax.swing.JTextField txtOfficeTerm;
    private javax.swing.JLabel lblFineReason;
    private javax.swing.JScrollPane pnlReason;
    private javax.swing.JTextArea txtFineReason;
    private javax.swing.JLabel lblBalance;
    private javax.swing.JTextField txtBalance;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JLabel lblReference;
    private javax.swing.JTextField txtReference;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
