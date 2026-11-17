package ui.panel;

import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import model.Session;
import service.AuthService;
import ui.dialog.ChangePasswordDialog;
import ui.util.UiStyle;

public final class AccountPanel extends javax.swing.JPanel {
    private AuthService auth;
    private Session session;
    private Consumer<Session> changed;

    public AccountPanel() {
        initComponents();
        lblUsername.setLabelFor(txtUsername);
        lblDisplayName.setLabelFor(txtDisplayName);
        lblRole.setLabelFor(txtRole);
        UiStyle.primary(btnChangePassword);
        btnChangePassword.setEnabled(false);
    }

    public void configure(AuthService auth, Session session, Consumer<Session> changed) {
        this.auth = auth;
        this.session = session;
        this.changed = changed;
        txtUsername.setText(session.user().username());
        txtDisplayName.setText(session.user().displayName());
        txtRole.setText(session.user().role().displayName());
        btnChangePassword.setEnabled(true);
        UiStyle.status(lblStatus, "Active account", false);
    }

    public void focusPrimaryAction() {
        javax.swing.JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) {
            root.setDefaultButton(btnChangePassword);
        }
        btnChangePassword.requestFocusInWindow();
    }

    public javax.swing.JButton primaryAction() {
        return btnChangePassword;
    }

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        if (auth == null || session == null) {
            return;
        }
        ChangePasswordDialog dialog = new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this), auth, session, false);
        dialog.setVisible(true);
        dialog.updatedSession().ifPresent(updated -> {
            session = updated;
            changed.accept(updated);
            UiStyle.status(lblStatus, "Password changed", false);
        });
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblDisplayName = new javax.swing.JLabel();
        txtDisplayName = new javax.swing.JTextField();
        lblRole = new javax.swing.JLabel();
        txtRole = new javax.swing.JTextField();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnChangePassword = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(560, 560));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtUsername, 36, 36, 36)
            .addGap(8)
            .addComponent(lblDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtDisplayName, 36, 36, 36)
            .addGap(8)
            .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtRole, 36, 36, 36)
            .addGap(8)
            .addComponent(lblHint, 48, 48, 48)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnChangePassword, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("My account");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblUsername.setText("Username");
        lblUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtUsername.setColumns(24);
        txtUsername.setEditable(false);
        txtUsername.setFocusable(false);
        lblDisplayName.setText("Display name");
        lblDisplayName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtDisplayName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtDisplayName.setColumns(24);
        txtDisplayName.setEditable(false);
        txtDisplayName.setFocusable(false);
        lblRole.setText("Role");
        lblRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtRole.setColumns(24);
        txtRole.setEditable(false);
        txtRole.setFocusable(false);
        lblHint.setText("<html>Keep your password private. Sign out before leaving a shared computer.</html>");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText(" ");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnChangePassword.setText("Change password");
        btnChangePassword.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangePasswordActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JLabel lblDisplayName;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JLabel lblRole;
    private javax.swing.JTextField txtRole;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnChangePassword;
    // End of variables declaration//GEN-END:variables
}
