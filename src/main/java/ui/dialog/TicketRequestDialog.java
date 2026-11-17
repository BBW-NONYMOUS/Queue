package ui.dialog;
import java.awt.Window;
import java.util.UUID;
import model.*;
import service.*;
import ui.util.*;
/** A request key survives retries for this dialog; recovery first resolves an uncertain result. */
public final class TicketRequestDialog extends javax.swing.JDialog {
    private final UUID request=UUID.randomUUID(); private QueueService service;private Session session;private long office;
    private boolean busy,uncertain;private QueueRow result;
    public TicketRequestDialog(){this(null,null,null,null);}
    public TicketRequestDialog(Window owner,QueueService service,Session session,Organization organization){
        super(owner,ModalityType.APPLICATION_MODAL);initComponents();this.service=service;this.session=session;
        if(organization!=null){office=organization.id();txtOffice.setText(organization.code().name());}
        if(session!=null&&session.user().role()==Role.STUDENT){txtStudent.setEditable(false);txtStudent.setText("Your linked student record");}
        lblStudent.setLabelFor(txtStudent);lblOffice.setLabelFor(txtOffice);btnCheck.setEnabled(false);btnIssue.setEnabled(service!=null);
        UiStyle.primary(btnIssue);getRootPane().setDefaultButton(btnIssue);DialogSupport.escape(this,this::cancel);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtStudent,btnCancel,btnCheck,btnIssue));setLocationRelativeTo(owner);
    }
    public java.util.Optional<QueueRow> result(){return java.util.Optional.ofNullable(result);}
    private void issue(){
        if(busy||service==null||uncertain)return;String number=txtStudent.getText();busy=true;
        UiTasks.run(this,()->service.issue(session,office,number,request),row->{busy=false;result=row;dispose();},error->{
            busy=false;UiStyle.status(lblStatus,error.getMessage(),true);
            if(error instanceof CommitUncertainException){uncertain=true;btnIssue.setEnabled(false);btnCheck.setEnabled(true);getRootPane().setDefaultButton(btnCheck);}
        },txtStudent,btnIssue,btnCancel,btnCheck);
    }
    private void check(){
        if(busy||service==null||!uncertain)return;busy=true;
        UiTasks.run(this,()->service.recoverIssue(session,office,request),row->{busy=false;
            if(row.isPresent()){result=row.get();dispose();}else{uncertain=false;btnCheck.setEnabled(false);btnIssue.setEnabled(true);getRootPane().setDefaultButton(btnIssue);UiStyle.status(lblStatus,"No ticket was saved for this request. You may retry Issue ticket.",false);}
        },error->{busy=false;btnIssue.setEnabled(false);UiStyle.status(lblStatus,error.getMessage(),true);},btnCheck,btnCancel,btnIssue,txtStudent);
    }
    private void cancel(){if(!busy)dispose();}
    private void btnIssueActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnIssueActionPerformed
        issue();
    }//GEN-LAST:event_btnIssueActionPerformed
    private void btnCheckActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCheckActionPerformed
        check();
    }//GEN-LAST:event_btnCheckActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCancelActionPerformed
        cancel();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt){//GEN-FIRST:event_formWindowClosing
        cancel();
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblOffice = new javax.swing.JLabel();
        txtOffice = new javax.swing.JTextField();
        lblStudent = new javax.swing.JLabel();
        txtStudent = new javax.swing.JTextField();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnCheck = new javax.swing.JButton();
        btnIssue = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Ticket");
        setMinimumSize(new java.awt.Dimension(720, 560));
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
            .addComponent(lblOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnIssue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(16)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtOffice, 36, 36, 36))
            .addGap(8)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudent, 36, 36, 36))
            .addGap(8)
            .addComponent(lblHint, 64, 64, 64)
            .addGap(8)
            .addComponent(lblStatus, 96, 96, 96)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnCheck, 40, 40, 40)
            .addComponent(btnIssue, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Request a queue ticket");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblOffice.setText("Office");
        lblOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOffice.setColumns(24);
        txtOffice.setEditable(false);
        txtOffice.setFocusable(false);
        lblStudent.setText("Student number *");
        lblStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setColumns(24);
        lblHint.setText("<html>Tickets are valid today. If the result is uncertain, use Check request before trying again.</html>");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.setText("Close");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnCheck.setText("Check request");
        btnCheck.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckActionPerformed(evt);
            }
        });
        btnIssue.setText("Issue ticket");
        btnIssue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnIssue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIssueActionPerformed(evt);
            }
        });
        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JTextField txtOffice;
    private javax.swing.JLabel lblStudent;
    private javax.swing.JTextField txtStudent;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCheck;
    private javax.swing.JButton btnIssue;
    // End of variables declaration//GEN-END:variables
}
