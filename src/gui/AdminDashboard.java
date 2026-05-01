package gui;

import data.DataStore;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminDashboard – Extended with Analytics, Student Management, Event Controls.
 */
public class AdminDashboard extends JFrame {

    private DataStore ds = DataStore.getInstance();
    private DefaultTableModel eventTableModel;
    private DefaultTableModel clubTableModel;
    private DefaultTableModel attendanceModel;
    private DefaultTableModel studentTableModel;

    // Analytics labels
    private JLabel statStudents, statEvents, statAttRate, statPopular;

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

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel badge = AppTheme.label("ADMIN", new Font("Segoe UI", Font.BOLD, 12), AppTheme.ACCENT_CYAN);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.ACCENT_CYAN, 1),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        left.add(badge);
        left.add(AppTheme.label("Welcome, " + admin.getUsername(),
            new Font("Segoe UI", Font.BOLD, 18), AppTheme.TEXT_PRIMARY));
        topBar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // 🔄 REFRESH BUTTON
        JButton refreshBtn = makeAccentButton("🔄  Refresh", AppTheme.ACCENT_CYAN);
        refreshBtn.addActionListener(e -> {
            DataStore.getInstance();   // re-use singleton; full reload via reload()
            refreshAll();
            AppTheme.showSuccess(this, "Data refreshed from server!");
        });
        right.add(refreshBtn);

        JButton logoutBtn = AppTheme.dangerButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        right.add(logoutBtn);
        topBar.add(right, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // ── Tabbed pane ──────────────────────────────────────────────────
        JTabbedPane tabs = AppTheme.styledTabs();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tabs.addTab("  📊  Analytics  ",     buildAnalyticsPanel());
        tabs.addTab("  🏛  Clubs  ",         buildClubsPanel());
        tabs.addTab("  📅  Events  ",        buildEventsPanel());
        tabs.addTab("  👥  Students  ",      buildStudentsPanel());
        tabs.addTab("  ✅  Attendance  ",    buildAttendancePanel());
        tabs.addTab("  💬  Feedback  ",      buildFeedbackPanel());

        root.add(tabs, BorderLayout.CENTER);
    }

    private void refreshAll() {
        refreshClubTable();
        refreshEventTable();
        refreshStudentTable();
        refreshAnalytics();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ANALYTICS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = AppTheme.label("📊  Analytics Dashboard",
            new Font("Segoe UI", Font.BOLD, 22), AppTheme.ACCENT_CYAN);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,16,0));
        panel.add(title, BorderLayout.NORTH);

        // Stat cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);

        statStudents = new JLabel("0", SwingConstants.CENTER);
        statEvents   = new JLabel("0", SwingConstants.CENTER);
        statAttRate  = new JLabel("0%", SwingConstants.CENTER);
        statPopular  = new JLabel("—", SwingConstants.CENTER);

        cardsRow.add(statCard("👩‍🎓  Total Students",   statStudents, new Color(0, 180, 130)));
        cardsRow.add(statCard("📅  Total Events",      statEvents,   new Color(30, 130, 255)));
        cardsRow.add(statCard("✅  Attendance Rate",   statAttRate,  new Color(230, 180, 0)));
        cardsRow.add(statCard("🏆  Most Popular Event",statPopular,  new Color(220, 80, 200)));

        panel.add(cardsRow, BorderLayout.CENTER);
        refreshAnalytics();

        // Event breakdown table
        DefaultTableModel breakModel = new DefaultTableModel(
            new String[]{"Event", "Club", "Date", "Registered", "Attended", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable breakTable = new JTable(breakModel);
        AppTheme.styleTable(breakTable);
        breakTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (models.Event ev : ds.getEvents()) {
            Club club = ds.findClub(ev.getClubId());
            breakModel.addRow(new Object[]{
                ev.getEventName(),
                club != null ? club.getClubName() : "—",
                ev.getDate(),
                ev.getRegisteredStudents().size(),
                ev.getAttendedStudents().size(),
                ev.isRegistrationClosed() ? "🔒 Closed" : (ev.isFull() ? "🚫 Full" : "✅ Open")
            });
        }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel subTitle = AppTheme.label("  Event Breakdown",
            new Font("Segoe UI", Font.BOLD, 15), AppTheme.TEXT_SECONDARY);
        subTitle.setBorder(BorderFactory.createEmptyBorder(12,0,6,0));
        bottom.add(subTitle, BorderLayout.NORTH);
        bottom.add(AppTheme.styledScroll(breakTable), BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 2),
            BorderFactory.createEmptyBorder(18, 14, 18, 14)));

        JLabel lbl = AppTheme.label(label, new Font("Segoe UI", Font.BOLD, 13), AppTheme.TEXT_SECONDARY);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accent);

        card.add(lbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshAnalytics() {
        statStudents.setText(String.valueOf(ds.getTotalStudents()));
        statEvents.setText(String.valueOf(ds.getTotalEvents()));
        statAttRate.setText(String.format("%.0f%%", ds.getOverallAttendanceRate()));
        models.Event pop = ds.getMostPopularEvent();
        statPopular.setText(pop != null ? "<html><center>" + pop.getEventName() + "</center></html>" : "—");
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(34);
        refreshClubTable();

        JPanel form = AppTheme.cardPanel("Add New Club");
        form.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        form.add(AppTheme.label("Club Name:", new Font("Segoe UI", Font.BOLD, 14), AppTheme.TEXT_SECONDARY));
        JTextField nameField = AppTheme.styledField(22);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(nameField);
        JButton addBtn = AppTheme.primaryButton("➕  Add Club");
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { AppTheme.showWarn(this, "Enter a club name."); return; }
            ds.addClub(name);
            nameField.setText("");
            refreshClubTable();
            refreshAnalytics();
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
            new String[]{"ID", "Event Name", "Club", "Date", "Max", "Registered", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(eventTableModel);
        AppTheme.styleTable(table);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        refreshEventTable();

        // ── Form card ──────────────────────────────────────────────────────
        JPanel form = AppTheme.cardPanel("Add New Event");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 10, 6, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField nameField = AppTheme.styledField(22);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField dateField = AppTheme.styledField(12);
        dateField.setText("YYYY-MM-DD");
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField maxField  = AppTheme.styledField(6);
        maxField.setText("0");
        maxField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComboBox<Club> clubBox = AppTheme.styledCombo();
        clubBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (Club c : ds.getClubs()) clubBox.addItem(c);

        // ── Description textarea (larger) ──────────────────────────────────
        JTextArea descArea = AppTheme.styledTextArea(5, 50);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane descScroll = AppTheme.styledScroll(descArea);
        descScroll.setPreferredSize(new Dimension(500, 100));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        form.add(AppTheme.label("Name:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; form.add(nameField, gc);

        gc.gridx=2; gc.weightx=0;
        form.add(AppTheme.label("Club:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=3; gc.weightx=1; form.add(clubBox, gc);

        gc.gridx=0; gc.gridy=1; gc.weightx=0;
        form.add(AppTheme.label("Date:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; form.add(dateField, gc);

        gc.gridx=2; gc.weightx=0;
        form.add(AppTheme.label("Max Participants (0=∞):", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=3; gc.weightx=1; form.add(maxField, gc);

        gc.gridx=0; gc.gridy=2; gc.weightx=0;
        form.add(AppTheme.label("Description:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.gridy=2; gc.gridwidth=3; gc.weightx=1; form.add(descScroll, gc);
        gc.gridwidth=1;

        JButton addBtn = AppTheme.primaryButton("➕  Add Event");
        gc.gridx=3; gc.gridy=3; gc.anchor=GridBagConstraints.EAST; gc.fill=GridBagConstraints.NONE;
        form.add(addBtn, gc);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String date = dateField.getText().trim();
            String desc = descArea.getText().trim();
            Club   club = (Club) clubBox.getSelectedItem();
            int    maxP = 0;
            try { maxP = Integer.parseInt(maxField.getText().trim()); } catch(Exception ignored) {}
            if (name.isEmpty() || date.equals("YYYY-MM-DD") || club == null) {
                AppTheme.showWarn(this, "Fill all fields."); return;
            }
            ds.addEvent(name, club.getClubId(), date, desc, maxP);
            nameField.setText(""); dateField.setText("YYYY-MM-DD"); descArea.setText(""); maxField.setText("0");
            clubBox.removeAllItems(); for (Club c : ds.getClubs()) clubBox.addItem(c);
            refreshEventTable();
            refreshAnalytics();
            AppTheme.showSuccess(this, "Event \"" + name + "\" added!");

            // ── WhatsApp notification ──────────────────────────────────────
            int waSend = JOptionPane.showConfirmDialog(this,
                "<html><b>Send WhatsApp notification to all students?</b><br>" +
                "Edge will open web.whatsapp.com for each student with a phone number.<br>" +
                "<small>Make sure you are already logged into WhatsApp Web in Edge.</small></html>",
                "Send WhatsApp Notification", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (waSend == JOptionPane.YES_OPTION) {
                final String evtName = name, evtDate = date, evtDesc = desc;
                ds.sendWhatsAppNotifications(evtName, evtDate, evtDesc);
                AppTheme.showSuccess(this,
                    "Opening WhatsApp Web in Edge for each student with a registered phone number.");
            }
        });

        // ── Action buttons ──────────────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionBar.setOpaque(false);

        JButton editBtn   = makeAccentButton("✏️  Edit Event",        new Color(30, 160, 255));
        JButton deleteBtn = AppTheme.dangerButton("🗑  Delete Event");
        JButton closeBtn  = makeAccentButton("🔒  Close Registration", new Color(220, 130, 0));
        JButton openBtn   = makeAccentButton("🔓  Open Registration",  new Color(50, 200, 100));

        actionBar.add(editBtn); actionBar.add(deleteBtn);
        actionBar.add(closeBtn); actionBar.add(openBtn);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event to edit."); return; }
            String eid = (String) eventTableModel.getValueAt(row, 0);
            models.Event  ev  = ds.findEvent(eid);
            if (ev == null) return;
            showEditEventDialog(ev);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event to delete."); return; }
            String eid  = (String) eventTableModel.getValueAt(row, 0);
            String name = (String) eventTableModel.getValueAt(row, 1);
            int conf = JOptionPane.showConfirmDialog(this,
                "Delete event \"" + name + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                ds.deleteEvent(eid);
                refreshEventTable();
                refreshAnalytics();
                AppTheme.showSuccess(this, "Event deleted.");
            }
        });

        closeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event."); return; }
            ds.closeRegistration((String) eventTableModel.getValueAt(row, 0));
            refreshEventTable();
            AppTheme.showSuccess(this, "Registration closed.");
        });

        openBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event."); return; }
            ds.openRegistration((String) eventTableModel.getValueAt(row, 0));
            refreshEventTable();
            AppTheme.showSuccess(this, "Registration opened.");
        });

        JPanel center = new JPanel(new BorderLayout(0,6));
        center.setOpaque(false);
        center.add(actionBar, BorderLayout.NORTH);
        center.add(AppTheme.styledScroll(table), BorderLayout.CENTER);

        panel.add(form, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void showEditEventDialog(models.Event ev) {
        JDialog dlg = new JDialog(this, "Edit Event — " + ev.getEventName(), true);
        dlg.setSize(560, 420);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppTheme.BG_CARD);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,12,8,12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        Font lf = new Font("Segoe UI", Font.BOLD, 14);
        JTextField nameF = AppTheme.styledField(22); nameF.setText(ev.getEventName());
        nameF.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField dateF = AppTheme.styledField(12); dateF.setText(ev.getDate());
        dateF.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField maxF  = AppTheme.styledField(6);  maxF.setText(String.valueOf(ev.getMaxParticipants()));
        maxF.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComboBox<Club> clubBox = AppTheme.styledCombo();
        clubBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (Club c : ds.getClubs()) { clubBox.addItem(c); if (c.getClubId().equals(ev.getClubId())) clubBox.setSelectedItem(c); }
        JTextArea descA = AppTheme.styledTextArea(4, 40); descA.setText(ev.getDescription());
        descA.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gc.gridx=0; gc.gridy=0; gc.weightx=0; p.add(AppTheme.label("Name:",   lf, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; p.add(nameF, gc);
        gc.gridx=0; gc.gridy=1; gc.weightx=0; p.add(AppTheme.label("Club:",   lf, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; p.add(clubBox, gc);
        gc.gridx=0; gc.gridy=2; gc.weightx=0; p.add(AppTheme.label("Date:",   lf, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; p.add(dateF, gc);
        gc.gridx=0; gc.gridy=3; gc.weightx=0; p.add(AppTheme.label("Max (0=∞):", lf, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; p.add(maxF, gc);
        gc.gridx=0; gc.gridy=4; gc.weightx=0; p.add(AppTheme.label("Description:", lf, AppTheme.TEXT_SECONDARY), gc);
        JScrollPane ds2 = AppTheme.styledScroll(descA); ds2.setPreferredSize(new Dimension(300,80));
        gc.gridx=1; gc.weightx=1; p.add(ds2, gc);

        JButton saveBtn = AppTheme.primaryButton("💾  Save Changes");
        gc.gridx=1; gc.gridy=5; gc.anchor=GridBagConstraints.EAST; gc.fill=GridBagConstraints.NONE;
        p.add(saveBtn, gc);

        saveBtn.addActionListener(e2 -> {
            Club c = (Club) clubBox.getSelectedItem();
            int maxP = 0;
            try { maxP = Integer.parseInt(maxF.getText().trim()); } catch(Exception ignored){}
            ds.updateEvent(ev.getEventId(), nameF.getText().trim(), c != null ? c.getClubId() : ev.getClubId(),
                dateF.getText().trim(), descA.getText().trim(), maxP);
            refreshEventTable();
            refreshAnalytics();
            dlg.dispose();
            AppTheme.showSuccess(this, "Event updated!");
        });

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void refreshEventTable() {
        eventTableModel.setRowCount(0);
        for (models.Event ev : ds.getEvents()) {
            Club   club = ds.findClub(ev.getClubId());
            String cn   = club != null ? club.getClubName() : "Unknown";
            String maxS = ev.getMaxParticipants() == 0 ? "∞" : String.valueOf(ev.getMaxParticipants());
            String stat = ev.isRegistrationClosed() ? "🔒 Closed" : (ev.isFull() ? "🚫 Full" : "✅ Open");
            eventTableModel.addRow(new Object[]{
                ev.getEventId(), ev.getEventName(), cn, ev.getDate(),
                maxS, ev.getRegisteredStudents().size(), stat
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  STUDENTS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        studentTableModel = new DefaultTableModel(
            new String[]{"Student ID", "Name", "Phone", "Email", "Registered Events", "Attended Events"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(studentTableModel);
        AppTheme.styleTable(table);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(34);
        refreshStudentTable();

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionBar.setOpaque(false);
        JButton addStudBtn = AppTheme.primaryButton("➕  Add Student");
        JButton editBtn    = makeAccentButton("✏️  Edit Student",    new Color(30, 160, 255));
        JButton deleteBtn  = AppTheme.dangerButton("🗑  Delete Student");
        JButton resetBtn   = makeAccentButton("🔑  Reset Password", new Color(220, 130, 0));
        actionBar.add(addStudBtn); actionBar.add(editBtn); actionBar.add(deleteBtn); actionBar.add(resetBtn);

        addStudBtn.addActionListener(e -> {
            Font dlgFont = new Font("Segoe UI", Font.PLAIN, 14);
            Font lblFont = new Font("Segoe UI", Font.BOLD, 14);
            JTextField newName  = AppTheme.styledField(20); newName.setFont(dlgFont);
            JTextField newPass  = AppTheme.styledField(20); newPass.setFont(dlgFont);
            JTextField newPhone = AppTheme.styledField(20); newPhone.setFont(dlgFont);
            newPhone.setToolTipText("With country code, e.g. 919876543210 for India");
            JTextField newEmail = AppTheme.styledField(20); newEmail.setFont(dlgFont);
            JPanel dlgP = new JPanel(new GridLayout(8, 1, 6, 6));
            dlgP.setBackground(AppTheme.BG_CARD);
            dlgP.add(AppTheme.label("Name:", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(newName);
            dlgP.add(AppTheme.label("Password:", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(newPass);
            dlgP.add(AppTheme.label("📱 Phone (with country code, e.g. 919876543210):", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(newPhone);
            dlgP.add(AppTheme.label("✉️ Email:", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(newEmail);
            int res = JOptionPane.showConfirmDialog(this, dlgP, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String nm = newName.getText().trim();
                String pw = newPass.getText().trim();
                if (nm.isEmpty() || pw.isEmpty()) {
                    AppTheme.showWarn(this, "Name and password are required.");
                    return;
                }
                ds.addStudent(nm, pw, newPhone.getText().trim(), newEmail.getText().trim());
                refreshStudentTable();
                refreshAnalytics();
                AppTheme.showSuccess(this, "Student \"" + nm + "\" added!");
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select a student."); return; }
            String sid    = (String) studentTableModel.getValueAt(row, 0);
            String sname  = (String) studentTableModel.getValueAt(row, 1);
            String sphone = (String) studentTableModel.getValueAt(row, 2);
            String semail = (String) studentTableModel.getValueAt(row, 3);
            Font dlgFont = new Font("Segoe UI", Font.PLAIN, 14);
            Font lblFont = new Font("Segoe UI", Font.BOLD, 14);
            JTextField nameF  = AppTheme.styledField(20); nameF.setText(sname);  nameF.setFont(dlgFont);
            JTextField passF  = AppTheme.styledField(20);                        passF.setFont(dlgFont);
            JTextField phoneF = AppTheme.styledField(20); phoneF.setText(sphone != null ? sphone : ""); phoneF.setFont(dlgFont);
            JTextField emailF = AppTheme.styledField(20); emailF.setText(semail != null ? semail : ""); emailF.setFont(dlgFont);
            JPanel dlgP = new JPanel(new GridLayout(8, 1, 6, 6));
            dlgP.setBackground(AppTheme.BG_CARD);
            dlgP.add(AppTheme.label("New Name:", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(nameF);
            dlgP.add(AppTheme.label("New Password (leave blank to keep):", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(passF);
            dlgP.add(AppTheme.label("📱 Phone (with country code, e.g. 919876543210):", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(phoneF);
            dlgP.add(AppTheme.label("✉️ Email:", lblFont, AppTheme.TEXT_SECONDARY));
            dlgP.add(emailF);
            int res = JOptionPane.showConfirmDialog(this, dlgP, "Edit Student – " + sid,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String pw = passF.getText().trim().isEmpty()
                    ? ds.getStudents().stream().filter(s->s.getStudentId().equals(sid))
                        .map(Student::getPassword).findFirst().orElse("")
                    : passF.getText().trim();
                ds.updateStudent(sid, nameF.getText().trim(), pw,
                    phoneF.getText().trim(), emailF.getText().trim());
                refreshStudentTable();
                refreshAnalytics();
                AppTheme.showSuccess(this, "Student updated!");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select a student."); return; }
            String sid  = (String) studentTableModel.getValueAt(row, 0);
            String name = (String) studentTableModel.getValueAt(row, 1);
            int conf = JOptionPane.showConfirmDialog(this,
                "Delete student \"" + name + "\" (" + sid + ")? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                ds.deleteStudent(sid);
                refreshStudentTable();
                refreshAnalytics();
                AppTheme.showSuccess(this, "Student deleted.");
            }
        });

        resetBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select a student."); return; }
            String sid  = (String) studentTableModel.getValueAt(row, 0);
            String newPw= JOptionPane.showInputDialog(this,
                "Enter new password for " + sid + ":", "Reset Password", JOptionPane.PLAIN_MESSAGE);
            if (newPw != null && !newPw.trim().isEmpty()) {
                ds.resetPassword(sid, newPw.trim());
                AppTheme.showSuccess(this, "Password reset for " + sid + "!");
            }
        });

        panel.add(actionBar, BorderLayout.NORTH);
        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshStudentTable() {
        studentTableModel.setRowCount(0);
        for (Student s : ds.getStudents()) {
            long reg = ds.getEvents().stream()
                .filter(ev -> ev.getRegisteredStudents().contains(s.getStudentId())).count();
            long att = ds.getEvents().stream()
                .filter(ev -> ev.getAttendedStudents().contains(s.getStudentId())).count();
            studentTableModel.addRow(new Object[]{
                s.getStudentId(), s.getName(),
                s.getPhone(), s.getEmail(),
                reg, att});
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);

        JPanel top = AppTheme.cardPanel("Mark Attendance");
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JComboBox<models.Event> eventBox = AppTheme.styledCombo();
        eventBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (models.Event ev : ds.getEvents()) eventBox.addItem(ev);
        JButton loadBtn = AppTheme.primaryButton("📋  Load Students");
        JButton markBtn = AppTheme.primaryButton("✅  Mark Selected Present");

        top.add(AppTheme.label("Event:", new Font("Segoe UI",Font.BOLD,14), AppTheme.TEXT_SECONDARY));
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
            boolean ok = ds.markAttendance(ev.getEventId(), sid);
            if (ok) { refreshAnalytics(); AppTheme.showSuccess(this, "Attendance marked!"); loadBtn.doClick(); }
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);

        JPanel top = AppTheme.cardPanel("View Feedback");
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JComboBox<models.Event> eventBox = AppTheme.styledCombo();
        eventBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (models.Event ev : ds.getEvents()) eventBox.addItem(ev);
        JButton loadBtn = AppTheme.primaryButton("📊  Load Feedback");
        JLabel avgLabel = AppTheme.label("Avg Rating: —", new Font("Segoe UI",Font.BOLD,15), AppTheme.ACCENT_CYAN);

        top.add(AppTheme.label("Event:", new Font("Segoe UI",Font.BOLD,14), AppTheme.TEXT_SECONDARY));
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

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPER: accent button factory
    // ═══════════════════════════════════════════════════════════════════════
    private JButton makeAccentButton(String text, Color accent) {
        Color dark = accent.darker();
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isRollover() ? accent.brighter() : accent;
                g2.setPaint(new java.awt.GradientPaint(0,0,top,0,getHeight(),dark));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        return b;
    }
}
