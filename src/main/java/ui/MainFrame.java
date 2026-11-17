package ui;

import java.awt.CardLayout;
import java.util.Objects;
import model.Session;
import service.AuthService;
import service.NavigationService;
import service.NavigationService.Destination;
import ui.panel.AccountPanel;
import ui.panel.DashboardPanel;
import ui.panel.StudentsPanel;
import ui.panel.OfficesPanel;
import ui.panel.UsersPanel;
import service.StudentService;
import service.OfficeService;
import service.UserService;
import service.FineService;
import service.PaymentService;
import service.TermService;
import ui.panel.FinesPanel;
import ui.panel.PaymentsPanel;
import ui.util.UiStyle;
import ui.util.UiTasks;

/** One application shell. Future screens register JPanel cards in this frame. */
public final class MainFrame extends javax.swing.JFrame {
    private final DashboardPanel dashboard = new DashboardPanel();
    private final AccountPanel account = new AccountPanel();
    private final StudentsPanel students = new StudentsPanel();
    private final OfficesPanel offices = new OfficesPanel();
    private final UsersPanel users = new UsersPanel();
    private final FinesPanel fines = new FinesPanel();
    private final PaymentsPanel payments = new PaymentsPanel();
    private final ui.panel.OfficerQueuePanel queuePanel = new ui.panel.OfficerQueuePanel();
    private final ui.panel.StudentDashboardPanel studentDashboard = new ui.panel.StudentDashboardPanel();
    private final ui.panel.DisplayBoardPanel board = new ui.panel.DisplayBoardPanel();
    private final ui.panel.ClearancePanel clearancePanel=new ui.panel.ClearancePanel();
    private final ui.panel.ReportsPanel reportsPanel=new ui.panel.ReportsPanel();
    private final ui.panel.AuditPanel auditPanel=new ui.panel.AuditPanel();
    private final ui.panel.BackupPanel backupPanel=new ui.panel.BackupPanel();
    private service.ClearanceService clearanceService;
    private service.ReportService reportService;
    private service.AuditService auditService;
    private service.BackupService backupService;
    private service.QueueService queueService;
    private boolean fullScreen;
    private Destination activeDestination=Destination.WORKSPACE;
    private java.awt.Rectangle windowBounds;
    private int windowState;
    private FineService fineService;
    private PaymentService paymentService;
    private TermService termService;
    private StudentService studentService;
    private OfficeService officeService;
    private UserService userService;
    private AuthService auth;
    private NavigationService navigation;
    private Session session;
    private Runnable signedOut;
    private Runnable exit;
    private boolean busy;

    public MainFrame() {
        initComponents();
        pnlContent.add(dashboard, Destination.WORKSPACE.name());
        pnlContent.add(queuePanel, Destination.QUEUE.name());
        pnlContent.add(board, Destination.BOARD.name());
        pnlContent.add(clearancePanel,Destination.CLEARANCE.name());pnlContent.add(reportsPanel,Destination.REPORTS.name());
        pnlContent.add(auditPanel,Destination.AUDIT.name());pnlContent.add(backupPanel,Destination.BACKUP.name());
        pnlContent.add(account, Destination.ACCOUNT.name());
        pnlContent.add(students, Destination.STUDENTS.name());
        pnlContent.add(offices, Destination.OFFICES.name());
        pnlContent.add(users, Destination.USERS.name());
        pnlContent.add(fines, Destination.FINES.name());
        pnlContent.add(payments, Destination.PAYMENTS.name());
        setFocusTraversalPolicy(new javax.swing.LayoutFocusTraversalPolicy());
        btnStudents.setVisible(false); btnOffices.setVisible(false); btnUsers.setVisible(false); btnFines.setVisible(false); btnPayments.setVisible(false); btnQueue.setVisible(false); btnBoard.setVisible(false);btnClearance.setVisible(false);btnReports.setVisible(false);btnAudit.setVisible(false);btnBackup.setVisible(false);
        enableNavigation(false);
        setLocationRelativeTo(null);
    }

    public MainFrame(AuthService auth, NavigationService navigation, StudentService studentService, OfficeService officeService,
            UserService userService, FineService fineService, PaymentService paymentService, TermService termService, service.QueueService queueService,service.ClearanceService clearanceService,service.ReportService reportService,service.AuditService auditService,service.BackupService backupService,
            Session session, Runnable signedOut, Runnable exit) {
        this();
        this.auth = Objects.requireNonNull(auth);
        this.navigation = Objects.requireNonNull(navigation);
        this.studentService = Objects.requireNonNull(studentService);
        this.officeService = Objects.requireNonNull(officeService);
        this.userService = Objects.requireNonNull(userService);
        this.fineService = Objects.requireNonNull(fineService);
        this.paymentService = Objects.requireNonNull(paymentService);
        this.termService = Objects.requireNonNull(termService);
        this.queueService = Objects.requireNonNull(queueService);
        this.clearanceService=Objects.requireNonNull(clearanceService);this.reportService=Objects.requireNonNull(reportService);
        this.auditService=Objects.requireNonNull(auditService);this.backupService=Objects.requireNonNull(backupService);
        this.session = Objects.requireNonNull(session);
        this.signedOut = Objects.requireNonNull(signedOut);
        this.exit = Objects.requireNonNull(exit);
        for (javax.swing.JButton button : new javax.swing.JButton[]{btnWorkspace,btnStudents,btnOffices,btnUsers,btnFines,btnPayments,btnQueue,btnBoard,btnClearance,btnReports,btnAudit,btnBackup,btnAccount}) {
            button.addFocusListener(new java.awt.event.FocusAdapter(){@Override public void focusGained(java.awt.event.FocusEvent event){button.scrollRectToVisible(new java.awt.Rectangle(0,0,button.getWidth(),button.getHeight()));}});
        }
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"),"leaveDisplay");
        getRootPane().getActionMap().put("leaveDisplay",new javax.swing.AbstractAction(){@Override public void actionPerformed(java.awt.event.ActionEvent e){if(fullScreen||activeDestination==Destination.BOARD)leaveDisplay();}});
        if(session.user().role()==model.Role.STUDENT){
            pnlContent.remove(dashboard);pnlContent.add(studentDashboard,Destination.WORKSPACE.name());
            studentDashboard.configure(queueService,session,this::screenActivity,text->UiStyle.status(lblCallAlert,text,text.contains("offline")));
        }
        enableNavigation(true);
        open(Destination.WORKSPACE);
    }

    private void open(Destination destination) {
        if (busy || navigation == null) {
            return;
        }
        busy = true;
        UiStyle.status(lblStatus, "Checking account...", false);
        UiTasks.run(this, () -> navigation.open(session, destination), workspace -> {
            busy = false;
            queuePanel.pause();board.pause();activeDestination=destination;
            session = workspace.session();
            lblRole.setText(session.user().role().displayName());
            btnWorkspace.setText(workspace.title());
            btnStudents.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.STUDENTS));
            btnOffices.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.OFFICES));
            btnUsers.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.USERS));
            btnFines.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.FINES));
            btnPayments.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.PAYMENTS));
            btnQueue.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.QUEUE));
            btnBoard.setVisible(workspace.entries().stream().anyMatch(entry -> entry.destination() == Destination.BOARD));
            btnClearance.setVisible(workspace.entries().stream().anyMatch(e->e.destination()==Destination.CLEARANCE));
            btnReports.setVisible(workspace.entries().stream().anyMatch(e->e.destination()==Destination.REPORTS));
            btnAudit.setVisible(workspace.entries().stream().anyMatch(e->e.destination()==Destination.AUDIT));
            btnBackup.setVisible(workspace.entries().stream().anyMatch(e->e.destination()==Destination.BACKUP));
            btnWorkspace.setToolTipText("Open your " + workspace.title().toLowerCase(java.util.Locale.ROOT));
            dashboard.showWorkspace(workspace);
            account.configure(auth, session, updated -> {
                session = updated;
                UiStyle.status(lblStatus, "Signed in", false);
            });
            ((CardLayout) pnlContent.getLayout()).show(pnlContent, destination.name());
            getRootPane().setDefaultButton(null);
            btnWorkspace.putClientProperty("JButton.buttonType", destination == Destination.WORKSPACE ? "roundRect" : null);
            UiStyle.status(lblStatus, "Signed in", false);
            switch (destination) {
                case CLEARANCE -> {clearancePanel.configure(clearanceService,session,this::screenActivity);clearancePanel.activate();clearancePanel.focusPrimaryAction();}
                case REPORTS -> {reportsPanel.configure(reportService,session,this::screenActivity);reportsPanel.activate();reportsPanel.focusPrimaryAction();}
                case AUDIT -> {auditPanel.configure(auditService,session,this::screenActivity);auditPanel.activate();auditPanel.focusPrimaryAction();}
                case BACKUP -> {backupPanel.configure(backupService,session,this::screenActivity);backupPanel.focusPrimaryAction();}
                case QUEUE -> { queuePanel.configure(queueService,session,this::screenActivity);queuePanel.activate();queuePanel.focusPrimaryAction(); }
                case BOARD -> { board.activate(queueService,session,this::enterDisplay,this::leaveDisplay);board.focusPrimaryAction(); }
                case ACCOUNT -> account.focusPrimaryAction();
                case WORKSPACE -> { if(session.user().role()==model.Role.STUDENT)studentDashboard.focusPrimaryAction();else btnWorkspace.requestFocusInWindow(); }
                case STUDENTS -> { students.configure(studentService, session, this::screenActivity); students.focusSearch(); students.reload(); }
                case OFFICES -> { offices.configure(officeService, session, this::screenActivity); offices.focusRefresh(); offices.reload(); }
                case USERS -> { users.configure(userService, session, this::screenActivity); users.focusSearch(); users.reload(); }
                case FINES -> { fines.configure(fineService, paymentService, termService, session, this::screenActivity); fines.focusSearch(); fines.reload(); }
                case PAYMENTS -> { payments.configure(paymentService, session, this::screenActivity, () -> open(Destination.FINES)); payments.focusSearch(); payments.reload(); }
            }
        }, failure -> {
            busy = false;
            UiStyle.status(lblStatus, failure.getMessage(), true);
        }, btnWorkspace, btnStudents, btnOffices, btnUsers, btnFines, btnPayments, btnQueue, btnBoard, btnClearance, btnReports, btnAudit, btnBackup, btnAccount, btnLogout);
    }

    private void enableNavigation(boolean enabled) {
        for (javax.swing.JButton button : new javax.swing.JButton[]{btnWorkspace, btnStudents, btnOffices, btnUsers, btnFines, btnPayments, btnQueue, btnBoard, btnClearance, btnReports, btnAudit, btnBackup, btnAccount, btnLogout}) { button.setEnabled(enabled); }
    }

    private void screenActivity(boolean active) {
        busy = active; enableNavigation(!active);
    }

    private void btnQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueueActionPerformed
         open(Destination.QUEUE); 
    }//GEN-LAST:event_btnQueueActionPerformed
    private void btnBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBoardActionPerformed
         open(Destination.BOARD); 
    }//GEN-LAST:event_btnBoardActionPerformed
    private void btnClearanceActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnClearanceActionPerformed
        open(Destination.CLEARANCE);
    }//GEN-LAST:event_btnClearanceActionPerformed
    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnReportsActionPerformed
        open(Destination.REPORTS);
    }//GEN-LAST:event_btnReportsActionPerformed
    private void btnAuditActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnAuditActionPerformed
        open(Destination.AUDIT);
    }//GEN-LAST:event_btnAuditActionPerformed
    private void btnBackupActionPerformed(java.awt.event.ActionEvent evt){//GEN-FIRST:event_btnBackupActionPerformed
        open(Destination.BACKUP);
    }//GEN-LAST:event_btnBackupActionPerformed
    private void enterDisplay() {
        if(fullScreen||busy)return;
        windowBounds=getBounds();windowState=getExtendedState();fullScreen=true;
        pnlSidebar.setVisible(false);super.dispose();setUndecorated(true);
        getGraphicsConfiguration().getDevice().setFullScreenWindow(this);setVisible(true);
    }
    private void leaveDisplay() {
        if(fullScreen){
            getGraphicsConfiguration().getDevice().setFullScreenWindow(null);super.dispose();setUndecorated(false);
            pnlSidebar.setVisible(true);setBounds(windowBounds);setExtendedState(windowState);fullScreen=false;setVisible(true);
        }
        open(Destination.WORKSPACE);
    }
    @Override public void dispose() {
        queuePanel.pause();board.pause();studentDashboard.close();
        if(fullScreen){getGraphicsConfiguration().getDevice().setFullScreenWindow(null);fullScreen=false;}
        super.dispose();
    }

    private void logout(boolean closeApplication) {
        if (busy) {
            UiStyle.status(lblStatus, "Please wait for the current request to finish.", true);
            return;
        }
        if (auth != null) {
            auth.logout(session);
        }
        dispose();
        Runnable callback = closeApplication ? exit : signedOut;
        if (callback != null) {
            callback.run();
        }
    }

    private void btnWorkspaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWorkspaceActionPerformed
        open(Destination.WORKSPACE);
    }//GEN-LAST:event_btnWorkspaceActionPerformed

    private void btnAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccountActionPerformed
        open(Destination.ACCOUNT);
    }//GEN-LAST:event_btnAccountActionPerformed

    private void btnStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStudentsActionPerformed
        open(Destination.STUDENTS);
    }//GEN-LAST:event_btnStudentsActionPerformed
    private void btnOfficesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOfficesActionPerformed
        open(Destination.OFFICES);
    }//GEN-LAST:event_btnOfficesActionPerformed
    private void btnUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsersActionPerformed
        open(Destination.USERS);
    }//GEN-LAST:event_btnUsersActionPerformed

    private void btnFinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinesActionPerformed
        open(Destination.FINES);
    }//GEN-LAST:event_btnFinesActionPerformed
    private void btnPaymentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPaymentsActionPerformed
        open(Destination.PAYMENTS);
    }//GEN-LAST:event_btnPaymentsActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        logout(false);
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        logout(true);
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        lblRole = new javax.swing.JLabel();
        pnlMenuScroll = new javax.swing.JScrollPane();
        pnlMenu = new javax.swing.JPanel();
        btnWorkspace = new javax.swing.JButton();
        btnStudents = new javax.swing.JButton();
        btnOffices = new javax.swing.JButton();
        btnUsers = new javax.swing.JButton();
        btnFines = new javax.swing.JButton();
        btnPayments = new javax.swing.JButton();
        btnQueue = new javax.swing.JButton();
        btnBoard = new javax.swing.JButton();
        btnClearance = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnAudit = new javax.swing.JButton();
        btnBackup = new javax.swing.JButton();
        btnAccount = new javax.swing.JButton();
        lblCallAlert = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        setDefaultCloseOperation(0);
        setTitle("Queue-Clearance");
        setMinimumSize(new java.awt.Dimension(1120, 760));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addComponent(pnlSidebar, 248, 248, 248)
            .addGap(16)
            .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(16)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGap(16)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(16)
        );
        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createSequentialGroup()
            .addGap(8)
            .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblBrand, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblRole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlMenuScroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblCallAlert, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(8)
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createSequentialGroup()
            .addGap(16)
            .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(16)
            .addComponent(pnlMenuScroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8)
            .addComponent(lblCallAlert, 96, 96, 96)
            .addGap(8)
            .addComponent(lblStatus, 64, 64, 64)
            .addGap(8)
            .addComponent(btnLogout, 40, 40, 40)
            .addGap(16)
        );
        lblBrand.setText("Queue-Clearance");
        lblBrand.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblRole.setText("Signed out");
        lblRole.setFont(new java.awt.Font("Segoe UI", 0, 14));
        pnlMenuScroll.setMinimumSize(new java.awt.Dimension(0, 60));
        pnlMenuScroll.setPreferredSize(new java.awt.Dimension(500, 460));
        pnlMenuScroll.setViewportView(pnlMenu);
        javax.swing.GroupLayout pnlMenuLayout = new javax.swing.GroupLayout(pnlMenu);
        pnlMenu.setLayout(pnlMenuLayout);
        pnlMenuLayout.setHorizontalGroup(
            pnlMenuLayout.createSequentialGroup()
            .addGap(0)
            .addGroup(pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnWorkspace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnStudents, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnOffices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnUsers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnFines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnPayments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnQueue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnBoard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnClearance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnReports, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnAudit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnBackup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnAccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(0)
        );
        pnlMenuLayout.setVerticalGroup(
            pnlMenuLayout.createSequentialGroup()
            .addComponent(btnWorkspace, 40, 40, 40)
            .addGap(8)
            .addComponent(btnStudents, 40, 40, 40)
            .addGap(8)
            .addComponent(btnOffices, 40, 40, 40)
            .addGap(8)
            .addComponent(btnUsers, 40, 40, 40)
            .addGap(8)
            .addComponent(btnFines, 40, 40, 40)
            .addGap(8)
            .addComponent(btnPayments, 40, 40, 40)
            .addGap(8)
            .addComponent(btnQueue, 40, 40, 40)
            .addGap(8)
            .addComponent(btnBoard, 40, 40, 40)
            .addGap(8)
            .addComponent(btnClearance, 40, 40, 40)
            .addGap(8)
            .addComponent(btnReports, 40, 40, 40)
            .addGap(8)
            .addComponent(btnAudit, 40, 40, 40)
            .addGap(8)
            .addComponent(btnBackup, 40, 40, 40)
            .addGap(8)
            .addComponent(btnAccount, 40, 40, 40)
            .addGap(8)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        btnWorkspace.setText("Workspace");
        btnWorkspace.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnWorkspace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWorkspaceActionPerformed(evt);
            }
        });
        btnStudents.setText("Students");
        btnStudents.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnStudents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStudentsActionPerformed(evt);
            }
        });
        btnOffices.setText("Offices");
        btnOffices.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnOffices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOfficesActionPerformed(evt);
            }
        });
        btnUsers.setText("Users");
        btnUsers.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsersActionPerformed(evt);
            }
        });
        btnFines.setText("Fines");
        btnFines.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnFines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinesActionPerformed(evt);
            }
        });
        btnPayments.setText("Payments");
        btnPayments.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnPayments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPaymentsActionPerformed(evt);
            }
        });
        btnQueue.setText("Office queue");
        btnQueue.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueueActionPerformed(evt);
            }
        });
        btnBoard.setText("Now serving");
        btnBoard.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnBoard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBoardActionPerformed(evt);
            }
        });
        btnClearance.setText("Clearance");
        btnClearance.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnClearance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearanceActionPerformed(evt);
            }
        });
        btnReports.setText("Reports");
        btnReports.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReportsActionPerformed(evt);
            }
        });
        btnAudit.setText("Audit log");
        btnAudit.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnAudit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAuditActionPerformed(evt);
            }
        });
        btnBackup.setText("Backup");
        btnBackup.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackupActionPerformed(evt);
            }
        });
        btnAccount.setText("My account");
        btnAccount.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAccountActionPerformed(evt);
            }
        });
        lblCallAlert.setText(" ");
        lblCallAlert.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblStatus.setText(" ");
        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnLogout.setText("Sign out");
        btnLogout.setFont(new java.awt.Font("Segoe UI", 0, 14));
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        pnlContent.setLayout(new java.awt.CardLayout());
        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblRole;
    private javax.swing.JScrollPane pnlMenuScroll;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JButton btnWorkspace;
    private javax.swing.JButton btnStudents;
    private javax.swing.JButton btnOffices;
    private javax.swing.JButton btnUsers;
    private javax.swing.JButton btnFines;
    private javax.swing.JButton btnPayments;
    private javax.swing.JButton btnQueue;
    private javax.swing.JButton btnBoard;
    private javax.swing.JButton btnClearance;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnAudit;
    private javax.swing.JButton btnBackup;
    private javax.swing.JButton btnAccount;
    private javax.swing.JLabel lblCallAlert;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton btnLogout;
    private javax.swing.JPanel pnlContent;
    // End of variables declaration//GEN-END:variables
}
