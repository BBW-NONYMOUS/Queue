package ui.panel;
import model.*;
import service.*;
import ui.util.*;
import ui.model.*;
import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;
public final class StudentDashboardPanel extends JPanel {
    private final QueueTableModel tickets=new QueueTableModel();private final StudentOfficeTableModel offices=new StudentOfficeTableModel();
    private QueueService service;private Session session;private Consumer<Boolean> activity=active->{};private Consumer<String> notify=text->{};
    private SwingPoller<StudentQueueSnapshot> poller;private StudentCallTracker tracker;private StudentQueueSnapshot latest;private boolean busy,refreshing,offline;private String notification="Queue connected";
    public StudentDashboardPanel(){initComponents();TableSupport.configure(tblTickets,tickets,"Your tickets",3);TableSupport.configure(tblOffices,offices,"Office availability and current-term clearance",2,4,6);tblOffices.setDefaultRenderer(java.math.BigDecimal.class,new MoneyCellRenderer());tblOffices.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);TableSupport.widths(tblOffices,90,120,85,64,80,110,140);
        tblTickets.getSelectionModel().addListSelectionListener(e->buttons());tblOffices.getSelectionModel().addListSelectionListener(e->buttons());chkSound.setSelected(true);UiStyle.primary(btnIssue);buttons();setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(chkSound,tblOffices,tblTickets,btnRefresh,btnRequeue,btnCancel,btnIssue));}
    public void configure(QueueService service,Session session,Consumer<Boolean> activity,Consumer<String> notify){
        if(this.service!=null)return;this.service=service;this.session=session;this.activity=activity;this.notify=notify;tracker=new StudentCallTracker(session.user().lastLoginAt());
        poller=new SwingPoller<>(()->service.studentSnapshot(session),this::show,error->{latest=null;offline=true;UiStyle.status(lblStatus,error.getMessage(),true);notify.accept("Queue offline. Check your ticket at the office.");buttons();},active->{refreshing=active;buttons();});poller.start();
    }
    public void close(){if(poller!=null)poller.close();}
    private QueueRow selected(){int r=tblTickets.getSelectedRow();return r<0?null:tickets.rowAt(tblTickets.convertRowIndexToModel(r));}
    private BoardRow office(){int r=tblOffices.getSelectedRow();return r<0?null:offices.rowAt(tblOffices.convertRowIndexToModel(r));}
    private void show(StudentQueueSnapshot snapshot){
        if(offline){offline=false;notify.accept(notification);}
        Long ticketId=selected()==null?null:selected().ticket().id();Long officeId=office()==null?null:office().orgId();latest=snapshot;tickets.setRows(snapshot.tickets());offices.clearances(snapshot.clearances());offices.setRows(snapshot.offices());
        for(int i=0;i<tickets.getRowCount();i++)if(Objects.equals(ticketId,tickets.rowAt(i).ticket().id())){int r=tblTickets.convertRowIndexToView(i);tblTickets.setRowSelectionInterval(r,r);}
        for(int i=0;i<offices.getRowCount();i++)if(Objects.equals(officeId,offices.rowAt(i).orgId())){int r=tblOffices.convertRowIndexToView(i);tblOffices.setRowSelectionInterval(r,r);}
        UiStyle.plainText(lblTerm,snapshot.term()==null?"No current term — ticket issue unavailable":snapshot.term().name()+" • "+snapshot.clock().day());
        var calls=tracker.accept(snapshot);if(!calls.isEmpty()){
            String message="Call recorded: "+calls.stream().map(QueueCallEvent::number).distinct().collect(java.util.stream.Collectors.joining(", "))+". Check ticket status below.";
            notification=message;UiStyle.status(lblAlert,message,false);notify.accept(message);if(chkSound.isSelected())java.awt.Toolkit.getDefaultToolkit().beep();
        }
        UiStyle.status(lblStatus,"Updated "+snapshot.clock().time().toLocalTime().withNano(0)+" • Today’s latest 200 tickets. Select an office to request a ticket.",false);buttons();
    }
    private void buttons(){QueueRow row=selected();BoardRow office=office();boolean ready=service!=null&&!busy&&!refreshing&&latest!=null;
        btnIssue.setEnabled(ready&&latest.term()!=null&&office!=null&&office.active()&&office.open());
        btnRequeue.setEnabled(ready&&row!=null&&row.ticket().status()==QueueStatus.DEFERRED);
        btnCancel.setEnabled(ready&&row!=null&&(row.ticket().status()==QueueStatus.WAITING||row.ticket().status()==QueueStatus.DEFERRED));
        btnRefresh.setEnabled(service!=null&&!busy&&!refreshing);
    }
    private void resume(){busy=false;activity.accept(false);poller.pause();buttons();poller.start();}
    private void btnIssueActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnIssueActionPerformed
        
        BoardRow choice=office();if(choice==null||busy)return;busy=true;activity.accept(true);buttons();
        Organization organization=new Organization(choice.orgId(),choice.office(),choice.name(),choice.open(),choice.active(),null,null);
        var dialog=new ui.dialog.TicketRequestDialog(SwingUtilities.getWindowAncestor(this),service,session,organization);dialog.setVisible(true);
        dialog.result().ifPresent(r->{UiStyle.status(lblAlert,"Your ticket: "+r.number()+" • "+r.ticket().status().displayName(),false);});resume();
    }//GEN-LAST:event_btnIssueActionPerformed
    private void btnRequeueActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnRequeueActionPerformed
        
        QueueRow row=selected();if(row==null||busy)return;busy=true;activity.accept(true);buttons();
        UiTasks.run(this,()->service.transition(session,row,QueueAction.REQUEUE,null),result->{UiStyle.status(lblAlert,result.number()+" requeued at the end of the waiting queue.",false);resume();},error->{UiStyle.status(lblAlert,error.getMessage(),true);resume();},tblTickets,tblOffices);
    }//GEN-LAST:event_btnRequeueActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCancelActionPerformed
        
        QueueRow row=selected();if(row==null||busy)return;busy=true;activity.accept(true);buttons();
        var dialog=new ui.dialog.ReasonActionDialog(SwingUtilities.getWindowAncestor(this),"Cancel your ticket",row.number(),"Cancel ticket",reason->service.transition(session,row,QueueAction.CANCEL,reason));dialog.setVisible(true);resume();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnRefreshActionPerformed
        if(poller!=null)poller.refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    public void focusPrimaryAction() {
        javax.swing.JRootPane root=javax.swing.SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnIssue);
        btnIssue.requestFocusInWindow();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblTerm = new javax.swing.JLabel();
        lblAlert = new javax.swing.JLabel();
        chkSound = new javax.swing.JCheckBox();
        lblOffices = new javax.swing.JLabel();
        pnlOffices = new javax.swing.JScrollPane();
        tblOffices = new javax.swing.JTable();
        lblTickets = new javax.swing.JLabel();
        pnlTickets = new javax.swing.JScrollPane();
        tblTickets = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();
        btnRequeue = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnIssue = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(720, 640));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblAlert, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chkSound, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblOffices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlOffices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblTickets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlTickets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnRequeue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnIssue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblAlert, 48, 48, 48)
            .addGap(8)
            .addComponent(chkSound, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblOffices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlOffices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblTickets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlTickets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnRefresh, 40, 40, 40)
            .addComponent(btnRequeue, 40, 40, 40)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnIssue, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("Your queue tickets");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTerm.setText("Current term");
        lblTerm.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblAlert.setText("Your calls will appear here.");
        lblAlert.setFont(new java.awt.Font("Segoe UI", 0, 14));
        chkSound.setText("Play a sound when called");
        chkSound.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblOffices.setText("Choose an office");
        lblOffices.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlOffices.setMinimumSize(new java.awt.Dimension(0, 112));
        pnlOffices.setPreferredSize(new java.awt.Dimension(500, 190));
        pnlOffices.setViewportView(tblOffices);
        tblOffices.setModel(new ui.model.StudentOfficeTableModel());
        tblOffices.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblOffices.setRowHeight(32);
        lblTickets.setText("Your tickets today");
        lblTickets.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlTickets.setMinimumSize(new java.awt.Dimension(0, 112));
        pnlTickets.setPreferredSize(new java.awt.Dimension(500, 190));
        pnlTickets.setViewportView(tblTickets);
        tblTickets.setModel(new ui.model.QueueTableModel());
        tblTickets.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblTickets.setRowHeight(32);
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRefresh.setText("Refresh");
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        btnRequeue.setText("Requeue selected");
        btnRequeue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRequeue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRequeueActionPerformed(evt);
            }
        });
        btnCancel.setText("Cancel selected");
        btnCancel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        btnIssue.setText("Issue ticket");
        btnIssue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnIssue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIssueActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTerm;
    private javax.swing.JLabel lblAlert;
    private javax.swing.JCheckBox chkSound;
    private javax.swing.JLabel lblOffices;
    private javax.swing.JScrollPane pnlOffices;
    private javax.swing.JTable tblOffices;
    private javax.swing.JLabel lblTickets;
    private javax.swing.JScrollPane pnlTickets;
    private javax.swing.JTable tblTickets;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRequeue;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnIssue;
    // End of variables declaration//GEN-END:variables
}
