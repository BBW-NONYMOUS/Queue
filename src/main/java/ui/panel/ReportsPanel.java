package ui.panel;
import model.*;
import service.*;
import ui.model.*;
import ui.util.*;
import java.util.function.Consumer;
import javax.swing.*;
public final class ReportsPanel extends JPanel {
    private final ReportTableModel model=new ReportTableModel();private ReportService service;private Session session;private Consumer<Boolean> activity=active->{};private boolean busy;
    public ReportsPanel(){initComponents();TableSupport.configure(tblReport,model,"Report preview");tblReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);for(String year:new String[]{"All","1","2","3","4","5","6"})cboYear.addItem(year);
        cboType.addActionListener(e->typeChanged());txtStudent.addActionListener(e->preview());UiStyle.primary(btnExcel);
        lblType.setLabelFor(cboType);lblOffice.setLabelFor(cboOffice);lblTerm.setLabelFor(cboTerm);lblStudent.setLabelFor(txtStudent);lblProgram.setLabelFor(txtProgram);lblYear.setLabelFor(cboYear);lblSection.setLabelFor(txtSection);lblFrom.setLabelFor(txtFrom);lblThrough.setLabelFor(txtThrough);lblOutput.setLabelFor(txtOutput);
        setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(cboType,cboOffice,cboTerm,txtStudent,txtProgram,cboYear,txtSection,txtFrom,txtThrough,chkInactive,btnPreview,tblReport,btnPdf,btnExcel));
    }
    public void configure(ReportService service,Session session,Consumer<Boolean> activity){this.service=service;this.session=session;this.activity=activity;txtOutput.setText(service.outputDirectory().toString());cboType.removeAllItems();
        for(ReportType type:ReportType.values()){Role role=session.user().role();if(role==Role.STUDENT&&type!=ReportType.CLEARANCE||role==Role.OFFICER&&type==ReportType.PAYMENTS||role==Role.CASHIER&&type==ReportType.QUEUE)continue;cboType.addItem(type);}
        boolean student=session.user().role()==Role.STUDENT;txtStudent.setEditable(!student);if(student)txtStudent.setText("Your linked record");chkInactive.setEnabled(!student);
    }
    public void activate(){if(busy||service==null)return;busy=true;activity.accept(true);UiTasks.run(this,()->service.references(session),refs->{busy=false;activity.accept(false);FinanceUi.offices(cboOffice,refs.offices(),session.user().role()!=Role.OFFICER);FinanceUi.terms(cboTerm,refs.terms(),true);typeChanged();UiStyle.status(lblStatus,"Preview shows up to 100 rows. Exports include all matching rows, up to 10,000.",false);},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);typeChanged();},controls());}
    private void typeChanged(){ReportType type=(ReportType)cboType.getSelectedItem();boolean roster=type==ReportType.STUDENTS;boolean dated=type==ReportType.QUEUE||type==ReportType.FINES||type==ReportType.PAYMENTS;
        cboOffice.setEnabled(!busy&&!roster);cboTerm.setEnabled(!busy&&!roster);txtFrom.setEnabled(!busy&&dated);txtThrough.setEnabled(!busy&&dated);if(!dated){txtFrom.setText("");txtThrough.setText("");}
        btnPreview.setEnabled(service!=null&&!busy);btnPdf.setEnabled(service!=null&&!busy);btnExcel.setEnabled(service!=null&&!busy);
    }
    private ReportFilter filter()throws ValidationException {ReportType type=(ReportType)cboType.getSelectedItem();String year=(String)cboYear.getSelectedItem();return new ReportFilter(type,type==ReportType.STUDENTS?null:FinanceUi.id(cboTerm),type==ReportType.STUDENTS?null:FinanceUi.id(cboOffice),session.user().role()==Role.STUDENT?"":txtStudent.getText(),txtProgram.getText(),year==null||year.equals("All")?null:Integer.valueOf(year),txtSection.getText(),FinanceUi.date(txtFrom.getText()),FinanceUi.date(txtThrough.getText()),chkInactive.isSelected());}
    private JComponent[] controls(){return new JComponent[]{cboType,cboOffice,cboTerm,txtStudent,txtProgram,cboYear,txtSection,txtFrom,txtThrough,chkInactive,btnPreview,btnPdf,btnExcel};}
    private void preview(){if(busy||service==null)return;ReportFilter filter;try{filter=filter();}catch(ValidationException ex){UiStyle.status(lblStatus,ex.getMessage(),true);return;}busy=true;activity.accept(true);
        UiTasks.run(this,()->service.preview(session,filter),data->{busy=false;activity.accept(false);model.show(data);TableSupport.configure(tblReport,model,"Report preview");for(int i=0;i<model.getColumnCount();i++)if(model.getColumnName(i).equals("Status"))tblReport.getColumnModel().getColumn(i).setCellRenderer(new StatusCellRenderer());tblReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);tblReport.setDefaultRenderer(java.math.BigDecimal.class,new MoneyCellRenderer());for(int i=0;i<model.getColumnCount();i++)tblReport.getColumnModel().getColumn(i).setPreferredWidth(160);UiStyle.status(lblStatus,data.rows().size()+" matching rows; showing the first "+model.getRowCount()+". Snapshot: "+data.generatedAt(),false);typeChanged();},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);typeChanged();},controls());
    }
    private void export(ExportFormat format){if(busy||service==null)return;ReportFilter filter;try{filter=filter();}catch(ValidationException ex){UiStyle.status(lblStatus,ex.getMessage(),true);return;}busy=true;activity.accept(true);UiStyle.status(lblStatus,"Exporting a fresh snapshot...",false);
        UiTasks.run(this,()->service.export(session,filter,format),file->{busy=false;activity.accept(false);UiStyle.status(lblStatus,"Saved: "+file,false);typeChanged();},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);typeChanged();},controls());
    }
    public void focusPrimaryAction(){var root=SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnPreview);cboType.requestFocusInWindow();}
    private void btnPreviewActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnPreviewActionPerformed
        preview();
    }//GEN-LAST:event_btnPreviewActionPerformed
    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnPdfActionPerformed
        export(ExportFormat.PDF);
    }//GEN-LAST:event_btnPdfActionPerformed
    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnExcelActionPerformed
        export(ExportFormat.EXCEL);
    }//GEN-LAST:event_btnExcelActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblType = new javax.swing.JLabel();
        cboType = new javax.swing.JComboBox<>();
        lblOffice = new javax.swing.JLabel();
        cboOffice = new javax.swing.JComboBox<>();
        lblTerm = new javax.swing.JLabel();
        cboTerm = new javax.swing.JComboBox<>();
        lblStudent = new javax.swing.JLabel();
        txtStudent = new javax.swing.JTextField();
        lblProgram = new javax.swing.JLabel();
        txtProgram = new javax.swing.JTextField();
        lblYear = new javax.swing.JLabel();
        cboYear = new javax.swing.JComboBox<>();
        lblSection = new javax.swing.JLabel();
        txtSection = new javax.swing.JTextField();
        lblFrom = new javax.swing.JLabel();
        txtFrom = new javax.swing.JTextField();
        lblThrough = new javax.swing.JLabel();
        txtThrough = new javax.swing.JTextField();
        chkInactive = new javax.swing.JCheckBox();
        btnPreview = new javax.swing.JButton();
        pnlTable = new javax.swing.JScrollPane();
        tblReport = new javax.swing.JTable();
        lblOutput = new javax.swing.JLabel();
        txtOutput = new javax.swing.JTextField();
        lblStatus = new javax.swing.JLabel();
        btnPdf = new javax.swing.JButton();
        btnExcel = new javax.swing.JButton();

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
            .addComponent(lblType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8))
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblProgram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtProgram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboYear, 88, 88, 88))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8))
            .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblThrough, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtThrough, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
            .addComponent(chkInactive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(txtOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnPdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnExcel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboType, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboOffice, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboTerm, 36, 36, 36)))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtStudent, 36, 36, 36))
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
            .addComponent(chkInactive, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addGap(28)
            .addComponent(btnPreview, 36, 36, 36)))
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(txtOutput, 32, 32, 32)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnPdf, 40, 40, 40)
            .addComponent(btnExcel, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("Reports");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblType.setText("Report");
        lblType.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboType.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboType.setMinimumSize(new java.awt.Dimension(0, 36));
        lblOffice.setText("Office");
        lblOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setMinimumSize(new java.awt.Dimension(0, 36));
        lblTerm.setText("Term");
        lblTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboTerm.setMinimumSize(new java.awt.Dimension(0, 36));
        lblStudent.setText("Student number (exact)");
        lblStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtStudent.setColumns(24);
        lblProgram.setText("Program (exact)");
        lblProgram.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtProgram.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtProgram.setColumns(24);
        lblYear.setText("Year");
        lblYear.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboYear.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboYear.setMinimumSize(new java.awt.Dimension(0, 36));
        lblSection.setText("Section (exact)");
        lblSection.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSection.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtSection.setColumns(24);
        lblFrom.setText("From (YYYY-MM-DD)");
        lblFrom.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFrom.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtFrom.setColumns(24);
        lblThrough.setText("To (YYYY-MM-DD)");
        lblThrough.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtThrough.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtThrough.setColumns(24);
        chkInactive.setText("Include inactive students");
        chkInactive.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPreview.setText("Preview");
        btnPreview.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewActionPerformed(evt);
            }
        });
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 120));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 200));
        pnlTable.setViewportView(tblReport);
        tblReport.setModel(new ui.model.ReportTableModel());
        tblReport.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblReport.setRowHeight(32);
        lblOutput.setText("Output folder");
        lblOutput.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOutput.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtOutput.setColumns(24);
        txtOutput.setEditable(false);
        txtOutput.setFocusable(false);
        lblStatus.setText("Choose filters, preview or export.");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPdf.setText("Export PDF");
        btnPdf.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfActionPerformed(evt);
            }
        });
        btnExcel.setText("Export Excel");
        btnExcel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcelActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JComboBox<model.ReportType> cboType;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JComboBox<ui.model.IdChoice> cboOffice;
    private javax.swing.JLabel lblTerm;
    private javax.swing.JComboBox<ui.model.IdChoice> cboTerm;
    private javax.swing.JLabel lblStudent;
    private javax.swing.JTextField txtStudent;
    private javax.swing.JLabel lblProgram;
    private javax.swing.JTextField txtProgram;
    private javax.swing.JLabel lblYear;
    private javax.swing.JComboBox<String> cboYear;
    private javax.swing.JLabel lblSection;
    private javax.swing.JTextField txtSection;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JTextField txtFrom;
    private javax.swing.JLabel lblThrough;
    private javax.swing.JTextField txtThrough;
    private javax.swing.JCheckBox chkInactive;
    private javax.swing.JButton btnPreview;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblReport;
    private javax.swing.JLabel lblOutput;
    private javax.swing.JTextField txtOutput;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnPdf;
    private javax.swing.JButton btnExcel;
    // End of variables declaration//GEN-END:variables
}
