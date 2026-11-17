package ui.panel;
import model.*;
import service.*;
import ui.model.*;
import ui.util.*;
import java.util.function.Consumer;
import javax.swing.*;
public final class AuditPanel extends JPanel {
    private final AuditTableModel rows=new AuditTableModel();private AuditService service;private Session session;private Consumer<Boolean> activity=active->{};private boolean busy,hasNext;private int page;
    public AuditPanel(){initComponents();TableSupport.configure(tblAudit,rows,"Audit entries");cboTable.addItem("");for(String table:AuditService.TABLES)cboTable.addItem(table);
        lblTable.setLabelFor(cboTable);lblActor.setLabelFor(txtActor);lblKey.setLabelFor(txtKey);lblFrom.setLabelFor(txtFrom);lblThrough.setLabelFor(txtThrough);txtActor.addActionListener(e->search(true));txtKey.addActionListener(e->search(true));tblAudit.getSelectionModel().addListSelectionListener(e->buttons());UiStyle.primary(btnDetails);buttons();
        setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(cboTable,txtActor,txtKey,txtFrom,txtThrough,btnSearch,tblAudit,btnPrevious,btnNext,btnDetails));
    }
    public void configure(AuditService service,Session session,Consumer<Boolean> activity){this.service=service;this.session=session;this.activity=activity;}
    public void activate(){search(true);}
    private void search(boolean first){if(service==null||busy)return;AuditFilter filter;try{filter=new AuditFilter((String)cboTable.getSelectedItem(),txtActor.getText(),txtKey.getText(),FinanceUi.date(txtFrom.getText()),FinanceUi.date(txtThrough.getText()),first?0:page);}catch(ValidationException ex){UiStyle.status(lblStatus,ex.getMessage(),true);return;}if(first)page=0;busy=true;activity.accept(true);buttons();
        UiTasks.run(this,()->service.search(session,filter),result->{busy=false;activity.accept(false);rows.setRows(result.items());hasNext=result.hasNext();lblPage.setText("Page "+(page+1));UiStyle.status(lblStatus,"Read-only audit. Blank dates use the last 30 days through today (server time).",false);buttons();},error->{busy=false;activity.accept(false);rows.setRows(java.util.List.of());hasNext=false;UiStyle.status(lblStatus,error.getMessage(),true);buttons();},cboTable,txtActor,txtKey,txtFrom,txtThrough,btnSearch,tblAudit);
    }
    private void buttons(){btnDetails.setEnabled(service!=null&&!busy&&tblAudit.getSelectedRow()>=0);btnPrevious.setEnabled(!busy&&page>0);btnNext.setEnabled(!busy&&hasNext);}
    public void focusPrimaryAction(){var root=SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnSearch);txtActor.requestFocusInWindow();}
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnSearchActionPerformed
        search(true);
    }//GEN-LAST:event_btnSearchActionPerformed
    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnPreviousActionPerformed
        if(!busy&&page>0){page--;search(false);}
    }//GEN-LAST:event_btnPreviousActionPerformed
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnNextActionPerformed
        if(!busy&&hasNext){page++;search(false);}
    }//GEN-LAST:event_btnNextActionPerformed
    private void btnDetailsActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnDetailsActionPerformed
        int selected=tblAudit.getSelectedRow();if(selected<0||busy)return;long id=rows.rowAt(tblAudit.convertRowIndexToModel(selected)).entry().id();busy=true;activity.accept(true);buttons();UiTasks.run(this,()->service.details(session,id),row->{busy=false;activity.accept(false);buttons();new ui.dialog.AuditDetailsDialog(SwingUtilities.getWindowAncestor(this),row).setVisible(true);},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);buttons();},btnSearch,tblAudit);
    }//GEN-LAST:event_btnDetailsActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblTable = new javax.swing.JLabel();
        cboTable = new javax.swing.JComboBox<>();
        lblActor = new javax.swing.JLabel();
        txtActor = new javax.swing.JTextField();
        lblKey = new javax.swing.JLabel();
        txtKey = new javax.swing.JTextField();
        lblFrom = new javax.swing.JLabel();
        txtFrom = new javax.swing.JTextField();
        lblThrough = new javax.swing.JLabel();
        txtThrough = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        pnlTable = new javax.swing.JScrollPane();
        tblAudit = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnDetails = new javax.swing.JButton();

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
            .addComponent(lblTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblActor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtActor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtKey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblThrough, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtThrough, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboTable, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblActor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtActor, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtKey, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtFrom, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblThrough, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtThrough, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addGap(28)
            .addComponent(btnSearch, 36, 36, 36)))
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnPrevious, 40, 40, 40)
            .addComponent(btnNext, 40, 40, 40)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnDetails, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("Audit log");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTable.setText("Table (blank = all)");
        lblTable.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTable.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTable.setMinimumSize(new java.awt.Dimension(0, 36));
        lblActor.setText("Actor username (exact)");
        lblActor.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtActor.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtActor.setColumns(24);
        lblKey.setText("Record key (exact)");
        lblKey.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtKey.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtKey.setColumns(24);
        lblFrom.setText("From (YYYY-MM-DD)");
        lblFrom.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFrom.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFrom.setColumns(24);
        lblThrough.setText("To (YYYY-MM-DD)");
        lblThrough.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtThrough.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtThrough.setColumns(24);
        btnSearch.setText("Search");
        btnSearch.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblAudit);
        tblAudit.setModel(new ui.model.AuditTableModel());
        tblAudit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblAudit.setRowHeight(32);
        lblStatus.setText("Read-only audit entries");
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
        btnDetails.setText("View entry");
        btnDetails.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetailsActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTable;
    private javax.swing.JComboBox<String> cboTable;
    private javax.swing.JLabel lblActor;
    private javax.swing.JTextField txtActor;
    private javax.swing.JLabel lblKey;
    private javax.swing.JTextField txtKey;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JTextField txtFrom;
    private javax.swing.JLabel lblThrough;
    private javax.swing.JTextField txtThrough;
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblAudit;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblPage;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnDetails;
    // End of variables declaration//GEN-END:variables
}
