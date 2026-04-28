package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * AppTheme – Central design system.
 * All colours, fonts, and helper factories live here.
 * Change something here and it updates everywhere.
 */
public class AppTheme {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(10,  25,  60);   // deep navy
    public static final Color BG_MID        = new Color(18,  40,  90);   // mid navy
    public static final Color BG_CARD       = new Color(22,  52, 110);   // card bg
    public static final Color ACCENT_BLUE   = new Color(30, 130, 255);   // vivid blue
    public static final Color ACCENT_CYAN   = new Color(0,  210, 230);   // cyan highlight
    public static final Color ACCENT_LIGHT  = new Color(100,180, 255);   // soft blue
    public static final Color TEXT_PRIMARY  = new Color(220, 235, 255);  // near-white
    public static final Color TEXT_SECONDARY= new Color(130, 165, 215);  // muted blue
    public static final Color BORDER_COLOR  = new Color(40,  80, 160);   // subtle border
    public static final Color ROW_ODD       = new Color(18,  42,  95);
    public static final Color ROW_EVEN      = new Color(22,  52, 110);
    public static final Color ROW_SELECT    = new Color(30, 100, 220);
    public static final Color SUCCESS       = new Color(50, 210, 120);
    public static final Color DANGER        = new Color(255, 80,  80);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADING   = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TAB       = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BTN       = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_FIELD     = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Button factory ───────────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isArmed() ? ACCENT_BLUE.darker()
                         : getModel().isRollover() ? new Color(60,160,255)
                         : ACCENT_BLUE;
                Color c2 = getModel().isArmed() ? new Color(0,80,200)
                         : getModel().isRollover() ? new Color(0,150,230)
                         : new Color(0, 90, 210);
                g2.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BTN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = primaryButton(text);
        b.setForeground(Color.WHITE);
        // Override paint for red gradient
        return new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isRollover() ? new Color(255,100,100) : new Color(220,60,60);
                Color bot = new Color(180, 30, 30);
                g2.setPaint(new GradientPaint(0,0,top,0,getHeight(),bot));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
                super.paintComponent(g);
            }
            { setForeground(Color.WHITE); setFont(FONT_BTN);
              setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              setBorder(BorderFactory.createEmptyBorder(8,18,8,18)); }
        };
    }

    // ── Text field factory ───────────────────────────────────────────────────
    public static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_FIELD);
        f.setBackground(new Color(12, 30, 75));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_CYAN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    public static JPasswordField styledPassField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(FONT_FIELD);
        f.setBackground(new Color(12, 30, 75));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_CYAN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    public static JTextArea styledTextArea(int rows, int cols) {
        JTextArea a = new JTextArea(rows, cols);
        a.setFont(FONT_FIELD);
        a.setBackground(new Color(12, 30, 75));
        a.setForeground(TEXT_PRIMARY);
        a.setCaretColor(ACCENT_CYAN);
        a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        return a;
    }

    public static <T> JComboBox<T> styledCombo() {
        JComboBox<T> c = new JComboBox<>();
        c.setFont(FONT_FIELD);
        c.setBackground(new Color(12, 30, 75));
        c.setForeground(TEXT_PRIMARY);
        c.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return c;
    }

    // ── Label helpers ─────────────────────────────────────────────────────────
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color);
        return l;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setFont(FONT_BODY);
        t.setForeground(TEXT_PRIMARY);
        t.setBackground(BG_MID);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(ROW_SELECT);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(FONT_BOLD);
        t.getTableHeader().setBackground(new Color(15, 35, 85));
        t.getTableHeader().setForeground(ACCENT_CYAN);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0, ACCENT_BLUE));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    setForeground(TEXT_PRIMARY);
                } else {
                    setBackground(ROW_SELECT);
                    setForeground(Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    // ── Scroll pane styling ───────────────────────────────────────────────────
    public static JScrollPane styledScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_MID);
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    // ── Titled card panel ─────────────────────────────────────────────────────
    public static JPanel cardPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(4,8,4,8),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_BOLD, ACCENT_CYAN)));
        return p;
    }

    // ── Gradient background panel ─────────────────────────────────────────────
    public static JPanel gradientPanel() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0, BG_DARK, getWidth(), getHeight(), BG_MID));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
    }

    // ── Styled tabbed pane ────────────────────────────────────────────────────
    public static JTabbedPane styledTabs() {
        JTabbedPane t = new JTabbedPane();
        t.setFont(FONT_TAB);
        t.setBackground(BG_MID);
        t.setForeground(TEXT_PRIMARY);
        t.setBorder(BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPane.selected", BG_CARD);
        UIManager.put("TabbedPane.background", BG_MID);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.selectedForeground", ACCENT_CYAN);
        UIManager.put("TabbedPane.focus", new Color(0,0,0,0));
        return t;
    }

    // ── Dialog helpers ────────────────────────────────────────────────────────
    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "✅  Success", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "❌  Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void showWarn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "⚠️  Warning", JOptionPane.WARNING_MESSAGE);
    }
}
