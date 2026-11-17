package ui.panel;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import model.*;
import service.UserService;
import ui.dialog.UserEditorDialog;
import ui.model.UserTableModel;
import ui.util.*;

public final class UsersPanel extends javax.swing.JPanel {
    private final UserTableModel rows = new UserTableModel();
    private UserService service;
    private Session session;
    private Consumer<Boolean> activity = value -> {};
    private boolean busy;
    private int page;
    private boolean hasNext;
    public UsersPanel() {
        initComponents(); TableSupport.configure(tblUsers, rows, "User accounts", 4, 5);
        TableSupport.widths(tblUsers, 136, 200, 144, 112, 96, 144);
        DefaultComboBoxModel<String> roles = new DefaultComboBoxModel<>(); roles.addElement("All roles");
        for (Role role : Role.values()) { roles.addElement(role.displayName()); } cboRole.setModel(roles);
        cboState.setModel(new DefaultComboBoxModel<>(new String[]{"Active","Inactive","All statuses"}));
        lblSearch.setLabelFor(txtSearch); lblRole.setLabelFor(cboRole); lblState.setLabelFor(cboState);
        txtSearch.addActionListener(event -> reload(0)); tblUsers.getSelectionModel().addListSelectionListener(event -> updateActions());
        UiStyle.primary(btnAdd); updateActions();
    }
    public void configure(UserService service, Session session, Consumer<Boolean> activity) {
        this.service = service; this.session = session; this.activity = activity; updateActions();
    }
    public void focusSearch() { SwingUtilities.getRootPane(this).setDefaultButton(btnSearch); txtSearch.requestFocusInWindow(); }
    public void reload() { reload(page); }
    private void reload(int requestedPage) {
        if (busy || service == null) { return; }
        UserFilter filter = new UserFilter(txtSearch.getText(), cboRole.getSelectedIndex() == 0 ? null : Role.values()[cboRole.getSelectedIndex() - 1],
                cboState.getSelectedIndex() == 2 ? null : cboState.getSelectedIndex() == 0, requestedPage, 25);
        busy = true; activity.accept(true); UiStyle.status(lblStatus, "Loading users...", false);
        UiTasks.run(this, () -> service.search(session, filter), result -> {
            page = result.number(); hasNext = result.hasNext(); rows.setRows(result.items());
            busy = false; activity.accept(false); updateActions(); lblPage.setText("Page " + (page + 1)); UiStyle.status(lblStatus, result.items().size() + " user accounts shown", false);
        }, error -> {
            rows.setRows(List.of()); hasNext = false; busy = false; activity.accept(false); updateActions(); UiStyle.status(lblStatus, error.getMessage(), true);
        }, txtSearch, cboRole, cboState, btnSearch, btnPrevious, btnNext, btnEdit, btnAdd, tblUsers);
    }
    private void updateActions() {
        btnSearch.setEnabled(!busy && service != null); btnAdd.setEnabled(!busy && service != null);
        btnEdit.setEnabled(!busy && service != null && tblUsers.getSelectedRow() >= 0);
        btnPrevious.setEnabled(!busy && page > 0); btnNext.setEnabled(!busy && hasNext);
    }
    private void edit(boolean create) {
        if (busy || service == null) { return; }
        int selected = tblUsers.getSelectedRow(); if (!create && selected < 0) { return; }
        UserAccount original = create ? null : rows.rowAt(tblUsers.convertRowIndexToModel(selected));
        busy = true; activity.accept(true); UiStyle.status(lblStatus, "Loading office choices...", false);
        UiTasks.run(this, () -> service.offices(session), offices -> {
            busy = false; activity.accept(false); updateActions();
            UserEditorDialog dialog = new UserEditorDialog(SwingUtilities.getWindowAncestor(this), service, session, original, offices);
            dialog.setVisible(true); if (dialog.savedAccount().isPresent()) { reload(create ? 0 : page); }
            else { UiStyle.status(lblStatus, "Ready", false); }
        }, error -> { busy = false; activity.accept(false); updateActions(); UiStyle.status(lblStatus, error.getMessage(), true); },
                txtSearch, cboRole, cboState, btnSearch, btnPrevious, btnNext, btnEdit, btnAdd, tblUsers);
    }
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        reload(0);
    }//GEN-LAST:event_btnSearchActionPerformed
    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        reload(page - 1);
    }//GEN-LAST:event_btnPreviousActionPerformed
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        reload(page + 1);
    }//GEN-LAST:event_btnNextActionPerformed
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        edit(false);
    }//GEN-LAST:event_btnEditActionPerformed
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        edit(true);
    }//GEN-LAST:event_btnAddActionPerformed
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblRole = new javax.swing.JLabel();
        cboRole = new javax.swing.JComboBox<>();
        lblState = new javax.swing.JLabel();
        cboState = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        pnlTable = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(640, 560));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboRole, 160, 160, 160))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboState, 128, 128, 128))
            .addGap(8)
            .addComponent(btnSearch, 88, 88, 88))
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtSearch, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboRole, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboState, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addGap(28)
            .addComponent(btnSearch, 36, 36, 36)))
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnPrevious, 40, 40, 40)
            .addComponent(btnNext, 40, 40, 40)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnEdit, 40, 40, 40)
            .addComponent(btnAdd, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Users");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSearch.setText("Username, name or assignment");
        lblSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setColumns(24);
        lblRole.setText("Role");
        lblRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblState.setText("Status");
        lblState.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboState.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearch.setText("Search");
        btnSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblUsers);
        tblUsers.setModel(new ui.model.UserTableModel());
        tblUsers.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblUsers.setRowHeight(32);
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
        btnEdit.setText("Edit selected");
        btnEdit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        btnAdd.setText("Add user");
        btnAdd.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JLabel lblRole;
    private javax.swing.JComboBox<java.lang.String> cboRole;
    private javax.swing.JLabel lblState;
    private javax.swing.JComboBox<java.lang.String> cboState;
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblUsers;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblPage;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnAdd;
    // End of variables declaration//GEN-END:variables
}
