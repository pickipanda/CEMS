package gui;

import data.DataStore;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminDashboard – full-screen, blue-themed.
 * Tabs: Clubs | Events | Attendance | Feedback
 */
public class AdminDashboard extends JFrame {

    private DataStore ds = DataStore.getInstance();
    private DefaultTableModel eventTableModel;
    private DefaultTableModel clubTableModel;
    private DefaultTableModel attendanceModel;

    public AdminDashboard(Admin admin) {
        setTitle("Admin Dashboard — " + admin.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI(admin);
    }

    private void buildUI(Admin admin) {
        JPanel root = AppTheme.gradientPanel();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        // ── Top bar ──────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        // Left: title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel badge = AppTheme.label("ADMIN", new Font("Segoe UI", Font.BOLD, 11), AppTheme.ACCENT_CYAN);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.ACCENT_CYAN, 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        left.add(badge);
        left.add(AppTheme.label("Welcome, " + admin.getUsername(), AppTheme.FONT_HEADING, AppTheme.TEXT_PRIMARY));
        topBar.add(left, BorderLayout.WEST);

        // Right: logout
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
        tabs.addTab("  🏛  Clubs  ",     buildClubsPanel());
        tabs.addTab("  📅  Events  ",    buildEventsPanel());
        tabs.addTab("  ✅  Attendance  ", buildAttendancePanel());
        tabs.addTab("  💬  Feedback  ",  buildFeedbackPanel());

        root.add(tabs, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CLUBS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildClubsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        clubTableModel = new DefaultTableModel(new String[]{"Club ID", "Club Name"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(clubTableModel);
        AppTheme.styleTable(table);
        refreshClubTable();

        // Form card
        JPanel form = AppTheme.cardPanel("Add New Club");
        form.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        form.add(AppTheme.label("Club Name:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY));
        JTextField nameField = AppTheme.styledField(20);
        form.add(nameField);
        JButton addBtn = AppTheme.primaryButton("Add Club");
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { AppTheme.showWarn(this, "Enter a club name."); return; }
            ds.addClub(name);
            nameField.setText("");
            refreshClubTable();
            AppTheme.showSuccess(this, "Club \"" + name + "\" added!");
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshClubTable() {
        clubTableModel.setRowCount(0);
        for (Club c : ds.getClubs())
            clubTableModel.addRow(new Object[]{c.getClubId(), c.getClubName()});
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EVENTS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        eventTableModel = new DefaultTableModel(
                new String[]{"Event ID", "Event Name", "Club", "Date", "Registered"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(eventTableModel);
        AppTheme.styleTable(table);
        refreshEventTable();

        // Form card
        JPanel form = AppTheme.cardPanel("Add New Event");
        form.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

        JTextField nameField = AppTheme.styledField(16);
        JTextField dateField = AppTheme.styledField(10);
        dateField.setText("YYYY-MM-DD");
        JComboBox<Club> clubBox = AppTheme.styledCombo();
        for (Club c : ds.getClubs()) clubBox.addItem(c);
        JButton addBtn = AppTheme.primaryButton("Add Event");

        form.add(AppTheme.label("Name:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY)); form.add(nameField);
        form.add(AppTheme.label("Club:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY)); form.add(clubBox);
        form.add(AppTheme.label("Date:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY)); form.add(dateField);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String date = dateField.getText().trim();
            Club   club = (Club) clubBox.getSelectedItem();
            if (name.isEmpty() || date.equals("YYYY-MM-DD") || club == null) {
                AppTheme.showWarn(this, "Fill all fields."); return;
            }
            ds.addEvent(name, club.getClubId(), date);
            nameField.setText(""); dateField.setText("YYYY-MM-DD");
            clubBox.removeAllItems(); for (Club c : ds.getClubs()) clubBox.addItem(c);
            refreshEventTable();
            AppTheme.showSuccess(this, "Event \"" + name + "\" added!");
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshEventTable() {
        eventTableModel.setRowCount(0);
        for (models.Event ev : ds.getEvents()) {
            Club   club     = ds.findClub(ev.getClubId());
            String clubName = club != null ? club.getClubName() : "Unknown";
            eventTableModel.addRow(new Object[]{
                ev.getEventId(), ev.getEventName(), clubName,
                ev.getDate(), ev.getRegisteredStudents().size()
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ATTENDANCE PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        attendanceModel = new DefaultTableModel(new String[]{"Student ID", "Name", "Attended"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(attendanceModel);
        AppTheme.styleTable(table);

        JPanel top = AppTheme.cardPanel("Mark Attendance");
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JComboBox<models.Event> eventBox = AppTheme.styledCombo();
        for (models.Event ev : ds.getEvents()) eventBox.addItem(ev);
        JButton loadBtn = AppTheme.primaryButton("Load Students");
        JButton markBtn = AppTheme.primaryButton("Mark Selected Present");

        top.add(AppTheme.label("Event:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY));
        top.add(eventBox); top.add(loadBtn); top.add(markBtn);

        loadBtn.addActionListener(e -> {
            models.Event ev = (models.Event) eventBox.getSelectedItem();
            if (ev == null) return;
            attendanceModel.setRowCount(0);
            for (String sid : ev.getRegisteredStudents()) {
                Student s = ds.getStudents().stream()
                    .filter(st -> st.getStudentId().equals(sid)).findFirst().orElse(null);
                String name = s != null ? s.getName() : sid;
                boolean attended = ev.getAttendedStudents().contains(sid);
                attendanceModel.addRow(new Object[]{sid, name, attended ? "✅ Yes" : "—"});
            }
        });

        markBtn.addActionListener(e -> {
            models.Event ev = (models.Event) eventBox.getSelectedItem();
            if (ev == null) return;
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select a student row."); return; }
            String sid = (String) attendanceModel.getValueAt(row, 0);
            boolean ok = ev.markAttendance(sid);
            if (ok) { AppTheme.showSuccess(this, "Attendance marked!"); loadBtn.doClick(); }
            else    { AppTheme.showWarn(this, "Already marked or not registered."); }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FEEDBACK PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        DefaultTableModel fbModel = new DefaultTableModel(
                new String[]{"Student ID", "Rating", "Comment"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(fbModel);
        AppTheme.styleTable(table);

        JPanel top = AppTheme.cardPanel("View Feedback");
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JComboBox<models.Event> eventBox = AppTheme.styledCombo();
        for (models.Event ev : ds.getEvents()) eventBox.addItem(ev);
        JButton loadBtn = AppTheme.primaryButton("Load Feedback");
        JLabel avgLabel = AppTheme.label("Avg Rating: —", AppTheme.FONT_BOLD, AppTheme.ACCENT_CYAN);

        top.add(AppTheme.label("Event:", AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY));
        top.add(eventBox); top.add(loadBtn); top.add(avgLabel);

        loadBtn.addActionListener(e -> {
            models.Event ev = (models.Event) eventBox.getSelectedItem();
            if (ev == null) return;
            fbModel.setRowCount(0);
            List<Feedback> list = ds.getFeedbacksForEvent(ev.getEventId());
            double sum = 0;
            for (Feedback fb : list) {
                String stars = "★".repeat(fb.getRating()) + "☆".repeat(5 - fb.getRating());
                fbModel.addRow(new Object[]{fb.getStudentId(), stars, fb.getComment()});
                sum += fb.getRating();
            }
            avgLabel.setText(list.isEmpty()
                ? "Avg Rating: —"
                : String.format("Avg: %.1f / 5", sum / list.size()));
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        return panel;
    }
}
