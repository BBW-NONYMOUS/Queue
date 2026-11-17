package ui.dialog;

import java.awt.Window;
import java.util.Optional;
import model.*;
import service.*;
import ui.util.*;

public final class FineEditorDialog extends javax.swing.JDialog {
    private FineService service; private Session session; private FineRow original; private FineRow saved; private boolean busy;
    public FineEditorDialog() { this(null, null, null, null, new FinanceReferences(java.util.List.of(), java.util.List.of())); }
    public FineEditorDialog(Window owner, FineService service, Session session, FineRow original, FinanceReferences references) {
        super(owner, ModalityType.APPLICATION_MODAL); initComponents();
        this.service = service; this.session = session; this.original = original;
        FinanceUi.offices(cboOffice, references.offices(), false); FinanceUi.terms(cboTerm, references.terms(), false);
        references.terms().stream().filter(Term::current).findFirst().ifPresent(t -> FinanceUi.select(cboTerm, t.id()));
        lblTitle.setText(original == null ? "Assess fine" : "Edit fine");
        if (original != null) {
            txtStudentNumber.setText(original.studentNumber()); txtReason.setText(original.fine().reason()); txtAmount.setText(original.fine().amount().toPlainString());
            FinanceUi.select(cboOffice, original.fine().orgId()); FinanceUi.select(cboTerm, original.fine().termId());
            txtStudentNumber.setEditable(false); cboOffice.setEnabled(false); cboTerm.setEnabled(false);
        }
        lblStudentNumber.setLabelFor(txtStudentNumber); lblOffice.setLabelFor(cboOffice); lblTerm.setLabelFor(cboTerm); lblReason.setLabelFor(txtReason); lblAmount.setLabelFor(txtAmount);
        getRootPane().setDefaultButton(btnSave); DialogSupport.escape(this, this::cancel); UiStyle.primary(btnSave);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtStudentNumber, cboOffice, cboTerm, txtAmount, txtReason, btnCancel, btnSave));
        btnSave.setEnabled(service != null); setLocationRelativeTo(owner);
    }
    public Optional<FineRow> savedFine() { return Optional.ofNullable(saved); }
    public void selectContext(Long officeId, Long termId) { if (original == null) { if (officeId != null) { FinanceUi.select(cboOffice, officeId); } if (termId != null) { FinanceUi.select(cboTerm, termId); } } }
    private void save() {
        if (busy || service == null) { return; }
        FineDraft draft;
        try {
            Long office = FinanceUi.id(cboOffice), term = FinanceUi.id(cboTerm);
            draft = new FineDraft(txtStudentNumber.getText(), office == null ? 0 : office, term == null ? 0 : term, txtReason.getText(), Money.parse(txtAmount.getText()));
        } catch (ServiceException ex) { UiStyle.status(lblStatus, ex.getMessage(), true); return; }
        busy = true; UiStyle.status(lblStatus, "Saving fine...", false);
        UiTasks.run(this, () -> service.save(session, original, draft), result -> { busy = false; saved = result; dispose(); }, error -> {
            busy = false; UiStyle.status(lblStatus, error.getMessage(), true); if (error instanceof CommitUncertainException) { btnSave.setEnabled(false); }
        }, txtStudentNumber, cboOffice, cboTerm, txtReason, txtAmount, btnSave, btnCancel);
    }
    private void cancel() { if (busy) { UiStyle.status(lblStatus, "Please wait for the save to finish.", true); } else { dispose(); } }
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        save();
    }//GEN-LAST:event_btnSaveActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        cancel();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancel();
    }//GEN-LAST:event_formWindowClosing
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblStudentNumber = new javax.swing.JLabel();
        txtStudentNumber = new javax.swing.JTextField();
        lblOffice = new javax.swing.JLabel();
        cboOffice = new javax.swing.JComboBox<>();
        lblTerm = new javax.swing.JLabel();
        cboTerm = new javax.swing.JComboBox<>();
        lblAmount = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        lblReason = new javax.swing.JLabel();
        txtReason = new javax.swing.JTextField();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Fine");
        setMinimumSize(new java.awt.Dimension(760, 600));
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
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtReason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudentNumber, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboOffice, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboTerm, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtAmount, 36, 36, 36)))
            .addGap(8)
            .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtReason, 36, 36, 36)
            .addGap(8)
            .addComponent(lblHint, 48, 48, 48)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Assess fine");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblStudentNumber.setText("Student number *");
        lblStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setColumns(24);
        lblOffice.setText("Office *");
        lblOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setMinimumSize(new java.awt.Dimension(0, 36));
        lblTerm.setText("Term *");
        lblTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setMinimumSize(new java.awt.Dimension(0, 36));
        lblAmount.setText("Amount (PHP) *");
        lblAmount.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtAmount.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtAmount.setColumns(24);
        txtAmount.setHorizontalAlignment(4);
        lblReason.setText("Reason *");
        lblReason.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtReason.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtReason.setColumns(24);
        lblHint.setText("<html>Use a positive amount without commas. Office, student and term stay fixed after assessment.</html>");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnSave.setText("Save fine");
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
    private javax.swing.JLabel lblStudentNumber;
    private javax.swing.JTextField txtStudentNumber;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JComboBox<ui.model.IdChoice> cboOffice;
    private javax.swing.JLabel lblTerm;
    private javax.swing.JComboBox<ui.model.IdChoice> cboTerm;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JLabel lblReason;
    private javax.swing.JTextField txtReason;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
