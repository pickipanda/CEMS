package gui;

import data.DataStore;
import models.Admin;
import models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame – full-screen login with animated card fade-in.
 */
public class LoginFrame extends JFrame {

    private float cardAlpha = 0f;
    private Timer fadeTimer;
    private JPanel card;

    public LoginFrame() {
        setTitle("College Event Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI();
    }

    private void buildUI() {
        // Gradient background
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                g2.setPaint(new GradientPaint(0, 0, AppTheme.BG_DARK, W, H, new Color(10, 40, 110)));
                g2.fillRect(0, 0, W, H);
                g2.setPaint(new RadialGradientPaint(W / 2f, 0, W * 0.6f,
                        new float[]{0f, 1f}, new Color[]{new Color(30, 100, 255, 60), new Color(0, 0, 0, 0)}));
                g2.fillRect(0, 0, W, H / 2);
            }
        };
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // Card panel
        card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardAlpha));
                g2.setPaint(new GradientPaint(0, 0, new Color(18, 45, 100), 0, getHeight(), new Color(12, 30, 75)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setPaint(new GradientPaint(0, 0, AppTheme.ACCENT_BLUE, getWidth(), getHeight(), AppTheme.ACCENT_CYAN));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(440, 510));
        card.setBorder(BorderFactory.createEmptyBorder(36, 44, 36, 44));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 1;

        // Logo
        JLabel logo = new JLabel("CEMS", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        logo.setForeground(AppTheme.ACCENT_CYAN);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 4, 0);
        card.add(logo, gc);

        JLabel title = AppTheme.label("College Event Manager", AppTheme.FONT_HEADING, Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 4, 0);
        card.add(title, gc);

        JLabel sub = AppTheme.label("Sign in to your account", AppTheme.FONT_SUBTITLE, AppTheme.TEXT_SECONDARY);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 18, 0);
        card.add(sub, gc);

        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER_COLOR);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 14, 0);
        card.add(sep, gc);

        // Role
        addLabel(card, gc, 4, "Login as");
        JComboBox<String> roleBox = AppTheme.styledCombo();
        roleBox.addItem("Admin"); roleBox.addItem("Student");
        gc.gridy = 5; gc.insets = new Insets(4, 0, 12, 0); card.add(roleBox, gc);

        // ID
        addLabel(card, gc, 6, "ID / Username");
        JTextField idField = AppTheme.styledField(0);
        gc.gridy = 7; gc.insets = new Insets(4, 0, 12, 0); card.add(idField, gc);

        // Password
        addLabel(card, gc, 8, "Password");
        JPasswordField passField = AppTheme.styledPassField(0);
        gc.gridy = 9; gc.insets = new Insets(4, 0, 22, 0); card.add(passField, gc);

        // Login button
        JButton loginBtn = AppTheme.primaryButton("  Sign In  ");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setPreferredSize(new Dimension(340, 44));
        gc.gridy = 10; gc.insets = new Insets(0, 0, 12, 0); card.add(loginBtn, gc);

        JLabel hint = AppTheme.label("Admin: admin / admin123   |   Student: S001 / alice123",
                AppTheme.FONT_SMALL, AppTheme.TEXT_SECONDARY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 11; gc.insets = new Insets(0, 0, 0, 0); card.add(hint, gc);

        bg.add(card);

        // Login logic
        ActionListener doLogin = e -> {
            String role = (String) roleBox.getSelectedItem();
            String id   = idField.getText().trim();
            String pass = new String(passField.getPassword());
            if (id.isEmpty() || pass.isEmpty()) { AppTheme.showWarn(this, "Please enter all fields."); return; }
            DataStore ds = DataStore.getInstance();
            if ("Admin".equals(role)) {
                Admin admin = ds.findAdmin(id, pass);
                if (admin != null) { dispose(); new AdminDashboard(admin).setVisible(true); }
                else AppTheme.showError(this, "Invalid admin credentials.");
            } else {
                Student s = ds.findStudent(id, pass);
                if (s != null) { dispose(); new StudentDashboard(s).setVisible(true); }
                else AppTheme.showError(this, "Invalid student credentials.");
            }
        };
        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);

        // Fade-in
        fadeTimer = new Timer(18, e -> {
            cardAlpha = Math.min(1f, cardAlpha + 0.045f);
            card.repaint();
            if (cardAlpha >= 1f) ((Timer) e.getSource()).stop();
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { fadeTimer.start(); }
        });
    }

    private void addLabel(JPanel p, GridBagConstraints gc, int row, String text) {
        JLabel l = AppTheme.label(text, AppTheme.FONT_BOLD, AppTheme.TEXT_SECONDARY);
        gc.gridy = row; gc.insets = new Insets(0, 0, 0, 0);
        p.add(l, gc);
    }
}
