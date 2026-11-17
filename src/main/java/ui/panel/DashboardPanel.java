package ui.panel;

import model.User;
import service.NavigationService;
import ui.util.UiStyle;

/** Role-specific signed-in identity. Feature cards are added in later phases. */
public final class DashboardPanel extends javax.swing.JPanel {
    public DashboardPanel() {
        initComponents();
    }

    public void showWorkspace(NavigationService.Workspace workspace) {
        User user = workspace.session().user();
        lblTitle.setText(workspace.title());
        UiStyle.plainText(lblWelcome, "Welcome, " + user.displayName());
        lblDescription.setText("<html>" + UiStyle.escape(workspace.description()) + "</html>");
        UiStyle.plainText(lblUsernameValue, user.username());
        UiStyle.plainText(lblRoleValue, user.role().displayName());
        UiStyle.plainText(lblAssignmentValue, workspace.assignment());
        UiStyle.status(lblAccountState, "Active account", false);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        lblUsernameValue = new javax.swing.JLabel();
        lblRole = new javax.swing.JLabel();
        lblRoleValue = new javax.swing.JLabel();
        lblAssignment = new javax.swing.JLabel();
        lblAssignmentValue = new javax.swing.JLabel();
        lblAccountState = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(560, 480));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblWelcome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUsernameValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRoleValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblAssignment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblAssignmentValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblAccountState, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblDescription, 48, 48, 48)
            .addGap(8)
            .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblUsernameValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblRoleValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblAssignment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblAssignmentValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblAccountState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGap(24)
        );
        lblTitle.setText("Workspace");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblWelcome.setText("Welcome");
        lblWelcome.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblDescription.setText("Your campus account");
        lblDescription.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblUsername.setText("Username");
        lblUsername.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblUsernameValue.setText(" ");
        lblUsernameValue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblRole.setText("Role");
        lblRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblRoleValue.setText(" ");
        lblRoleValue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblAssignment.setText("Assignment");
        lblAssignment.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblAssignmentValue.setText(" ");
        lblAssignmentValue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblAccountState.setText(" ");
        lblAccountState.setFont(new java.awt.Font("Segoe UI", 0, 14));
    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblUsernameValue;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblRoleValue;
    private javax.swing.JLabel lblAssignment;
    private javax.swing.JLabel lblAssignmentValue;
    private javax.swing.JLabel lblAccountState;
    // End of variables declaration//GEN-END:variables
}
