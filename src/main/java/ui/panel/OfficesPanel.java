package ui.panel;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import model.*;
import service.OfficeService;
import ui.dialog.OfficeEditorDialog;
import ui.model.OfficeTableModel;
import ui.util.*;

public final class OfficesPanel extends javax.swing.JPanel {
    private final OfficeTableModel rows = new OfficeTableModel();
    private OfficeService service;
    private Session session;
    private Consumer<Boolean> activity = value -> {};
    private boolean busy;
    public OfficesPanel() {
        initComponents(); TableSupport.configure(tblOffices, rows, "Clearance offices", 2, 3);
        TableSupport.widths(tblOffices, 88, 320, 112, 112);
        tblOffices.getSelectionModel().addListSelectionListener(event -> updateActions());
        UiStyle.primary(btnEdit); updateActions();
    }
    public void configure(OfficeService service, Session session, Consumer<Boolean> activity) {
        this.service = service; this.session = session; this.activity = activity; updateActions();
    }
    public void focusRefresh() { SwingUtilities.getRootPane(this).setDefaultButton(btnRefresh); btnRefresh.requestFocusInWindow(); }
    public void reload() {
        if (busy || service == null) { return; }
        busy = true; activity.accept(true); UiStyle.status(lblStatus, "Loading offices...", false);
        UiTasks.run(this, () -> service.list(session), result -> {
            rows.setRows(result); busy = false; activity.accept(false); updateActions(); UiStyle.status(lblStatus, result.size() + " offices shown", false);
        }, error -> {
            rows.setRows(List.of()); busy = false; activity.accept(false); updateActions(); UiStyle.status(lblStatus, error.getMessage(), true);
        }, btnRefresh, btnEdit, tblOffices);
    }
    private void updateActions() { btnRefresh.setEnabled(!busy && service != null); btnEdit.setEnabled(!busy && service != null && tblOffices.getSelectedRow() >= 0); }
    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        reload();
    }//GEN-LAST:event_btnRefreshActionPerformed
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        if (busy || tblOffices.getSelectedRow() < 0 || service == null) { return; }
        Organization original = rows.rowAt(tblOffices.convertRowIndexToModel(tblOffices.getSelectedRow()));
        OfficeEditorDialog dialog = new OfficeEditorDialog(SwingUtilities.getWindowAncestor(this), service, session, original);
        dialog.setVisible(true); if (dialog.savedOffice().isPresent()) { reload(); }
    }//GEN-LAST:event_btnEditActionPerformed
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblHint = new javax.swing.JLabel();
        pnlTable = new javax.swing.JScrollPane();
        tblOffices = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(640, 560));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblHint, 48, 48, 48)
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnRefresh, 40, 40, 40)
            .addComponent(btnEdit, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Offices");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblHint.setText("<html>Manage the seven clearance offices. Queue availability controls new tickets.</html>");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblOffices);
        tblOffices.setModel(new ui.model.OfficeTableModel());
        tblOffices.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblOffices.setRowHeight(32);
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRefresh.setText("Refresh");
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        btnEdit.setText("Edit office");
        btnEdit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblHint;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblOffices;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnEdit;
    // End of variables declaration//GEN-END:variables
}
