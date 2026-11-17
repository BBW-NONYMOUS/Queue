package ui.dialog;

import java.awt.Window;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import model.*;
import service.StudentService;
import ui.util.*;

public final class StudentEditorDialog extends javax.swing.JDialog {
    private StudentService service;
    private Session session;
    private Student original;
    private Student saved;
    private boolean busy;

    public StudentEditorDialog() { this(null, null, null, null); }
    public StudentEditorDialog(Window owner, StudentService service, Session session, Student original) {
        super(owner, ModalityType.APPLICATION_MODAL);
        initComponents();
        this.service = service; this.session = session; this.original = original;
        cboYear.setModel(new DefaultComboBoxModel<>(new String[]{"1","2","3","4","5","6"}));
        lblTitle.setText(original == null ? "Add student" : "Edit student");
        chkActive.setSelected(original == null || original.active());
        if (original != null) {
            txtStudentNumber.setText(original.studentNumber()); txtFirstName.setText(original.firstName());
            txtMiddleName.setText(original.middleName()); txtLastName.setText(original.lastName());
            txtSuffix.setText(original.suffix()); txtProgram.setText(original.program());
            cboYear.setSelectedItem(Integer.toString(original.yearLevel())); txtSection.setText(original.section());
        }
        lblStudentNumber.setLabelFor(txtStudentNumber); lblFirstName.setLabelFor(txtFirstName);
        lblMiddleName.setLabelFor(txtMiddleName); lblLastName.setLabelFor(txtLastName); lblSuffix.setLabelFor(txtSuffix);
        lblProgram.setLabelFor(txtProgram); lblYear.setLabelFor(cboYear); lblSection.setLabelFor(txtSection);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtStudentNumber, txtFirstName, txtMiddleName,
                txtLastName, txtSuffix, txtProgram, cboYear, txtSection, chkActive, btnCancel, btnSave));
        getRootPane().setDefaultButton(btnSave); UiStyle.primary(btnSave); DialogSupport.escape(this, this::cancel);
        btnSave.setEnabled(service != null); setLocationRelativeTo(owner);
    }
    public Optional<Student> savedStudent() { return Optional.ofNullable(saved); }
    private void save() {
        if (busy || service == null) { return; }
        StudentDraft draft = new StudentDraft(txtStudentNumber.getText(), txtFirstName.getText(), txtMiddleName.getText(),
                txtLastName.getText(), txtSuffix.getText(), txtProgram.getText(), cboYear.getSelectedIndex() + 1, txtSection.getText(), chkActive.isSelected());
        busy = true; UiStyle.status(lblStatus, "Saving student...", false);
        UiTasks.run(this, () -> service.save(session, original, draft), result -> { busy = false; saved = result; dispose(); },
                error -> { busy = false; UiStyle.status(lblStatus, error.getMessage(), true); },
                txtStudentNumber, txtFirstName, txtMiddleName, txtLastName, txtSuffix, txtProgram, cboYear, txtSection, chkActive, btnCancel, btnSave);
    }
    private void cancel() {
        if (busy) { UiStyle.status(lblStatus, "Please wait for the save to finish.", true); }
        else { dispose(); }
    }
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
        lblFirstName = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        lblMiddleName = new javax.swing.JLabel();
        txtMiddleName = new javax.swing.JTextField();
        lblLastName = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        lblSuffix = new javax.swing.JLabel();
        txtSuffix = new javax.swing.JTextField();
        lblProgram = new javax.swing.JLabel();
        txtProgram = new javax.swing.JTextField();
        lblYear = new javax.swing.JLabel();
        cboYear = new javax.swing.JComboBox<>();
        lblSection = new javax.swing.JLabel();
        txtSection = new javax.swing.JTextField();
        chkActive = new javax.swing.JCheckBox();
        lblHint = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Student details");
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
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblMiddleName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtMiddleName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblSuffix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtSuffix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblYear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cboYear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblLastName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtLastName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblProgram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtProgram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(chkActive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            .addComponent(lblFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtFirstName, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblMiddleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtMiddleName, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtLastName, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtSuffix, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblProgram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtProgram, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboYear, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtSection, 36, 36, 36)))
            .addGap(8)
            .addComponent(chkActive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblHint, 48, 48, 48)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnSave, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Student details");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblStudentNumber.setText("Student number *");
        lblStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudentNumber.setColumns(24);
        lblFirstName.setText("First name *");
        lblFirstName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFirstName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFirstName.setColumns(24);
        lblMiddleName.setText("Middle name");
        lblMiddleName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtMiddleName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtMiddleName.setColumns(24);
        lblLastName.setText("Last name *");
        lblLastName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtLastName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtLastName.setColumns(24);
        lblSuffix.setText("Suffix");
        lblSuffix.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSuffix.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSuffix.setColumns(24);
        lblProgram.setText("Program *");
        lblProgram.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtProgram.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtProgram.setColumns(24);
        lblYear.setText("Year level *");
        lblYear.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboYear.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSection.setText("Section *");
        lblSection.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSection.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSection.setColumns(24);
        chkActive.setText("Active student");
        chkActive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblHint.setText("<html>Uncheck Active student to deactivate. Student and clearance history remain available.</html>");
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
        btnSave.setText("Save");
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
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JLabel lblMiddleName;
    private javax.swing.JTextField txtMiddleName;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JLabel lblSuffix;
    private javax.swing.JTextField txtSuffix;
    private javax.swing.JLabel lblProgram;
    private javax.swing.JTextField txtProgram;
    private javax.swing.JLabel lblYear;
    private javax.swing.JComboBox<java.lang.String> cboYear;
    private javax.swing.JLabel lblSection;
    private javax.swing.JTextField txtSection;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    // End of variables declaration//GEN-END:variables
}
