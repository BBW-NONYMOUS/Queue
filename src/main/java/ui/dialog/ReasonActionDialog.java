package ui.dialog;

import java.awt.Window;
import service.*;
import ui.util.*;

/** Shared, reviewable confirmation form for waivers and payment voids. */
public final class ReasonActionDialog extends javax.swing.JDialog {
    @FunctionalInterface public interface Action { void execute(String reason) throws ServiceException; }
    private Action action; private boolean busy; private boolean completed;
    public ReasonActionDialog() { this(null, "Confirm action", "", "Confirm", null); }
    public ReasonActionDialog(Window owner, String title, String details, String buttonText, Action action) {
        super(owner, ModalityType.APPLICATION_MODAL); initComponents(); this.action = action;
        UiStyle.plainText(lblTitle, title); txtDetails.setText(details); btnConfirm.setText(buttonText);
        lblReason.setLabelFor(txtReason); DialogSupport.escape(this, this::cancel); getRootPane().setDefaultButton(btnConfirm); UiStyle.primary(btnConfirm);
        chkConfirm.addActionListener(event -> btnConfirm.setEnabled(!busy && chkConfirm.isSelected() && this.action != null));
        btnConfirm.setEnabled(false); setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtDetails, txtReason, chkConfirm, btnCancel, btnConfirm)); setLocationRelativeTo(owner);
    }
    public boolean completed() { return completed; }
    private void apply() {
        if (busy || action == null || !chkConfirm.isSelected()) { return; }
        String reason = txtReason.getText(); busy = true; UiStyle.status(lblStatus, "Applying action...", false);
        UiTasks.run(this, () -> { action.execute(reason); return Boolean.TRUE; }, result -> { busy = false; completed = true; dispose(); }, error -> {
            busy = false; UiStyle.status(lblStatus, error.getMessage(), true);
            if (error instanceof CommitUncertainException) { btnConfirm.setEnabled(false); chkConfirm.setEnabled(false); }
        }, txtReason, chkConfirm, btnConfirm, btnCancel);
    }
    private void cancel() { if (busy) { UiStyle.status(lblStatus, "Please wait for this action to finish.", true); } else { dispose(); } }
    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
        apply();
    }//GEN-LAST:event_btnConfirmActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        cancel();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancel();
    }//GEN-LAST:event_formWindowClosing
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        pnlDetails = new javax.swing.JScrollPane();
        txtDetails = new javax.swing.JTextArea();
        lblReason = new javax.swing.JLabel();
        txtReason = new javax.swing.JTextField();
        chkConfirm = new javax.swing.JCheckBox();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnConfirm = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Confirm action");
        setMinimumSize(new java.awt.Dimension(720, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblReason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtReason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtReason, 36, 36, 36)
            .addGap(8)
            .addComponent(chkConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblStatus, 80, 80, 80)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnConfirm, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Confirm action");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        pnlDetails.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlDetails.setPreferredSize(new java.awt.Dimension(500, 120));
        pnlDetails.setViewportView(txtDetails);
        txtDetails.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtDetails.setEditable(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        lblReason.setText("Required reason (up to 500 characters)");
        lblReason.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtReason.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtReason.setColumns(24);
        chkConfirm.setText("I confirm this action");
        chkConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText("Review the details and enter the reason.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnConfirm.setText("Confirm");
        btnConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmActionPerformed(evt);
            }
        });
        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane pnlDetails;
    private javax.swing.JTextArea txtDetails;
    private javax.swing.JLabel lblReason;
    private javax.swing.JTextField txtReason;
    private javax.swing.JCheckBox chkConfirm;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnConfirm;
    // End of variables declaration//GEN-END:variables
}
