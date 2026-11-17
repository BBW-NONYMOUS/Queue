package ui.dialog;

import java.awt.Window;
import java.util.List;
import java.util.Optional;
import model.*;
import service.*;
import ui.util.*;

public final class TermSettingsDialog extends javax.swing.JDialog {
    private TermService service; private Session session; private List<Term> terms = List.of(); private Term original; private Term saved; private boolean busy;
    public TermSettingsDialog() { this(null, null, null, List.of()); }
    public TermSettingsDialog(Window owner, TermService service, Session session, List<Term> terms) {
        super(owner, ModalityType.APPLICATION_MODAL); initComponents(); this.service = service; this.session = session; this.terms = List.copyOf(terms);
        FinanceUi.terms(cboTerm, terms, true); if (cboTerm.getItemCount() > 0) { cboTerm.insertItemAt(new ui.model.IdChoice(null, "New term"), 0); cboTerm.removeItemAt(1); }
        cboTerm.addActionListener(event -> select()); cboTerm.setSelectedIndex(0); select();
        lblTerm.setLabelFor(cboTerm); lblCode.setLabelFor(txtCode); lblName.setLabelFor(txtName); lblStarts.setLabelFor(txtStarts); lblEnds.setLabelFor(txtEnds);
        chkActive.addActionListener(event -> { if (!chkActive.isSelected()) { chkCurrent.setSelected(false); } });
        chkCurrent.addActionListener(event -> { if (chkCurrent.isSelected()) { chkActive.setSelected(true); } });
        getRootPane().setDefaultButton(btnSave); UiStyle.primary(btnSave); DialogSupport.escape(this, this::cancel);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(cboTerm, txtCode, txtName, txtStarts, txtEnds, chkActive, chkCurrent, btnCancel, btnSave));
        btnSave.setEnabled(service != null); setLocationRelativeTo(owner);
    }
    private void select() {
        Long id = FinanceUi.id(cboTerm); original = terms.stream().filter(t -> java.util.Objects.equals(t.id(), id)).findFirst().orElse(null);
        txtCode.setText(original == null ? "" : original.code()); txtName.setText(original == null ? "" : original.name());
        txtStarts.setText(original == null ? "" : original.startsOn().toString()); txtEnds.setText(original == null ? "" : original.endsOn().toString());
        chkActive.setSelected(original != null && original.active()); chkCurrent.setSelected(original != null && original.current());
    }
    public Optional<Term> savedTerm() { return Optional.ofNullable(saved); }
    private void save() {
        if (busy || service == null) { return; }
        TermDraft draft;
        try { draft = new TermDraft(txtCode.getText(), txtName.getText(), FinanceUi.date(txtStarts.getText()), FinanceUi.date(txtEnds.getText()), chkActive.isSelected(), chkCurrent.isSelected()); }
        catch (ServiceException ex) { UiStyle.status(lblStatus, ex.getMessage(), true); return; }
        Term expected = original; busy = true; UiStyle.status(lblStatus, "Saving term settings...", false);
        UiTasks.run(this, () -> service.save(session, expected, draft), result -> { busy = false; saved = result; dispose(); }, error -> {
            busy = false; UiStyle.status(lblStatus, error.getMessage(), true); if (error instanceof CommitUncertainException) { btnSave.setEnabled(false); }
        }, cboTerm, txtCode, txtName, txtStarts, txtEnds, chkActive, chkCurrent, btnSave, btnCancel);
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
        lblTerm = new javax.swing.JLabel();
        cboTerm = new javax.swing.JComboBox<>();
        lblCode = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblStarts = new javax.swing.JLabel();
        txtStarts = new javax.swing.JTextField();
        lblEnds = new javax.swing.JLabel();
        txtEnds = new javax.swing.JTextField();
        chkActive = new javax.swing.JCheckBox();
        chkCurrent = new javax.swing.JCheckBox();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Terms");
        setMinimumSize(new java.awt.Dimension(800, 680));
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
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStarts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtStarts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblEnds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtEnds, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(chkActive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(chkCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboTerm, 36, 36, 36))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtCode, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtName, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStarts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStarts, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblEnds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtEnds, 36, 36, 36)))
            .addGap(8)
            .addComponent(chkActive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(chkCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        lblTitle.setText("Term settings");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTerm.setText("Choose an existing term, or New term");
        lblTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setMinimumSize(new java.awt.Dimension(0, 36));
        lblCode.setText("Term code *");
        lblCode.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtCode.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtCode.setColumns(24);
        lblName.setText("Term name *");
        lblName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtName.setColumns(24);
        lblStarts.setText("Starts on (YYYY-MM-DD) *");
        lblStarts.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStarts.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStarts.setColumns(24);
        lblEnds.setText("Ends on (YYYY-MM-DD) *");
        lblEnds.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtEnds.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtEnds.setColumns(24);
        chkActive.setText("Active term");
        chkActive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        chkCurrent.setText("Current term");
        chkCurrent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblHint.setText("<html>Use registrar-confirmed dates. A term with records keeps its code and dates.</html>");
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
        btnSave.setText("Save term");
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
    private javax.swing.JLabel lblTerm;
    private javax.swing.JComboBox<ui.model.IdChoice> cboTerm;
    private javax.swing.JLabel lblCode;
    private javax.swing.JTextField txtCode;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField txtName;
    private javax.swing.JLabel lblStarts;
    private javax.swing.JTextField txtStarts;
    private javax.swing.JLabel lblEnds;
    private javax.swing.JTextField txtEnds;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JCheckBox chkCurrent;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
