package ui.panel;
import model.*;
import service.*;
import ui.model.*;
import ui.util.*;
import java.util.function.Consumer;
import javax.swing.*;
public final class ClearancePanel extends JPanel {
    private final ClearanceTableModel rows=new ClearanceTableModel();private ClearanceService service;private Session session;private Consumer<Boolean> activity=active->{};private boolean busy;
    public ClearancePanel(){initComponents();TableSupport.configure(tblClearance,rows,"Office clearance",1,3);tblClearance.setDefaultRenderer(java.math.BigDecimal.class,new MoneyCellRenderer());
        lblStudent.setLabelFor(txtStudent);lblTerm.setLabelFor(cboTerm);txtStudent.addActionListener(e->search());tblClearance.getSelectionModel().addListSelectionListener(e->buttons());UiStyle.primary(btnEdit);buttons();
        javax.swing.event.DocumentListener changed=new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){invalidateRows();}public void removeUpdate(javax.swing.event.DocumentEvent e){invalidateRows();}public void changedUpdate(javax.swing.event.DocumentEvent e){invalidateRows();}};
        txtStudent.getDocument().addDocumentListener(changed);cboTerm.addActionListener(e->invalidateRows());
        setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtStudent,cboTerm,btnSearch,tblClearance,btnEdit));
    }
    public void configure(ClearanceService service,Session session,Consumer<Boolean> activity){this.service=service;this.session=session;this.activity=activity;boolean student=session.user().role()==Role.STUDENT;txtStudent.setEditable(!student);if(student)txtStudent.setText("Your linked student record");btnEdit.setVisible(session.user().role()==Role.ADMIN||session.user().role()==Role.OFFICER);}
    public void activate(){if(service==null||busy)return;busy=true;activity.accept(true);
        Long previous=FinanceUi.id(cboTerm);UiTasks.run(this,()->service.references(session),refs->{busy=false;activity.accept(false);FinanceUi.terms(cboTerm,refs.terms(),false);if(previous!=null)FinanceUi.select(cboTerm,previous);else refs.terms().stream().filter(Term::current).findFirst().ifPresent(t->FinanceUi.select(cboTerm,t.id()));buttons();if(session.user().role()==Role.STUDENT||!txtStudent.getText().isBlank())search();else UiStyle.status(lblStatus,"Enter an exact student number and choose a term.",false);},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);buttons();},txtStudent,cboTerm,btnSearch,btnEdit);
    }
    private void search(){if(service==null||busy)return;Long term=FinanceUi.id(cboTerm);if(term==null){UiStyle.status(lblStatus,"Choose a term.",true);return;}String number=txtStudent.getText();busy=true;activity.accept(true);buttons();
        UiTasks.run(this,()->service.find(session,number,term),result->{busy=false;activity.accept(false);rows.setRows(result);UiStyle.status(lblStatus,"Balances are computed from the current ledger. An absent decision is Pending.",false);buttons();},error->{busy=false;activity.accept(false);rows.setRows(java.util.List.of());UiStyle.status(lblStatus,error.getMessage(),true);buttons();},txtStudent,cboTerm,btnSearch,tblClearance);
    }
    private void invalidateRows(){rows.setRows(java.util.List.of());buttons();}
    private void buttons(){btnEdit.setEnabled(service!=null&&!busy&&tblClearance.getSelectedRow()>=0);}
    public void focusPrimaryAction(){var root=SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnSearch);txtStudent.requestFocusInWindow();}
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnSearchActionPerformed
        search();
    }//GEN-LAST:event_btnSearchActionPerformed
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnEditActionPerformed
        int selected=tblClearance.getSelectedRow();if(selected<0||busy)return;ClearanceRow row=rows.rowAt(tblClearance.convertRowIndexToModel(selected));busy=true;activity.accept(true);var dialog=new ui.dialog.ClearanceDecisionDialog(SwingUtilities.getWindowAncestor(this),service,session,row);dialog.setVisible(true);busy=false;activity.accept(false);search();
    }//GEN-LAST:event_btnEditActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblStudent = new javax.swing.JLabel();
        txtStudent = new javax.swing.JTextField();
        lblTerm = new javax.swing.JLabel();
        cboTerm = new javax.swing.JComboBox<>();
        btnSearch = new javax.swing.JButton();
        pnlTable = new javax.swing.JScrollPane();
        tblClearance = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        btnEdit = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(720, 640));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudent, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboTerm, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addGap(28)
            .addComponent(btnSearch, 36, 36, 36)))
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addComponent(btnEdit, 40, 40, 40)
            .addGap(16)
        );
        lblTitle.setText("Clearance");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblStudent.setText("Student number (exact)");
        lblStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setColumns(24);
        lblTerm.setText("Term");
        lblTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setMinimumSize(new java.awt.Dimension(0, 36));
        btnSearch.setText("View clearance");
        btnSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblClearance);
        tblClearance.setModel(new ui.model.ClearanceTableModel());
        tblClearance.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblClearance.setRowHeight(32);
        lblStatus.setText("Choose a student and term.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnEdit.setText("Record decision");
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
    private javax.swing.JLabel lblStudent;
    private javax.swing.JTextField txtStudent;
    private javax.swing.JLabel lblTerm;
    private javax.swing.JComboBox<ui.model.IdChoice> cboTerm;
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblClearance;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnEdit;
    // End of variables declaration//GEN-END:variables
}
