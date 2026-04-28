package gui;

import data.DataStore;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDashboard – Extended with Comments, My Events, Favourites, Search.
 */
public class StudentDashboard extends JFrame {

    private DataStore ds;
    private Student   student;
    private DefaultTableModel eventModel;
    private DefaultTableModel myEventsModel;
    private DefaultTableModel favModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeBox;

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
        JLabel badge = AppTheme.label("STUDENT", new Font("Segoe UI", Font.BOLD, 12), AppTheme.SUCCESS);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.SUCCESS, 1),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        left.add(badge);
        left.add(AppTheme.label(
            student.getName() + "  (" + student.getStudentId() + ")",
            new Font("Segoe UI", Font.BOLD, 18), AppTheme.TEXT_PRIMARY));
        topBar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // 🔄 REFRESH BUTTON
        JButton refreshBtn = makeColorButton("🔄  Refresh", AppTheme.ACCENT_CYAN);
        refreshBtn.addActionListener(e -> {
            refreshEventTable();
            refreshMyEvents();
            refreshFavourites();
            AppTheme.showSuccess(this, "Data refreshed!");
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
        tabs.addTab("  📅  Events  ",        buildEventsPanel());
        tabs.addTab("  🗂  My Events  ",     buildMyEventsPanel());
        tabs.addTab("  ⭐  My Favourites  ", buildFavouritesPanel());
        tabs.addTab("  💬  Feedback  ",      buildFeedbackPanel());

        root.add(tabs, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EVENTS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ── Search bar ─────────────────────────────────────────────────────
        JPanel searchBar = AppTheme.cardPanel("🔍  Search Events");
        searchBar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        searchField = AppTheme.styledField(28);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Search by name, date, or club");

        String[] searchTypes = {"By Name", "By Date", "By Club"};
        searchTypeBox = AppTheme.styledCombo();
        searchTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (String s : searchTypes) searchTypeBox.addItem(s);

        JButton searchBtn = AppTheme.primaryButton("🔍  Search");
        JButton clearBtn  = makeColorButton("✖  Clear", new Color(150,150,180));

        searchBar.add(AppTheme.label("Filter:", new Font("Segoe UI",Font.BOLD,14), AppTheme.TEXT_SECONDARY));
        searchBar.add(searchTypeBox);
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        // ── Event table ────────────────────────────────────────────────────
        eventModel = new DefaultTableModel(
            new String[]{"ID", "Event Name", "Club", "Date", "Status", "Registered?", "Saved?"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(eventModel);
        AppTheme.styleTable(table);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(34);
        refreshEventTable();

        searchBtn.addActionListener(e -> applySearch());
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshEventTable(); });
        searchField.addActionListener(e -> applySearch());

        // ── Action buttons ──────────────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionBar.setOpaque(false);

        JButton registerBtn = AppTheme.primaryButton("📋  Register");
        JButton saveBtn     = makeColorButton("⭐  Save Event",   new Color(220,170,0));
        JButton unsaveBtn   = makeColorButton("✖  Unsave",       new Color(150,150,180));
        JButton commentBtn  = makeColorButton("💬  Add Comment",  new Color(80,180,130));
        JButton viewCmtBtn  = makeColorButton("👁  View Comments",new Color(100,160,220));

        actionBar.add(registerBtn);
        actionBar.add(saveBtn);
        actionBar.add(unsaveBtn);
        actionBar.add(commentBtn);
        actionBar.add(viewCmtBtn);
        actionBar.add(AppTheme.label(
            "  Select a row to act on it.",
            new Font("Segoe UI", Font.ITALIC, 12), AppTheme.TEXT_SECONDARY));

        registerBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event first."); return; }
            String eid = (String) eventModel.getValueAt(row, 0);
            models.Event  ev  = ds.findEvent(eid);
            if (ev == null) return;
            if (!ev.canRegister()) {
                AppTheme.showWarn(this, "Registration is closed or the event is full."); return;
            }
            boolean ok = ds.registerStudent(eid, student.getStudentId());
            if (ok) { AppTheme.showSuccess(this, "Registered for \"" + ev.getEventName() + "\" successfully!"); }
            else    { AppTheme.showWarn(this, "You are already registered for this event."); }
            refreshEventTable();
            refreshMyEvents();
        });

        saveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event first."); return; }
            String eid = (String) eventModel.getValueAt(row, 0);
            boolean ok = ds.saveEvent(eid, student.getStudentId());
            if (ok) { AppTheme.showSuccess(this, "Event saved to Favourites!"); }
            else    { AppTheme.showWarn(this, "Already saved."); }
            refreshEventTable();
            refreshFavourites();
        });

        unsaveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event first."); return; }
            String eid = (String) eventModel.getValueAt(row, 0);
            ds.unsaveEvent(eid, student.getStudentId());
            refreshEventTable();
            refreshFavourites();
        });

        commentBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event first."); return; }
            String eid = (String) eventModel.getValueAt(row, 0);
            models.Event ev = ds.findEvent(eid);
            if (ev == null) return;
            String text = JOptionPane.showInputDialog(this,
                "Write your comment for \"" + ev.getEventName() + "\":",
                "Add Comment", JOptionPane.PLAIN_MESSAGE);
            if (text != null && !text.trim().isEmpty()) {
                ds.addComment(eid, student.getStudentId(), text.trim());
                AppTheme.showSuccess(this, "Comment posted!");
            }
        });

        viewCmtBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event first."); return; }
            String eid = (String) eventModel.getValueAt(row, 0);
            showCommentsDialog(eid);
        });

        JPanel center = new JPanel(new BorderLayout(0,6));
        center.setOpaque(false);
        center.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        center.add(actionBar, BorderLayout.SOUTH);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { refreshEventTable(); return; }
        String type = (String) searchTypeBox.getSelectedItem();
        eventModel.setRowCount(0);
        for (models.Event ev : ds.getEvents()) {
            Club club = ds.findClub(ev.getClubId());
            String clubName = club != null ? club.getClubName() : "";
            boolean match = false;
            if ("By Name".equals(type))  match = ev.getEventName().toLowerCase().contains(query);
            if ("By Date".equals(type))  match = ev.getDate().toLowerCase().contains(query);
            if ("By Club".equals(type))  match = clubName.toLowerCase().contains(query);
            if (match) {
                boolean reg  = ev.getRegisteredStudents().contains(student.getStudentId());
                boolean saved= ev.getSavedByStudents().contains(student.getStudentId());
                String stat  = ev.isRegistrationClosed() ? "🔒 Closed" : (ev.isFull() ? "🚫 Full" : "✅ Open");
                eventModel.addRow(new Object[]{
                    ev.getEventId(), ev.getEventName(), clubName, ev.getDate(),
                    stat, reg ? "✅ Yes" : "—", saved ? "⭐ Yes" : "—"
                });
            }
        }
    }

    private void refreshEventTable() {
        eventModel.setRowCount(0);
        for (models.Event ev : ds.getEvents()) {
            Club   club = ds.findClub(ev.getClubId());
            String cn   = club != null ? club.getClubName() : "Unknown";
            boolean reg  = ev.getRegisteredStudents().contains(student.getStudentId());
            boolean saved= ev.getSavedByStudents().contains(student.getStudentId());
            String stat  = ev.isRegistrationClosed() ? "🔒 Closed" : (ev.isFull() ? "🚫 Full" : "✅ Open");
            eventModel.addRow(new Object[]{
                ev.getEventId(), ev.getEventName(), cn, ev.getDate(),
                stat, reg ? "✅ Yes" : "—", saved ? "⭐ Yes" : "—"
            });
        }
    }

    private void showCommentsDialog(String eventId) {
        models.Event ev = ds.findEvent(eventId);
        if (ev == null) return;
        JDialog dlg = new JDialog(this, "💬  Comments — " + ev.getEventName(), true);
        dlg.setSize(560, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        // Comments list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String raw : ev.getComments()) {
            String[] parts = raw.split("\\|\\|\\|", 2);
            String sid  = parts[0];
            String text = parts.length > 1 ? parts[1] : "";
            // find student name
            String name = ds.getStudents().stream()
                .filter(s->s.getStudentId().equals(sid))
                .map(Student::getName).findFirst().orElse(sid);
            listModel.addElement("🧑  " + name + " (" + sid + "):  " + text);
        }
        JList<String> cList = new JList<>(listModel);
        cList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cList.setBackground(AppTheme.BG_MID);
        cList.setForeground(AppTheme.TEXT_PRIMARY);
        cList.setFixedCellHeight(36);
        JScrollPane scroll = AppTheme.styledScroll(cList);
        if (listModel.isEmpty()) {
            JLabel empty = AppTheme.label("No comments yet. Be the first!",
                new Font("Segoe UI",Font.ITALIC,14), AppTheme.TEXT_SECONDARY);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(empty, BorderLayout.CENTER);
        } else {
            p.add(scroll, BorderLayout.CENTER);
        }

        // Add comment section
        JPanel bottom = new JPanel(new BorderLayout(8,0));
        bottom.setOpaque(false);
        JTextField cmtField = AppTheme.styledField(32);
        cmtField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmtField.setToolTipText("Write a comment...");
        JButton postBtn = AppTheme.primaryButton("Post");
        bottom.add(cmtField, BorderLayout.CENTER);
        bottom.add(postBtn, BorderLayout.EAST);

        postBtn.addActionListener(e2 -> {
            String text = cmtField.getText().trim();
            if (text.isEmpty()) return;
            ds.addComment(eventId, student.getStudentId(), text);
            cmtField.setText("");
            String name = student.getName();
            listModel.addElement("🧑  " + name + " (" + student.getStudentId() + "):  " + text);
        });
        cmtField.addActionListener(e2 -> postBtn.doClick());

        p.add(AppTheme.label("  Comments", new Font("Segoe UI",Font.BOLD,16), AppTheme.ACCENT_CYAN), BorderLayout.NORTH);
        p.add(bottom, BorderLayout.SOUTH);
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  MY EVENTS PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildMyEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = AppTheme.label("🗂  My Events",
            new Font("Segoe UI", Font.BOLD, 20), AppTheme.ACCENT_CYAN);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        panel.add(title, BorderLayout.NORTH);

        myEventsModel = new DefaultTableModel(
            new String[]{"Event Name", "Club", "Date", "Status", "Attended?"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(myEventsModel);
        AppTheme.styleTable(table);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(34);
        refreshMyEvents();

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setOpaque(false);
        JButton viewCmtBtn = makeColorButton("💬  View Comments", new Color(100,160,220));
        bottom.add(viewCmtBtn);
        viewCmtBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event."); return; }
            String name = (String) myEventsModel.getValueAt(row, 0);
            String eid = ds.getEvents().stream()
                .filter(ev->ev.getEventName().equals(name)).map(models.Event::getEventId)
                .findFirst().orElse(null);
            if (eid != null) showCommentsDialog(eid);
        });

        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMyEvents() {
        if (myEventsModel == null) return;
        myEventsModel.setRowCount(0);
        String sid = student.getStudentId();
        for (models.Event ev : ds.getEvents()) {
            boolean reg = ev.getRegisteredStudents().contains(sid);
            if (!reg) continue;
            Club club = ds.findClub(ev.getClubId());
            String cn  = club != null ? club.getClubName() : "Unknown";
            boolean att = ev.getAttendedStudents().contains(sid);
            String stat = ev.isRegistrationClosed() ? "🔒 Closed" : (ev.isFull() ? "🚫 Full" : "✅ Open");
            myEventsModel.addRow(new Object[]{
                ev.getEventName(), cn, ev.getDate(), stat, att ? "✅ Yes" : "—"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FAVOURITES PANEL
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildFavouritesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = AppTheme.label("⭐  My Favourite Events",
            new Font("Segoe UI", Font.BOLD, 20), new Color(220,170,0));
        title.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        panel.add(title, BorderLayout.NORTH);

        favModel = new DefaultTableModel(
            new String[]{"Event Name", "Club", "Date", "Status", "Registered?"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(favModel);
        AppTheme.styleTable(table);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(34);
        refreshFavourites();

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionBar.setOpaque(false);
        JButton registerBtn = AppTheme.primaryButton("📋  Register from Favourites");
        JButton removeBtn   = makeColorButton("✖  Remove Favourite", new Color(220,80,80));
        actionBar.add(registerBtn); actionBar.add(removeBtn);

        registerBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event."); return; }
            String name = (String) favModel.getValueAt(row, 0);
            models.Event ev = ds.getEvents().stream().filter(x->x.getEventName().equals(name)).findFirst().orElse(null);
            if (ev == null) return;
            if (!ev.canRegister()) { AppTheme.showWarn(this, "Registration is closed or event is full."); return; }
            boolean ok = ds.registerStudent(ev.getEventId(), student.getStudentId());
            if (ok) { AppTheme.showSuccess(this, "Registered!"); }
            else    { AppTheme.showWarn(this, "Already registered."); }
            refreshFavourites();
            refreshEventTable();
            refreshMyEvents();
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { AppTheme.showWarn(this, "Select an event."); return; }
            String name = (String) favModel.getValueAt(row, 0);
            models.Event ev = ds.getEvents().stream().filter(x->x.getEventName().equals(name)).findFirst().orElse(null);
            if (ev != null) ds.unsaveEvent(ev.getEventId(), student.getStudentId());
            refreshFavourites();
            refreshEventTable();
        });

        panel.add(AppTheme.styledScroll(table), BorderLayout.CENTER);
        panel.add(actionBar, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshFavourites() {
        if (favModel == null) return;
        favModel.setRowCount(0);
        String sid = student.getStudentId();
        for (models.Event ev : ds.getEvents()) {
            if (!ev.getSavedByStudents().contains(sid)) continue;
            Club club = ds.findClub(ev.getClubId());
            String cn  = club != null ? club.getClubName() : "Unknown";
            boolean reg = ev.getRegisteredStudents().contains(sid);
            String stat = ev.isRegistrationClosed() ? "🔒 Closed" : (ev.isFull() ? "🚫 Full" : "✅ Open");
            favModel.addRow(new Object[]{ev.getEventName(), cn, ev.getDate(), stat, reg ? "✅ Yes" : "—"});
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

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        JComboBox<models.Event> eventBox = AppTheme.styledCombo();
        eventBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (models.Event ev : ds.getEvents())
            if (ev.getAttendedStudents().contains(student.getStudentId())) eventBox.addItem(ev);

        String[] ratingOpts = {"5 — Excellent ★★★★★", "4 — Good ★★★★☆",
                               "3 — Average ★★★☆☆", "2 — Poor ★★☆☆☆", "1 — Terrible ★☆☆☆☆"};
        JComboBox<String> ratingBox = AppTheme.styledCombo();
        ratingBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (String r : ratingOpts) ratingBox.addItem(r);

        JTextArea commentArea = AppTheme.styledTextArea(5, 50);
        commentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton submitBtn = AppTheme.primaryButton("✅  Submit Feedback");

        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        form.add(AppTheme.label("Event:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; form.add(eventBox, gc);

        gc.gridx=0; gc.gridy=1; gc.weightx=0;
        form.add(AppTheme.label("Rating:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1; form.add(ratingBox, gc);

        gc.gridx=0; gc.gridy=2; gc.weightx=0;
        form.add(AppTheme.label("Comment:", labelFont, AppTheme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        JScrollPane commentScroll = AppTheme.styledScroll(commentArea);
        commentScroll.setPreferredSize(new Dimension(420, 110));
        form.add(commentScroll, gc);

        gc.gridx=1; gc.gridy=3; gc.anchor=GridBagConstraints.EAST;
        gc.weightx=0; gc.fill=GridBagConstraints.NONE;
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

        JLabel info = AppTheme.label(
            "ⓘ  You can only submit feedback for events you attended. Ask the admin to mark your attendance.",
            new Font("Segoe UI", Font.ITALIC, 13), AppTheme.TEXT_SECONDARY);
        info.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(form, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════════════════
    private JButton makeColorButton(String text, Color accent) {
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
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
}
