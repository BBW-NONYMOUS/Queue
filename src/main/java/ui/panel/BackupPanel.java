package ui.panel;
import model.*;
import service.*;
import ui.util.*;
import javax.swing.*;
import java.util.function.Consumer;
public final class BackupPanel extends JPanel {
    private BackupService service;private Session session;private Consumer<Boolean> activity=active->{};private boolean busy;
    public BackupPanel(){initComponents();chkConfirm.addActionListener(e->buttons());UiStyle.primary(btnBackup);lblOutput.setLabelFor(txtOutput);txtResult.getAccessibleContext().setAccessibleName("Backup result and checksum");buttons();setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtResult,chkConfirm,btnBackup));}
    public void configure(BackupService service,Session session,Consumer<Boolean> activity){this.service=service;this.session=session;this.activity=activity;txtOutput.setText(service.outputDirectory().toString());UiStyle.status(lblStatus,service.enabled()?"Ready on the configured host. Pause schema changes before starting.":"Backup is disabled here. Configure it on the host administrator workstation.",!service.enabled());buttons();}
    private void buttons(){btnBackup.setEnabled(!busy&&service!=null&&service.enabled()&&chkConfirm.isSelected());}
    public void focusPrimaryAction(){var root=SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnBackup);chkConfirm.requestFocusInWindow();}
    private void btnBackupActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnBackupActionPerformed
        if(busy||service==null||!chkConfirm.isSelected())return;busy=true;activity.accept(true);buttons();UiStyle.status(lblStatus,"Backing up. Keep this application open until the result appears.",false);
        UiTasks.run(this,()->service.create(session),result->{busy=false;activity.accept(false);txtResult.setText("Backup complete\nFolder: "+result.directory()+"\nBytes: "+result.bytes()+"\nSHA-256: "+result.sha256()+"\nStarted (server +08:00): "+result.serverStartedAt()+"\n\nCopy this folder to protected offline storage. Test restoration on a separate MySQL 8.4 server.");UiStyle.status(lblStatus,"Backup created. A successful restore test is still required.",false);chkConfirm.setSelected(false);buttons();},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);buttons();},chkConfirm,btnBackup);
    
    }//GEN-LAST:event_btnBackupActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblHint = new javax.swing.JLabel();
        lblOutput = new javax.swing.JLabel();
        txtOutput = new javax.swing.JTextField();
        pnlResult = new javax.swing.JScrollPane();
        txtResult = new javax.swing.JTextArea();
        chkConfirm = new javax.swing.JCheckBox();
        lblStatus = new javax.swing.JLabel();
        btnBackup = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(720, 640));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnBackup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblHint, 72, 72, 72)
            .addGap(8)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtOutput, 36, 36, 36))
            .addGap(8)
            .addComponent(pnlResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(chkConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addComponent(btnBackup, 40, 40, 40)
            .addGap(16)
        );
        lblTitle.setText("Database backup");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblHint.setText("<html>Run on the configured host administrator workstation. This backup contains student and financial records. Keep schema changes paused while it runs.</html>");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblOutput.setText("Protected output folder");
        lblOutput.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOutput.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOutput.setColumns(24);
        txtOutput.setEditable(false);
        txtOutput.setFocusable(false);
        pnlResult.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlResult.setPreferredSize(new java.awt.Dimension(500, 300));
        pnlResult.setViewportView(txtResult);
        txtResult.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        chkConfirm.setText("Schema changes are paused and this folder is protected");
        chkConfirm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText("Backup is not configured.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnBackup.setText("Create backup");
        btnBackup.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblOutput;
    private javax.swing.JTextField txtOutput;
    private javax.swing.JScrollPane pnlResult;
    private javax.swing.JTextArea txtResult;
    private javax.swing.JCheckBox chkConfirm;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnBackup;
    // End of variables declaration//GEN-END:variables
}
