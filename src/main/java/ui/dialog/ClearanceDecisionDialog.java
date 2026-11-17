package ui.dialog;
import model.*;
import service.*;
import ui.util.*;
import javax.swing.*;
import java.awt.Window;
public final class ClearanceDecisionDialog extends JDialog {
    private ClearanceService service;private Session session;private ClearanceRow original;private boolean busy,saved;
    public ClearanceDecisionDialog(){this(null,null,null,null);}
    public ClearanceDecisionDialog(Window owner,ClearanceService service,Session session,ClearanceRow row){
        super(owner,ModalityType.APPLICATION_MODAL);initComponents();this.service=service;this.session=session;original=row;
        for(ClearanceStatus status:ClearanceStatus.values())cboStatus.addItem(status);
        if(row!=null){txtStudent.setText(row.student().studentNumber()+" | "+row.office().code()+" | "+row.term().code());txtBalance.setText(UiStyle.pesos(row.balance()));cboStatus.setSelectedItem(row.status());if(row.record()!=null){txtRemarks.setText(row.record().remarks());chkException.setSelected(row.record().balanceException());txtException.setText(row.record().exceptionReason());}}
        chkException.setEnabled(session!=null&&session.user().role()==Role.ADMIN);cboStatus.addActionListener(e->{if(cboStatus.getSelectedItem()!=ClearanceStatus.CLEARED)chkException.setSelected(false);exceptionState();});chkException.addActionListener(e->exceptionState());exceptionState();
        lblStatusChoice.setLabelFor(cboStatus);lblRemarks.setLabelFor(txtRemarks);lblException.setLabelFor(txtException);lblBalance.setLabelFor(txtBalance);lblStudent.setLabelFor(txtStudent);
        chkConfirm.addActionListener(e->btnSave.setEnabled(!busy&&service!=null&&chkConfirm.isSelected()));btnSave.setEnabled(false);UiStyle.primary(btnSave);getRootPane().setDefaultButton(btnSave);DialogSupport.escape(this,this::close);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(cboStatus,txtRemarks,chkException,txtException,chkConfirm,btnCancel,btnSave));setLocationRelativeTo(owner);
    }
    public boolean saved(){return saved;}
    private void exceptionState(){boolean clearing=cboStatus.getSelectedItem()==ClearanceStatus.CLEARED;
        if(!clearing)chkException.setSelected(false);boolean admin=session!=null&&session.user().role()==Role.ADMIN;
        chkException.setEnabled(admin&&clearing&&!busy);txtException.setEnabled(chkException.isSelected());txtException.setEditable(admin);
    }
    private void close(){if(!busy)dispose();}
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnSaveActionPerformed
        
        if(busy||service==null||!chkConfirm.isSelected())return;ClearanceDraft draft=new ClearanceDraft((ClearanceStatus)cboStatus.getSelectedItem(),txtRemarks.getText(),chkException.isSelected(),txtException.getText());busy=true;
        UiTasks.run(this,()->service.decide(session,original,draft),row->{busy=false;saved=true;dispose();},error->{busy=false;UiStyle.status(lblStatus,error.getMessage(),true);if(error instanceof CommitUncertainException){btnSave.setEnabled(false);chkConfirm.setEnabled(false);}},cboStatus,txtRemarks,chkException,txtException,chkConfirm,btnSave,btnCancel);
    
    }//GEN-LAST:event_btnSaveActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCancelActionPerformed
        close();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt){//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblStudent = new javax.swing.JLabel();
        txtStudent = new javax.swing.JTextField();
        lblBalance = new javax.swing.JLabel();
        txtBalance = new javax.swing.JTextField();
        lblStatusChoice = new javax.swing.JLabel();
        cboStatus = new javax.swing.JComboBox<>();
        lblRemarks = new javax.swing.JLabel();
        pnlRemarks = new javax.swing.JScrollPane();
        txtRemarks = new javax.swing.JTextArea();
        chkException = new javax.swing.JCheckBox();
        lblException = new javax.swing.JLabel();
        txtException = new javax.swing.JTextField();
        chkConfirm = new javax.swing.JCheckBox();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Clearance");
        setMinimumSize(new java.awt.Dimension(760, 680));
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
            .addComponent(lblStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRemarks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlRemarks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkException, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblException, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtException, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtBalance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStatusChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudent, 36, 36, 36))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtBalance, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStatusChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboStatus, 36, 36, 36)))
            .addGap(8)
            .addComponent(lblRemarks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlRemarks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(chkException, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblException, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtException, 36, 36, 36))
            .addGap(8)
            .addComponent(chkConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Office clearance decision");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblStudent.setText("Student / office / term");
        lblStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setColumns(24);
        txtStudent.setEditable(false);
        txtStudent.setFocusable(false);
        lblBalance.setText("Current balance");
        lblBalance.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtBalance.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtBalance.setColumns(24);
        txtBalance.setEditable(false);
        txtBalance.setFocusable(false);
        txtBalance.setHorizontalAlignment(4);
        lblStatusChoice.setText("Decision");
        lblStatusChoice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboStatus.setMinimumSize(new java.awt.Dimension(0, 36));
        lblRemarks.setText("Remarks / required hold reason");
        lblRemarks.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlRemarks.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlRemarks.setPreferredSize(new java.awt.Dimension(500, 80));
        pnlRemarks.setViewportView(txtRemarks);
        txtRemarks.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtRemarks.setEditable(true);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        chkException.setText("Approve balance exception (administrators only)");
        chkException.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblException.setText("Required exception reason");
        lblException.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtException.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtException.setColumns(24);
        chkConfirm.setText("I reviewed this student, office, term and balance");
        chkConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText("A remaining balance requires an administrator-approved exception.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnSave.setText("Save decision");
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
    private javax.swing.JLabel lblStudent;
    private javax.swing.JTextField txtStudent;
    private javax.swing.JLabel lblBalance;
    private javax.swing.JTextField txtBalance;
    private javax.swing.JLabel lblStatusChoice;
    private javax.swing.JComboBox<model.ClearanceStatus> cboStatus;
    private javax.swing.JLabel lblRemarks;
    private javax.swing.JScrollPane pnlRemarks;
    private javax.swing.JTextArea txtRemarks;
    private javax.swing.JCheckBox chkException;
    private javax.swing.JLabel lblException;
    private javax.swing.JTextField txtException;
    private javax.swing.JCheckBox chkConfirm;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
