package ui.dialog;
import model.*;
import ui.util.*;
import java.awt.Window;
public final class AuditDetailsDialog extends javax.swing.JDialog {
    public AuditDetailsDialog(){this(null,null);}
    public AuditDetailsDialog(Window owner,AuditRow row){super(owner,ModalityType.APPLICATION_MODAL);initComponents();
        if(row!=null){var a=row.entry();txtDetails.setText("Entry: "+a.id()+"\nServer time: "+a.occurredAt()+"\nActor: "+row.actorName()+"\nDatabase account: "+a.databaseAccount()+"\nConnection: "+a.connectionId()+"\nRequest: "+a.requestId()+"\nAction: "+a.action()+"\nTable: "+a.tableName()+"\nRecord: "+a.recordKey()+"\n\nBEFORE\n"+(a.oldValuesJson()==null?"No prior row":a.oldValuesJson())+"\n\nAFTER\n"+a.newValuesJson());txtDetails.setCaretPosition(0);}
        txtDetails.getAccessibleContext().setAccessibleName("Audit entry before and after values");DialogSupport.escape(this,this::dispose);getRootPane().setDefaultButton(btnClose);UiStyle.primary(btnClose);setLocationRelativeTo(owner);
    }
    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt){//GEN-FIRST:event_formWindowClosing
        dispose();
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        pnlDetails = new javax.swing.JScrollPane();
        txtDetails = new javax.swing.JTextArea();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Audit entry");
        setMinimumSize(new java.awt.Dimension(880, 700));
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
            .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(btnClose, 40, 40, 40)
            .addGap(24)
        );
        lblTitle.setText("Audit entry");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        pnlDetails.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlDetails.setPreferredSize(new java.awt.Dimension(500, 480));
        pnlDetails.setViewportView(txtDetails);
        txtDetails.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtDetails.setEditable(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        btnClose.setText("Close");
        btnClose.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane pnlDetails;
    private javax.swing.JTextArea txtDetails;
    private javax.swing.JButton btnClose;
    // End of variables declaration//GEN-END:variables
}
