package ui.panel;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import model.*;
import service.StudentService;
import ui.dialog.StudentEditorDialog;
import ui.model.StudentTableModel;
import ui.util.*;

public final class StudentsPanel extends javax.swing.JPanel {
    private final StudentTableModel rows = new StudentTableModel();
    private StudentService service;
    private Session session;
    private Consumer<Boolean> activity = value -> {};
    private boolean busy;
    private int page;
    private boolean hasNext;

    public StudentsPanel() {
        initComponents(); TableSupport.configure(tblStudents, rows, "Student search results", 5);
        TableSupport.widths(tblStudents, 136, 220, 96, 56, 96, 96);
        cboYear.setModel(new DefaultComboBoxModel<>(new String[]{"All years","1","2","3","4","5","6"}));
        cboState.setModel(new DefaultComboBoxModel<>(new String[]{"Active","Inactive","All statuses"}));
        lblSearch.setLabelFor(txtSearch); lblProgram.setLabelFor(txtProgram); lblYear.setLabelFor(cboYear);
        lblSection.setLabelFor(txtSection); lblState.setLabelFor(cboState);
        txtSearch.addActionListener(event -> reload(0)); txtProgram.addActionListener(event -> reload(0)); txtSection.addActionListener(event -> reload(0));
        tblStudents.getSelectionModel().addListSelectionListener(event -> updateActions());
        UiStyle.primary(btnAdd); updateActions();
    }
    public void configure(StudentService service, Session session, Consumer<Boolean> activity) {
        this.service = service; this.session = session; this.activity = activity;
        boolean admin = session.user().role() == Role.ADMIN;
        btnAdd.setVisible(admin); btnEdit.setVisible(admin); updateActions();
    }
    public void reload() { reload(page); }
    public void focusSearch() { SwingUtilities.getRootPane(this).setDefaultButton(btnSearch); txtSearch.requestFocusInWindow(); }
    private void reload(int requestedPage) {
        if (busy || service == null) { return; }
        StudentFilter filter = new StudentFilter(txtSearch.getText(), txtProgram.getText(), cboYear.getSelectedIndex() == 0 ? null : cboYear.getSelectedIndex(),
                txtSection.getText(), cboState.getSelectedIndex() == 2 ? null : cboState.getSelectedIndex() == 0, requestedPage, 25);
        busy = true; activity.accept(true); UiStyle.status(lblStatus, "Loading students...", false);
        UiTasks.run(this, () -> service.search(session, filter), result -> {
            page = result.number(); hasNext = result.hasNext(); rows.setRows(result.items());
            busy = false; activity.accept(false); updateActions();
            lblPage.setText("Page " + (page + 1)); UiStyle.status(lblStatus, result.items().size() + " student records shown", false);
        }, error -> {
            rows.setRows(List.of()); hasNext = false; busy = false; activity.accept(false); updateActions(); UiStyle.status(lblStatus, error.getMessage(), true);
        }, txtSearch, txtProgram, txtSection, cboYear, cboState, btnSearch, btnPrevious, btnNext, btnEdit, btnAdd, tblStudents);
    }
    private void updateActions() {
        boolean admin = session != null && session.user().role() == Role.ADMIN;
        btnSearch.setEnabled(!busy && service != null); btnAdd.setEnabled(!busy && admin);
        btnEdit.setEnabled(!busy && admin && tblStudents.getSelectedRow() >= 0);
        btnPrevious.setEnabled(!busy && page > 0); btnNext.setEnabled(!busy && hasNext);
    }
    private void edit(boolean create) {
        if (busy || service == null) { return; }
        int selected = tblStudents.getSelectedRow();
        if (!create && selected < 0) { return; }
        Student original = create ? null : rows.rowAt(tblStudents.convertRowIndexToModel(selected));
        StudentEditorDialog dialog = new StudentEditorDialog(SwingUtilities.getWindowAncestor(this), service, session, original);
        dialog.setVisible(true); if (dialog.savedStudent().isPresent()) { reload(create ? 0 : page); }
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
        lblState = new javax.swing.JLabel();
        cboState = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        lblProgram = new javax.swing.JLabel();
        txtProgram = new javax.swing.JTextField();
        lblYear = new javax.swing.JLabel();
        cboYear = new javax.swing.JComboBox<>();
        lblSection = new javax.swing.JLabel();
        txtSection = new javax.swing.JTextField();
        pnlTable = new javax.swing.JScrollPane();
        tblStudents = new javax.swing.JTable();
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
            .addComponent(lblState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboState, 144, 144, 144))
            .addGap(8)
            .addComponent(btnSearch, 96, 96, 96))
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblProgram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtProgram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboYear, 112, 112, 112))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
            .addComponent(lblState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboState, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addGap(28)
            .addComponent(btnSearch, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblProgram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtProgram, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboYear, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtSection, 36, 36, 36)))
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
        lblTitle.setText("Students");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSearch.setText("Student number or name");
        lblSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSearch.setColumns(24);
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
        lblProgram.setText("Program");
        lblProgram.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtProgram.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtProgram.setColumns(24);
        lblYear.setText("Year");
        lblYear.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboYear.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSection.setText("Section");
        lblSection.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSection.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSection.setColumns(24);
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblStudents);
        tblStudents.setModel(new ui.model.StudentTableModel());
        tblStudents.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblStudents.setRowHeight(32);
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
        btnAdd.setText("Add student");
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
    private javax.swing.JLabel lblState;
    private javax.swing.JComboBox<java.lang.String> cboState;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel lblProgram;
    private javax.swing.JTextField txtProgram;
    private javax.swing.JLabel lblYear;
    private javax.swing.JComboBox<java.lang.String> cboYear;
    private javax.swing.JLabel lblSection;
    private javax.swing.JTextField txtSection;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblStudents;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblPage;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnAdd;
    // End of variables declaration//GEN-END:variables
}
