package ui.dialog;

import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import model.*;
import service.UserService;
import ui.util.*;

public final class UserEditorDialog extends javax.swing.JDialog {
    private UserService service;
    private Session session;
    private UserAccount original;
    private UserAccount saved;
    private boolean busy;
    private boolean ownAccount;
    private record OfficeChoice(Long id, String label) { @Override public String toString() { return label; } }

    public UserEditorDialog() { this(null, null, null, null, List.of()); }
    public UserEditorDialog(Window owner, UserService service, Session session, UserAccount original, List<Organization> offices) {
        super(owner, ModalityType.APPLICATION_MODAL);
        initComponents(); this.service = service; this.session = session; this.original = original;
        ownAccount = original != null && session != null && original.user().id() == session.user().id();
        cboRole.setModel(new DefaultComboBoxModel<>(Role.values()));
        if (original == null) { cboRole.setSelectedItem(Role.STUDENT); }
        DefaultComboBoxModel<OfficeChoice> choices = new DefaultComboBoxModel<>();
        choices.addElement(new OfficeChoice(null, "Choose an office"));
        for (Organization office : offices) {
            choices.addElement(new OfficeChoice(office.id(), office.code().name() + " - " + office.name() + (office.active() ? "" : " (inactive)")));
        }
        cboOffice.setModel(choices);
        lblTitle.setText(original == null ? "Add user" : "Edit user");
        chkActive.setSelected(original == null || original.user().active());
        chkMustChange.setSelected(original == null || original.user().mustChangePassword());
        if (original == null) { chkMustChange.setEnabled(false); }
        if (original != null) {
            txtUsername.setText(original.user().username()); txtDisplayName.setText(original.user().displayName());
            cboRole.setSelectedItem(original.user().role()); txtStudentNumber.setText(original.studentNumber());
            for (int i = 0; i < choices.getSize(); i++) {
                if (java.util.Objects.equals(choices.getElementAt(i).id(), original.user().orgId())) { cboOffice.setSelectedIndex(i); }
            }
        }
        cboRole.addActionListener(event -> updateAssignment());
        updateAssignment();
        if (ownAccount) {
            cboRole.setEnabled(false); chkActive.setEnabled(false); chkMustChange.setEnabled(false);
            pwdTemporary.setEnabled(false); pwdConfirm.setEnabled(false);
            lblHint.setText("<html>Use My account to change your own password. Keep this administrator active.</html>");
        } else if (original != null) {
            lblHint.setText("<html>Leave both password fields empty to keep the current password. A reset requires a password change at next sign-in.</html>");
        }
        if (original != null && original.user().mustChangePassword()) { chkMustChange.setEnabled(false); }
        lblUsername.setLabelFor(txtUsername); lblDisplayName.setLabelFor(txtDisplayName); lblRole.setLabelFor(cboRole);
        lblOffice.setLabelFor(cboOffice); lblStudentNumber.setLabelFor(txtStudentNumber);
        lblActive.setLabelFor(chkActive);
        lblTemporary.setLabelFor(pwdTemporary); lblConfirm.setLabelFor(pwdConfirm);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtUsername, txtDisplayName, cboRole, cboOffice,
                txtStudentNumber, chkActive, pwdTemporary, pwdConfirm, chkMustChange, btnCancel, btnSave));
        DialogSupport.escape(this, this::cancel); getRootPane().setDefaultButton(btnSave); UiStyle.primary(btnSave);
        btnSave.setEnabled(service != null); setLocationRelativeTo(owner);
    }
    private void updateAssignment() {
        Role role = (Role) cboRole.getSelectedItem();
        boolean student = role == Role.STUDENT, officer = role == Role.OFFICER;
        txtStudentNumber.setEnabled(student); cboOffice.setEnabled(officer);
        if (!student) { txtStudentNumber.setText(""); }
        if (!officer) { cboOffice.setSelectedIndex(0); }
    }
    public Optional<UserAccount> savedAccount() { return Optional.ofNullable(saved); }
    private void save() {
        if (busy || service == null) { return; }
        char[] password = pwdTemporary.getPassword(), confirmation = pwdConfirm.getPassword();
        boolean matches = Arrays.equals(password, confirmation); Arrays.fill(confirmation, '\0');
        pwdTemporary.setText(""); pwdConfirm.setText("");
        if (!matches) { Arrays.fill(password, '\0'); UiStyle.status(lblStatus, "The temporary passwords do not match. Enter them again.", true); return; }
        OfficeChoice choice = (OfficeChoice) cboOffice.getSelectedItem();
        UserDraft draft = new UserDraft(txtUsername.getText(), txtDisplayName.getText(), (Role) cboRole.getSelectedItem(),
                txtStudentNumber.getText(), choice == null ? null : choice.id(), chkActive.isSelected(), chkMustChange.isSelected());
        busy = true; UiStyle.status(lblStatus, "Saving user...", false);
        UiTasks.run(this, () -> service.save(session, original, draft, password), result -> { busy = false; saved = result; dispose(); },
                error -> { busy = false; UiStyle.status(lblStatus, error.getMessage(), true); }, txtUsername, txtDisplayName, cboRole,
                cboOffice, txtStudentNumber, pwdTemporary, pwdConfirm, chkActive, chkMustChange, btnSave, btnCancel);
    }
    private void cancel() {
        if (busy) { UiStyle.status(lblStatus, "Please wait for the save to finish.", true); }
        else { pwdTemporary.setText(""); pwdConfirm.setText(""); dispose(); }
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
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblDisplayName = new javax.swing.JLabel();
        txtDisplayName = new javax.swing.JTextField();
        lblRole = new javax.swing.JLabel();
        cboRole = new javax.swing.JComboBox<>();
        lblOffice = new javax.swing.JLabel();
        cboOffice = new javax.swing.JComboBox<>();
        lblStudentNumber = new javax.swing.JLabel();
        txtStudentNumber = new javax.swing.JTextField();
        lblActive = new javax.swing.JLabel();
        chkActive = new javax.swing.JCheckBox();
        lblTemporary = new javax.swing.JLabel();
        pwdTemporary = new javax.swing.JPasswordField();
        lblConfirm = new javax.swing.JLabel();
        pwdConfirm = new javax.swing.JPasswordField();
        chkMustChange = new javax.swing.JCheckBox();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | User details");
        setMinimumSize(new java.awt.Dimension(840, 700));
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
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cboRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblTemporary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pwdTemporary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cboOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblActive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkActive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pwdConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(chkMustChange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtUsername, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtDisplayName, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboRole, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboOffice, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudentNumber, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblActive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(chkActive, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTemporary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pwdTemporary, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pwdConfirm, 36, 36, 36)))
            .addGap(8)
            .addComponent(chkMustChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblHint, 48, 48, 48)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("User details");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblUsername.setText("Username *");
        lblUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtUsername.setColumns(24);
        lblDisplayName.setText("Display name *");
        lblDisplayName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtDisplayName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtDisplayName.setColumns(24);
        lblRole.setText("Role *");
        lblRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblOffice.setText("Office (officers only)");
        lblOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStudentNumber.setText("Student number (students only)");
        lblStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setColumns(24);
        lblActive.setText("Account status");
        lblActive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        chkActive.setText("Active user");
        chkActive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblTemporary.setText("Initial / reset password");
        lblTemporary.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdTemporary.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdTemporary.setColumns(24);
        lblConfirm.setText("Confirm initial / reset password");
        lblConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdConfirm.setColumns(24);
        chkMustChange.setText("Require password change at next sign-in");
        chkMustChange.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblHint.setText("<html>Initial passwords need at least 12 characters, within 72 UTF-8 bytes. New users must change their password at first sign-in.</html>");
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
        btnSave.setText("Save");
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
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JLabel lblDisplayName;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JLabel lblRole;
    private javax.swing.JComboBox<model.Role> cboRole;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JComboBox<OfficeChoice> cboOffice;
    private javax.swing.JLabel lblStudentNumber;
    private javax.swing.JTextField txtStudentNumber;
    private javax.swing.JLabel lblActive;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JLabel lblTemporary;
    private javax.swing.JPasswordField pwdTemporary;
    private javax.swing.JLabel lblConfirm;
    private javax.swing.JPasswordField pwdConfirm;
    private javax.swing.JCheckBox chkMustChange;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
