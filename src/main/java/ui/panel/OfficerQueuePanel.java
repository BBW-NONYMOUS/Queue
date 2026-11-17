package ui.panel;
import model.*;
import service.*;
import ui.util.*;
import ui.model.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
public final class OfficerQueuePanel extends JPanel {
    private final QueueTableModel model=new QueueTableModel();private QueueService service;private Session session;
    private Consumer<Boolean> activity=active->{};private SwingPoller<QueueSnapshot> poller;private QueueRow current;
    private int page;private boolean busy,loading,refreshing,hasNext;private long office;private String filter="active";private String outcome="";
    private List<Organization> offices=List.of();
    public OfficerQueuePanel(){initComponents();TableSupport.configure(tblQueue,model,"Office tickets",3);lblOffice.setLabelFor(cboOffice);lblFilter.setLabelFor(cboFilter);
        for(String status:List.of("active","waiting","called","serving","deferred","completed","cancelled","all"))cboFilter.addItem(status);
        cboOffice.addActionListener(e->changeFilter());cboFilter.addActionListener(e->changeFilter());
        tblQueue.getSelectionModel().addListSelectionListener(e->buttons());UiStyle.primary(btnCallNext);buttons();setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(cboOffice,cboFilter,tblQueue,btnPrevious,btnNext,btnRefresh,btnRollover,btnStart,btnComplete,btnDefer,btnRequeue,btnCancel,btnIssue,btnCallNext));
    }
    public void configure(QueueService service,Session session,Consumer<Boolean> activity){this.service=service;this.session=session;this.activity=activity;}
    public void activate(){
        if(service==null||busy)return;pause();busy=true;activity.accept(true);
        UiTasks.run(this,()->service.offices(session),list->{busy=false;activity.accept(false);offices=list;loading=true;cboOffice.removeAllItems();for(var o:list)cboOffice.addItem(new IdChoice(o.id(),o.code().name()));loading=false;
            if(!list.isEmpty()){office=list.getFirst().id();restart();}else{UiStyle.status(lblStatus,"No office is available.",true);buttons();}
        },error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);buttons();},cboOffice,cboFilter,btnRefresh);
    }
    public void pause(){if(poller!=null)poller.close();}
    private void changeFilter(){if(loading||busy||service==null)return;Long id=FinanceUi.id(cboOffice);if(id==null)return;office=id;filter=(String)cboFilter.getSelectedItem();page=0;restart();}
    private QueueRow selected(){int r=tblQueue.getSelectedRow();return r<0?null:model.rowAt(tblQueue.convertRowIndexToModel(r));}
    private void show(QueueSnapshot snap){
        Long selected=selected()==null?null:selected().ticket().id();model.setRows(snap.tickets());
        if(selected!=null)for(int i=0;i<model.getRowCount();i++)if(model.rowAt(i).ticket().id()==selected){int view=tblQueue.convertRowIndexToView(i);tblQueue.setRowSelectionInterval(view,view);break;}
        current=snap.current();UiStyle.plainText(lblCurrent,current==null?"Desk: Idle":"Desk: "+current.number()+" • "+current.ticket().status().displayName());
        UiStyle.status(lblStatus,"Updated "+snap.clock().time().toLocalTime().withNano(0)+" • Previous-day active tickets: "+snap.expired()+(outcome.isEmpty()?"":" • "+outcome),false);
        lblPage.setText("Page "+(page+1));hasNext=snap.hasNext();buttons();
    }
    private void restart(){
        pause();current=null;buttons();long chosen=office;String state=filter;int chosenPage=page;
        poller=new SwingPoller<>(()->service.officeSnapshot(session,chosen,state,chosenPage),this::show,error->{current=null;model.setRows(List.of());UiStyle.status(lblStatus,error.getMessage(),true);buttons();},active->{refreshing=active;buttons();});poller.start();
    }
    private void buttons(){boolean ready=service!=null&&!busy&&!refreshing&&office>0;
        btnCallNext.setEnabled(ready&&current==null);btnStart.setEnabled(ready&&current!=null&&current.ticket().status()==QueueStatus.CALLED);
        btnComplete.setEnabled(ready&&current!=null&&current.ticket().status()==QueueStatus.SERVING);btnDefer.setEnabled(ready&&current!=null);
        QueueRow selected=selected();btnRequeue.setEnabled(ready&&selected!=null&&selected.ticket().status()==QueueStatus.DEFERRED);
        btnCancel.setEnabled(ready&&selected!=null&&selected.ticket().status()!=QueueStatus.COMPLETED&&selected.ticket().status()!=QueueStatus.CANCELLED);
        btnPrevious.setEnabled(ready&&page>0);btnNext.setEnabled(ready&&hasNext);
        btnIssue.setEnabled(ready);btnRollover.setEnabled(ready);cboOffice.setEnabled(!busy&&!refreshing);cboFilter.setEnabled(!busy&&!refreshing);btnRefresh.setEnabled(!busy&&!refreshing);
    }
    private <T>void mutate(java.util.concurrent.Callable<T> work,java.util.function.Function<T,String> message){
        if(busy)return;pause();busy=true;activity.accept(true);buttons();
        UiTasks.run(this,work,result->{busy=false;activity.accept(false);outcome=message.apply(result);UiStyle.status(lblStatus,outcome,false);restart();},error->{busy=false;activity.accept(false);UiStyle.status(lblStatus,error.getMessage(),true);current=null;buttons();},btnPrevious,btnNext,tblQueue);
    }
    private void transition(QueueRow row,QueueAction action){if(row!=null)mutate(()->service.transition(session,row,action,null),r->r.number()+" • "+r.ticket().status().displayName());}
    private void btnCallNextActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCallNextActionPerformed
        mutate(()->service.callNext(session,office),r->r.isPresent()?"Called "+r.get().number():"No waiting tickets.");
    }//GEN-LAST:event_btnCallNextActionPerformed
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnStartActionPerformed
        transition(current,QueueAction.START_SERVING);
    }//GEN-LAST:event_btnStartActionPerformed
    private void btnCompleteActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCompleteActionPerformed
        transition(current,QueueAction.COMPLETE);
    }//GEN-LAST:event_btnCompleteActionPerformed
    private void btnDeferActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnDeferActionPerformed
        transition(current,QueueAction.DEFER);
    }//GEN-LAST:event_btnDeferActionPerformed
    private void btnRequeueActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnRequeueActionPerformed
        transition(selected(),QueueAction.REQUEUE);
    }//GEN-LAST:event_btnRequeueActionPerformed
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnCancelActionPerformed
        
        QueueRow row=selected();if(row==null||busy)return;pause();busy=true;activity.accept(true);buttons();
        var dialog=new ui.dialog.ReasonActionDialog(SwingUtilities.getWindowAncestor(this),"Cancel ticket",row.number()+" • "+row.studentNumber(),"Cancel ticket",reason->service.transition(session,row,QueueAction.CANCEL,reason));dialog.setVisible(true);
        busy=false;activity.accept(false);restart();
    }//GEN-LAST:event_btnCancelActionPerformed
    private void btnIssueActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnIssueActionPerformed
        
        if(busy)return;var organization=offices.stream().filter(o->o.id()==office).findFirst().orElse(null);if(organization==null)return;
        pause();busy=true;activity.accept(true);buttons();var dialog=new ui.dialog.TicketRequestDialog(SwingUtilities.getWindowAncestor(this),service,session,organization);dialog.setVisible(true);busy=false;activity.accept(false);restart();
    }//GEN-LAST:event_btnIssueActionPerformed
    private void btnRolloverActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnRolloverActionPerformed
        mutate(()->service.closePreviousDay(session,office),n->"Closed "+n+" previous-day tickets.");
    }//GEN-LAST:event_btnRolloverActionPerformed
    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnPreviousActionPerformed
        if(!busy&&page>0){page--;restart();}
    }//GEN-LAST:event_btnPreviousActionPerformed
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnNextActionPerformed
        if(!busy){page++;restart();}
    }//GEN-LAST:event_btnNextActionPerformed
    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnRefreshActionPerformed
        if(!busy&&office>0)restart();
    }//GEN-LAST:event_btnRefreshActionPerformed

    public void focusPrimaryAction() {
        javax.swing.JRootPane root=javax.swing.SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnCallNext);
        btnCallNext.requestFocusInWindow();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblOffice = new javax.swing.JLabel();
        cboOffice = new javax.swing.JComboBox<>();
        lblFilter = new javax.swing.JLabel();
        cboFilter = new javax.swing.JComboBox<>();
        lblCurrent = new javax.swing.JLabel();
        pnlTable = new javax.swing.JScrollPane();
        tblQueue = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnRollover = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        btnComplete = new javax.swing.JButton();
        btnDefer = new javax.swing.JButton();
        btnRequeue = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnIssue = new javax.swing.JButton();
        btnCallNext = new javax.swing.JButton();

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
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboOffice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cboFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(lblCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnRollover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnComplete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnDefer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnRequeue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(btnIssue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnCallNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblOffice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboOffice, 36, 36, 36))
            .addGroup(layout.createSequentialGroup()
            .addComponent(lblFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(cboFilter, 36, 36, 36)))
            .addGap(8)
            .addComponent(lblCurrent, 32, 32, 32)
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 48, 48, 48)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnPrevious, 40, 40, 40)
            .addComponent(btnNext, 40, 40, 40)
            .addComponent(lblPage, 40, 40, 40)
            .addComponent(btnRefresh, 40, 40, 40)
            .addComponent(btnRollover, 40, 40, 40))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnStart, 40, 40, 40)
            .addComponent(btnComplete, 40, 40, 40)
            .addComponent(btnDefer, 40, 40, 40))
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnRequeue, 40, 40, 40)
            .addComponent(btnCancel, 40, 40, 40)
            .addComponent(btnIssue, 40, 40, 40)
            .addComponent(btnCallNext, 40, 40, 40))
            .addGap(8)
            .addGap(8)
        );
        lblTitle.setText("Office queue");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblOffice.setText("Office");
        lblOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboOffice.setMinimumSize(new java.awt.Dimension(0, 36));
        lblFilter.setText("Today\u2019s status");
        lblFilter.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboFilter.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cboFilter.setMinimumSize(new java.awt.Dimension(0, 36));
        lblCurrent.setText("Desk: Idle");
        lblCurrent.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblQueue);
        tblQueue.setModel(new ui.model.QueueTableModel());
        tblQueue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblQueue.setRowHeight(32);
        lblStatus.setText("Ready");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblPage.setText("Page 1");
        lblPage.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrevious.setText("Previous");
        btnPrevious.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });
        btnNext.setText("Next");
        btnNext.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        btnRefresh.setText("Refresh");
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        btnRollover.setText("Close previous day");
        btnRollover.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnRollover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRolloverActionPerformed(evt);
            }
        });
        btnStart.setText("Start current");
        btnStart.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        btnComplete.setText("Complete current");
        btnComplete.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnComplete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompleteActionPerformed(evt);
            }
        });
        btnDefer.setText("Defer current");
        btnDefer.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnDefer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeferActionPerformed(evt);
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
        btnIssue.setText("Issue for student");
        btnIssue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnIssue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIssueActionPerformed(evt);
            }
        });
        btnCallNext.setText("Call next");
        btnCallNext.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnCallNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCallNextActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblOffice;
    private javax.swing.JComboBox<ui.model.IdChoice> cboOffice;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JComboBox<String> cboFilter;
    private javax.swing.JLabel lblCurrent;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblQueue;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblPage;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRollover;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnComplete;
    private javax.swing.JButton btnDefer;
    private javax.swing.JButton btnRequeue;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnIssue;
    private javax.swing.JButton btnCallNext;
    // End of variables declaration//GEN-END:variables
}
