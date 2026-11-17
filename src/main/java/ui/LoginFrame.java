package ui;

import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import model.Session;
import service.AuthService;
import ui.util.OrderedFocusTraversalPolicy;
import ui.util.UiStyle;
import ui.util.UiTasks;

public final class LoginFrame extends javax.swing.JFrame {
    private AuthService auth;
    private Consumer<Session> signedIn;
    private Runnable exit;
    private boolean busy;
    private char passwordEcho;

    /** Designer constructor: builds the view without connecting to the database. */
    public LoginFrame() {
        initComponents();
        passwordEcho = pwdPassword.getEchoChar();
        getRootPane().setDefaultButton(btnLogin);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtUsername, pwdPassword, chkShowPassword, btnLogin));
        lblUsername.setLabelFor(txtUsername);
        lblPassword.setLabelFor(pwdPassword);
        txtUsername.getAccessibleContext().setAccessibleName("Username");
        pwdPassword.getAccessibleContext().setAccessibleName("Password");
        UiStyle.primary(btnLogin);
        btnLogin.setEnabled(false);
        setLocationRelativeTo(null);
    }

    public LoginFrame(AuthService auth, Consumer<Session> signedIn, Runnable exit) {
        this();
        this.auth = Objects.requireNonNull(auth);
        this.signedIn = Objects.requireNonNull(signedIn);
        this.exit = Objects.requireNonNull(exit);
        btnLogin.setEnabled(true);
        SwingUtilities.invokeLater(txtUsername::requestFocusInWindow);
    }

    public void showMessage(String message, boolean error) {
        UiStyle.status(lblStatus, message, error);
    }

    private void signIn() {
        if (busy || auth == null) {
            return;
        }
        String username = txtUsername.getText();
        char[] password = pwdPassword.getPassword();
        pwdPassword.setText("");
        chkShowPassword.setSelected(false);
        pwdPassword.setEchoChar(passwordEcho);
        busy = true;
        UiStyle.status(lblStatus, "Signing in...", false);
        UiTasks.run(this, () -> auth.login(username, password), session -> {
            busy = false;
            UiStyle.status(lblStatus, "Signed in.", false);
            signedIn.accept(session);
        }, failure -> {
            busy = false;
            showMessage(failure.getMessage(), true);
            pwdPassword.requestFocusInWindow();
        }, txtUsername, pwdPassword, chkShowPassword, btnLogin);
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        signIn();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        pwdPassword.requestFocusInWindow();
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void pwdPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdPasswordActionPerformed
        signIn();
    }//GEN-LAST:event_pwdPasswordActionPerformed

    private void chkShowPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowPasswordActionPerformed
        pwdPassword.setEchoChar(chkShowPassword.isSelected() ? '\0' : passwordEcho);
    }//GEN-LAST:event_chkShowPasswordActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (busy) {
            showMessage("Please wait for sign-in to finish before closing.", true);
        } else if (exit != null) {
            exit.run();
        } else {
            dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblBrand = new javax.swing.JLabel();
        lblInstruction = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        pwdPassword = new javax.swing.JPasswordField();
        chkShowPassword = new javax.swing.JCheckBox();
        lblStatus = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Sign in");
        setMinimumSize(new java.awt.Dimension(600, 520));
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
            .addComponent(lblBrand, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblInstruction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pwdPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkShowPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblInstruction, 48, 48, 48)
            .addGap(8)
            .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtUsername, 36, 36, 36)
            .addGap(8)
            .addComponent(lblPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pwdPassword, 36, 36, 36)
            .addGap(8)
            .addComponent(chkShowPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnLogin, 40, 40, 40))
            .addGap(24)
        );
        lblBrand.setText("Queue-Clearance");
        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblInstruction.setText("Sign in with your campus account.");
        lblInstruction.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblUsername.setText("Username");
        lblUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtUsername.setColumns(24);
        txtUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsernameActionPerformed(evt);
            }
        });
        lblPassword.setText("Password");
        lblPassword.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdPassword.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pwdPassword.setColumns(24);
        pwdPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdPasswordActionPerformed(evt);
            }
        });
        chkShowPassword.setText("Show password");
        chkShowPassword.setFont(new java.awt.Font("Segoe UI", 0, 14));
        chkShowPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowPasswordActionPerformed(evt);
            }
        });
        lblStatus.setText("Ready to sign in.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnLogin.setText("Sign in");
        btnLogin.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblInstruction;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JPasswordField pwdPassword;
    private javax.swing.JCheckBox chkShowPassword;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnLogin;
    // End of variables declaration//GEN-END:variables
}
