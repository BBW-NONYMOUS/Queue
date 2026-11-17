package ui.dialog;

import java.awt.Window;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import model.Session;
import service.AuthService;
import ui.util.OrderedFocusTraversalPolicy;
import ui.util.UiStyle;
import ui.util.UiTasks;

public final class ChangePasswordDialog extends javax.swing.JDialog {
    private AuthService auth;
    private Session session;
    private Session result;
    private boolean busy;

    public ChangePasswordDialog() {
        this((Window) null, null, null, false);
    }

    public ChangePasswordDialog(Window owner, AuthService auth, Session session, boolean mandatory) {
        super(owner, ModalityType.APPLICATION_MODAL);
        initComponents();
        this.auth = auth;
        this.session = session;
        lblCurrent.setLabelFor(pwdCurrent);
        lblNew.setLabelFor(pwdNew);
        lblConfirm.setLabelFor(pwdConfirm);
        pwdCurrent.getAccessibleContext().setAccessibleName("Current password");
        pwdNew.getAccessibleContext().setAccessibleName("New password");
        pwdConfirm.getAccessibleContext().setAccessibleName("Confirm new password");
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(pwdCurrent, pwdNew, pwdConfirm, btnCancel, btnSave));
        getRootPane().setDefaultButton(btnSave);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) { cancel(); }
        });
        UiStyle.primary(btnSave);
        btnSave.setEnabled(auth != null);
        if (mandatory) {
            lblInstruction.setText("<html>Choose a new password to finish signing in.</html>");
            btnCancel.setText("Cancel sign-in");
        }
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(pwdCurrent::requestFocusInWindow);
    }

    public Optional<Session> updatedSession() {
        return Optional.ofNullable(result);
    }

    private void save() {
        if (busy || auth == null) {
            return;
        }
        char[] current = pwdCurrent.getPassword();
        char[] replacement = pwdNew.getPassword();
        char[] confirmation = pwdConfirm.getPassword();
        boolean matches = Arrays.equals(replacement, confirmation);
        Arrays.fill(confirmation, '\0');
        clearFields();
        if (!matches) {
            Arrays.fill(current, '\0');
            Arrays.fill(replacement, '\0');
            UiStyle.status(lblStatus, "The new passwords do not match. Enter them again.", true);
            pwdNew.requestFocusInWindow();
            return;
        }
        busy = true;
        UiStyle.status(lblStatus, "Changing password...", false);
        UiTasks.run(this, () -> auth.changePassword(session, current, replacement), updated -> {
            busy = false;
            result = updated;
            dispose();
        }, failure -> {
            busy = false;
            UiStyle.status(lblStatus, failure.getMessage(), true);
            pwdCurrent.requestFocusInWindow();
        }, pwdCurrent, pwdNew, pwdConfirm, btnSave, btnCancel);
    }

    private void clearFields() {
        pwdCurrent.setText("");
        pwdNew.setText("");
        pwdConfirm.setText("");
    }

    private void cancel() {
        if (busy) {
            UiStyle.status(lblStatus, "Please wait for the password change to finish.", true);
            return;
        }
        clearFields();
        dispose();
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
        lblInstruction = new javax.swing.JLabel();
        lblCurrent = new javax.swing.JLabel();
        pwdCurrent = new javax.swing.JPasswordField();
        lblNew = new javax.swing.JLabel();
        pwdNew = new javax.swing.JPasswordField();
        lblConfirm = new javax.swing.JLabel();
        pwdConfirm = new javax.swing.JPasswordField();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Change password");
        setMinimumSize(new java.awt.Dimension(620, 600));
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
            .addComponent(lblInstruction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pwdCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pwdNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pwdConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addComponent(lblInstruction, 48, 48, 48)
            .addGap(8)
            .addComponent(lblCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pwdCurrent, 36, 36, 36)
            .addGap(8)
            .addComponent(lblNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pwdNew, 36, 36, 36)
            .addGap(8)
            .addComponent(lblConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pwdConfirm, 36, 36, 36)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Change password");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblInstruction.setText("<html>Use at least 12 characters, within the 72 UTF-8 byte limit.</html>");
        lblInstruction.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblCurrent.setText("Current password");
        lblCurrent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdCurrent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdCurrent.setColumns(24);
        lblNew.setText("New password");
        lblNew.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdNew.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdNew.setColumns(24);
        lblConfirm.setText("Confirm new password");
        lblConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdConfirm.setColumns(24);
        lblStatus.setText("Enter your current password and choose a new one.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnSave.setText("Save password");
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
    private javax.swing.JLabel lblInstruction;
    private javax.swing.JLabel lblCurrent;
    private javax.swing.JPasswordField pwdCurrent;
    private javax.swing.JLabel lblNew;
    private javax.swing.JPasswordField pwdNew;
    private javax.swing.JLabel lblConfirm;
    private javax.swing.JPasswordField pwdConfirm;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
