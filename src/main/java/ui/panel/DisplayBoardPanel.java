package ui.panel;
import model.*;
import service.*;
import ui.util.*;
import ui.model.*;
import javax.swing.*;
public final class DisplayBoardPanel extends JPanel {
    private final BoardTableModel model=new BoardTableModel();private SwingPoller<java.util.List<BoardRow>> poller;private Runnable fullscreen=()->{},leave=()->{};
    public DisplayBoardPanel(){initComponents();TableSupport.configure(tblBoard,model,"Now serving board",2,4);tblBoard.setAutoCreateRowSorter(false);tblBoard.setFocusable(false);TableSupport.widths(tblBoard,150,280,170,100,160);
        tblBoard.addComponentListener(new java.awt.event.ComponentAdapter(){@Override public void componentResized(java.awt.event.ComponentEvent e){resizeBoard();}});UiStyle.primary(btnFullscreen);setFocusTraversalPolicyProvider(true);setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(btnExit,btnFullscreen));}
    private void resizeBoard(){
        int row=Math.max(40,(pnlTable.getViewport().getHeight()-2)/7);tblBoard.setRowHeight(row);
        int size=Math.max(18,Math.min(54,row/3));
        while(size>14){
            java.awt.Font font=new java.awt.Font("Segoe UI",java.awt.Font.BOLD,size);var metrics=tblBoard.getFontMetrics(font);boolean fits=true;
            for(int c=0;c<model.getColumnCount();c++)for(int r=0;r<model.getRowCount();r++)
                if(metrics.stringWidth(String.valueOf(model.getValueAt(r,c)))>tblBoard.getColumnModel().getColumn(c).getWidth()-16)fits=false;
            if(fits)break;size--;
        }
        tblBoard.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,size));
    }
    public void activate(QueueService service,Session session,Runnable fullscreen,Runnable leave){
        pause();this.fullscreen=fullscreen;this.leave=leave;
        poller=new SwingPoller<>(()->service.board(session),rows->{model.setRows(rows);resizeBoard();UiStyle.status(lblStatus,"Live • refreshes every 3 seconds",false);},error->{model.setRows(java.util.List.of());UiStyle.status(lblStatus,"OFFLINE — serving numbers unavailable. "+error.getMessage(),true);});poller.start();
    }
    public void pause(){if(poller!=null)poller.close();}
    private void btnFullscreenActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnFullscreenActionPerformed
        fullscreen.run();
    }//GEN-LAST:event_btnFullscreenActionPerformed
    private void btnExitActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnExitActionPerformed
        leave.run();
    }//GEN-LAST:event_btnExitActionPerformed

    public void focusPrimaryAction() {
        javax.swing.JRootPane root=javax.swing.SwingUtilities.getRootPane(this);if(root!=null)root.setDefaultButton(btnFullscreen);
        btnFullscreen.requestFocusInWindow();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblHint = new javax.swing.JLabel();
        pnlTable = new javax.swing.JScrollPane();
        tblBoard = new javax.swing.JTable();
        lblStatus = new javax.swing.JLabel();
        btnExit = new javax.swing.JButton();
        btnFullscreen = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(720, 640));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
            .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(btnFullscreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnExit, 40, 40, 40)
            .addComponent(btnFullscreen, 40, 40, 40))
            .addGap(16)
        );
        lblTitle.setText("NOW SERVING");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblHint.setText("Please proceed when your ticket is called.");
        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlTable.setMinimumSize(new java.awt.Dimension(0, 160));
        pnlTable.setPreferredSize(new java.awt.Dimension(500, 240));
        pnlTable.setViewportView(tblBoard);
        tblBoard.setModel(new ui.model.BoardTableModel());
        tblBoard.setFont(new java.awt.Font("Segoe UI", 0, 14));
        tblBoard.setRowHeight(32);
        lblStatus.setText("Connecting\u2026");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnExit.setText("Exit display (Esc)");
        btnExit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        btnFullscreen.setText("Full screen");
        btnFullscreen.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnFullscreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFullscreenActionPerformed(evt);
            }
        });
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblHint;
    private javax.swing.JScrollPane pnlTable;
    private javax.swing.JTable tblBoard;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnFullscreen;
    // End of variables declaration//GEN-END:variables
}
