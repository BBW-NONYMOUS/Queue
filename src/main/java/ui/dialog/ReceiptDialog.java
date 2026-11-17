package ui.dialog;

import java.awt.Window;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import model.*;
import report.ReceiptPrintable;
import service.*;
import ui.util.*;

public final class ReceiptDialog extends javax.swing.JDialog {
    private PaymentService service; private Session session; private long paymentId; private boolean busy; private boolean loaded;
    public ReceiptDialog() { this(null, null, null, 0); }
    public ReceiptDialog(Window owner, PaymentService service, Session session, long paymentId) {
        super(owner, ModalityType.APPLICATION_MODAL); initComponents(); this.service = service; this.session = session; this.paymentId = paymentId;
        lblPreview.setLabelFor(txtPreview); getRootPane().setDefaultButton(btnPrint); UiStyle.primary(btnPrint); DialogSupport.escape(this, this::close);
        setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(txtPreview, btnClose, btnPrint)); btnPrint.setEnabled(false); setLocationRelativeTo(owner);
        if (service != null) { load(); }
    }
    private void showReceipt(ReceiptData receipt) {
        txtPreview.setText(new ReceiptPrintable(receipt).previewText()); txtPreview.setCaretPosition(0);
        UiStyle.status(lblStatus, receipt.payment().voidedAt() == null ? "Recorded payment - ready to print" : "Voided payment - printout will be marked VOID", receipt.payment().voidedAt() != null);
        loaded = true; btnPrint.setEnabled(true);
    }
    private void load() {
        busy = true; UiStyle.status(lblStatus, "Loading archived receipt...", false);
        UiTasks.run(this, () -> service.receipt(session, paymentId), result -> { busy = false; showReceipt(result); }, error -> {
            busy = false; loaded = false; btnPrint.setEnabled(false); UiStyle.status(lblStatus, error.getMessage(), true);
        }, btnPrint, btnClose);
    }
    private void printReceipt() {
        if (busy || service == null || !loaded) { return; }
        busy = true; UiStyle.status(lblStatus, "Preparing printer...", false);
        UiTasks.run(this, () -> { service.receipt(session, paymentId); return PrinterJob.getPrinterJob(); }, job -> {
            boolean accepted;
            try { accepted = job.printDialog(); }
            catch (RuntimeException ex) { busy = false; UiStyle.status(lblStatus, "The print dialog could not open. Check the printer setup.", true); return; }
            if (!accepted) { busy = false; UiStyle.status(lblStatus, "Printing cancelled. This payment is already recorded.", false); return; }
            UiTasks.run(this, () -> {
                ReceiptData latest = service.receipt(session, paymentId);
                job.setJobName("Receipt " + latest.payment().receiptReference()); job.setPrintable(new ReceiptPrintable(latest));
                try { job.print(); } catch (PrinterException ex) { throw new ServiceException("The printer could not complete the job. Reprint this receipt without recording another payment.", ex); }
                return latest;
            }, latest -> { busy = false; showReceipt(latest); UiStyle.status(lblStatus, "Receipt sent to the printer.", false); }, error -> {
                busy = false; UiStyle.status(lblStatus, error.getMessage(), true);
            }, btnPrint, btnClose);
        }, error -> { busy = false; UiStyle.status(lblStatus, error.getMessage(), true); }, btnPrint, btnClose);
    }
    private void close() { if (busy) { UiStyle.status(lblStatus, "Please wait for the current request to finish.", true); } else { dispose(); } }
    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        printReceipt();
    }//GEN-LAST:event_btnPrintActionPerformed
    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        close();
    }//GEN-LAST:event_btnCloseActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblPreview = new javax.swing.JLabel();
        pnlPreview = new javax.swing.JScrollPane();
        txtPreview = new javax.swing.JTextArea();
        lblStatus = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance | Receipt");
        setMinimumSize(new java.awt.Dimension(760, 700));
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
            .addComponent(lblPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(pnlPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(24)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(24)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnClose, 40, 40, 40)
            .addComponent(btnPrint, 40, 40, 40))
            .addGap(24)
        );
        lblTitle.setText("Payment receipt");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblPreview.setText("Archived receipt details");
        lblPreview.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlPreview.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlPreview.setPreferredSize(new java.awt.Dimension(500, 380));
        pnlPreview.setViewportView(txtPreview);
        txtPreview.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txtPreview.setEditable(false);
        txtPreview.setLineWrap(true);
        txtPreview.setWrapStyleWord(true);
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClose.setText("Close");
        btnClose.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        btnPrint.setText("Print receipt");
        btnPrint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JScrollPane pnlPreview;
    private javax.swing.JTextArea txtPreview;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPrint;
    // End of variables declaration//GEN-END:variables
}
