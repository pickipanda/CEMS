package gui;

import data.DataStore;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * StudentDashboard – full-screen, blue-themed.
 * Tabs: Events | Feedback
 */
public class StudentDashboard extends JFrame {

    private DataStore ds;
    private Student   student;
    private DefaultTableModel eventModel;

    public StudentDashboard(Student student) {
        this.ds      = DataStore.getInstance();
        this.student = student;
        setTitle("Student Dashboard — " + student.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI();
    }

    private void buildUI() {
        JPanel root = AppTheme.gradientPanel();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        // ── Top bar ──────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel badge = AppTheme.label("STUDENT", new Font("Segoe UI", Font.BOLD, 11), AppTheme.SUCCESS);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.SUCCESS, 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        left.add(badge);
        left.add(AppTheme.label(
            student.getName() + "  (" + student.getStudentId() + ")",
            AppTheme.FONT_HEADING, AppTheme.TEXT_PRIMARY));
        topBar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        JButton logoutBtn = AppTheme.dangerButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        right.add(logoutBtn);
        topBar.add(right, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // ── Tabbed pane ──────────────────────────────────────────────────
        JTabbedPane tabs = AppTheme.styledTabs();
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tabs.addTab("  📅  Events  ",   buildEventsPanel());
        tabs.addTab("  💬  Feedback  ", buildFeedbackPanel());

        root.add(tabs, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EVENTS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        eventModel = new DefaultTableModel(
                new String[]{"Event ID", "Event Name", "Club", "Date", "Registered?", "Attended?"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(eventModel);
        AppTheme.styleTable(table);
        refreshEventTable();

        JPanel bottom = AppTheme.cardPanel("Actions");
        bottom.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JButton registerBtn = AppTheme.primaryButton("Register for Selected Event");
        bottom.add(registerBtn);
        bottom.add(AppTheme.label(
            "Select a row above and click Register to sign up for an event.",
            AppTheme.FONT_SMALL, AppTheme.TEXT_SECONDARY));

        registerBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event first."); return; }
            String eid = (String) eventModel.getValueAt(row, 0);
            models.Event  ev  = ds.findEvent(eid);
            if (ev == null) return;
            boolean ok = ev.registerStudent(student.getStudentId());
            if (ok) { AppTheme.showSuccess(this, "Registered for \"" + ev.getEventName() + "\" successfully!"); }
            else    { AppTheme.showWarn(this, "You are already registered for this event."); }
            refreshEventTable();
        });

        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshEventTable() {
        eventModel.setRowCount(0);
        for (models.Event ev : ds.getEvents()) {
            Club   club     = ds.findClub(ev.getClubId());
            String clubName = club != null ? club.getClubName() : "Unknown";
            boolean reg = ev.getRegisteredStudents().contains(student.getStudentId());
            boolean att = ev.getAttendedStudents().contains(student.getStudentId());
            eventModel.addRow(new Object[]{
                ev.getEventId(), ev.getEventName(), clubName, ev.getDate(),
                reg ? "✅ Yes" : "—",
                att ? "✅ Yes" : "—"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FEEDBACK PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = AppTheme.cardPanel("Submit Feedback  (only for events you attended)");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 10, 8, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<models.Event> eventBox = AppTheme.styledCombo();
        for (models.Event ev : ds.getEvents())
            if (ev.getAttendedStudents().contains(student.getStudentId())) eventBox.addItem(ev);

        String[] ratingOpts = {"5 — Excellent ★★★★★", "4 — Good ★★★★☆",
                               "3 — Average ★★★☆☆", "2 — Poor ★★☆☆☆", "1 — Terrible ★☆☆☆☆"};
        JComboBox<String> ratingBox = AppTheme.styledCombo();
        for (String r : ratingOpts) ratingBox.addItem(r);

        JTextArea commentArea = AppTheme.styledTextArea(4, 40);
        JButton submitBtn     = AppTheme.primaryButton("Submit Feedback");

        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        form.add(AppTheme.label("Event:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; form.add(eventBox, gc);

        gc.gridx=0; gc.gridy=1; gc.weightx=0;
        form.add(AppTheme.label("Rating:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; form.add(ratingBox, gc);

        gc.gridx=0; gc.gridy=2; gc.weightx=0;
        form.add(AppTheme.label("Comment:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        JScrollPane commentScroll = AppTheme.styledScroll(commentArea);
        commentScroll.setPreferredSize(new Dimension(400, 90));
        form.add(commentScroll, gc);

        gc.gridx=1; gc.gridy=3; gc.anchor=GridBagConstraints.EAST; gc.weightx=0; gc.fill=GridBagConstraints.NONE;
        form.add(submitBtn, gc);

        submitBtn.addActionListener(e -> {
            models.Event ev = (models.Event) eventBox.getSelectedItem();
            if (ev == null) {
                AppTheme.showWarn(this, "No attended events. Attend an event first."); return;
            }
            if (ds.hasFeedback(student.getStudentId(), ev.getEventId())) {
                AppTheme.showWarn(this, "You already submitted feedback for this event."); return;
            }
            int    rating  = 5 - ratingBox.getSelectedIndex();
            String comment = commentArea.getText().trim();
            ds.addFeedback(new Feedback(student.getStudentId(), ev.getEventId(), rating, comment));
            commentArea.setText("");
            AppTheme.showSuccess(this, "Feedback submitted. Thank you!");
        });

        // Info label
        JLabel info = AppTheme.label(
            "ⓘ  You can only submit feedback for events you attended. Ask the admin to mark your attendance.",
            AppTheme.FONT_SMALL, AppTheme.TEXT_SECONDARY);
        info.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(form, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }
}
