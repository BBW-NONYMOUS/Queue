package ui.dialog;

import java.awt.Window;
import java.util.Optional;
import model.*;
import service.OfficeService;
import ui.util.*;

public final class OfficeEditorDialog extends javax.swing.JDialog {
    private OfficeService service;
    private Session session;
    private Organization original;
    private Organization saved;
    private boolean busy;
    public OfficeEditorDialog() { this(null, null, null, null); }
    public OfficeEditorDialog(Window owner, OfficeService service, Session session, Organization original) {
        super(owner, ModalityType.APPLICATION_MODAL);
        initComponents(); this.service = service; this.session = session; this.original = original;
        lblCode.setLabelFor(txtCode); lblName.setLabelFor(txtName);
        if (original != null) {
            txtCode.setText(original.code().name()); txtName.setText(original.name());
            chkQueueOpen.setSelected(original.queueOpen()); chkActive.setSelected(original.active());
        }
        boolean admin = session != null && session.user().role() == Role.ADMIN;
        txtName.setEditable(admin); chkActive.setEnabled(admin);
        chkQueueOpen.setEnabled(chkActive.isSelected());
        chkActive.addActionListener(event -> {
            if (!chkActive.isSelected()) { chkQueueOpen.setSelected(false); }
            chkQueueOpen.setEnabled(chkActive.isSelected());
        });
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtName, chkQueueOpen, chkActive, btnCancel, btnSave));
        getRootPane().setDefaultButton(btnSave); UiStyle.primary(btnSave); DialogSupport.escape(this, this::cancel);
        btnSave.setEnabled(service != null && original != null); setLocationRelativeTo(owner);
    }
    public Optional<Organization> savedOffice() { return Optional.ofNullable(saved); }
    private void save() {
        if (busy || service == null || original == null) { return; }
        OfficeDraft draft = new OfficeDraft(txtName.getText(), chkQueueOpen.isSelected(), chkActive.isSelected());
        busy = true; UiStyle.status(lblStatus, "Saving office...", false);
        UiTasks.run(this, () -> service.save(session, original, draft), result -> { busy = false; saved = result; dispose(); },
                error -> { busy = false; UiStyle.status(lblStatus, error.getMessage(), true); }, txtName, chkQueueOpen, chkActive, btnSave, btnCancel);
    }
    private void cancel() {
        if (busy) { UiStyle.status(lblStatus, "Please wait for the save to finish.", true); } else { dispose(); }
    }
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
        lblCode = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        chkQueueOpen = new javax.swing.JCheckBox();
        chkActive = new javax.swing.JCheckBox();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Office settings");
        setMinimumSize(new java.awt.Dimension(620, 580));
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
            .addComponent(lblCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkQueueOpen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkActive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtCode, 36, 36, 36)
            .addGap(8)
            .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtName, 36, 36, 36)
            .addGap(8)
            .addComponent(chkQueueOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(chkActive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblHint, 48, 48, 48)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Office settings");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblCode.setText("Office code");
        lblCode.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtCode.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtCode.setColumns(24);
        txtCode.setEditable(false);
        txtCode.setFocusable(false);
        lblName.setText("Office name *");
        lblName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtName.setColumns(24);
        chkQueueOpen.setText("Open for new tickets");
        chkQueueOpen.setFont(new java.awt.Font("Segoe UI", 0, 14));
        chkActive.setText("Active office");
        chkActive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblHint.setText("<html>Closing the queue stops new tickets. Existing tickets remain available to officers.</html>");
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
        btnSave.setText("Save office");
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
    private javax.swing.JLabel lblCode;
    private javax.swing.JTextField txtCode;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField txtName;
    private javax.swing.JCheckBox chkQueueOpen;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
