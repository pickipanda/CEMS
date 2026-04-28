package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * SplashScreen – shown for ~3 seconds on startup.
 * Draws animated particles, a logo ring, and a loading bar.
 * After loading completes it auto-opens LoginFrame.
 */
public class SplashScreen extends JWindow {

    private int       progress  = 0;
    private Timer     animator;
    private float     angle     = 0f;

    // Simple random star/particle positions
    private final int[][] stars;

    public SplashScreen() {
        setSize(600, 380);
        setLocationRelativeTo(null);

        Random rng = new Random(42);
        stars = new int[60][2];
        for (int i = 0; i < stars.length; i++) {
            stars[i][0] = rng.nextInt(600);
            stars[i][1] = rng.nextInt(380);
        }

        // ── Canvas ───────────────────────────────────────────────────────
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();

                // Background gradient
                g2.setPaint(new GradientPaint(0, 0, AppTheme.BG_DARK, W, H, new Color(10, 40, 100)));
                g2.fillRect(0, 0, W, H);

                // Glowing top arc
                g2.setPaint(new RadialGradientPaint(W/2f, 0, 300,
                    new float[]{0f, 1f},
                    new Color[]{new Color(30,100,255,80), new Color(0,0,0,0)}));
                g2.fillRect(0, 0, W, 200);

                // Stars / particles
                for (int i = 0; i < stars.length; i++) {
                    float alpha = 0.3f + 0.5f * (float)Math.abs(Math.sin(angle + i * 0.4f));
                    g2.setColor(new Color(1f, 1f, 1f, alpha));
                    int sz = (i % 3 == 0) ? 3 : 2;
                    g2.fillOval(stars[i][0], stars[i][1], sz, sz);
                }

                // Spinning ring
                int cx = W/2, cy = 138, r = 62;
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(new Color(30,80,180,60));
                g2.drawOval(cx - r, cy - r, r*2, r*2);

                // Spinning arc
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                GradientPaint ringGrad = new GradientPaint(cx-r,cy-r, AppTheme.ACCENT_BLUE, cx+r, cy+r, AppTheme.ACCENT_CYAN);
                g2.setPaint(ringGrad);
                g2.drawArc(cx - r, cy - r, r*2, r*2, (int)(angle * 3), 220);

                // Inner circle (icon bg)
                g2.setPaint(new RadialGradientPaint(cx, cy, r - 6,
                    new float[]{0f, 1f},
                    new Color[]{new Color(20,60,150), new Color(10,30,80)}));
                g2.fillOval(cx - r + 5, cy - r + 5, (r-5)*2, (r-5)*2);

                // Icon: graduation cap emoji as text
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "🎓";
                g2.drawString(icon, cx - fm.stringWidth(icon)/2, cy + 12);

                // Title
                g2.setFont(AppTheme.FONT_TITLE);
                String title = "College Event Manager";
                fm = g2.getFontMetrics();
                g2.setPaint(new GradientPaint(cx - 160, 0, AppTheme.ACCENT_BLUE, cx + 160, 0, AppTheme.ACCENT_CYAN));
                g2.drawString(title, cx - fm.stringWidth(title)/2, cy + r + 28);

                // Subtitle
                g2.setFont(AppTheme.FONT_SUBTITLE);
                g2.setColor(AppTheme.TEXT_SECONDARY);
                String sub = "Manage Clubs · Events · Students · Feedback";
                fm = g2.getFontMetrics();
                g2.drawString(sub, cx - fm.stringWidth(sub)/2, cy + r + 50);

                // ── Progress bar ─────────────────────────────────────────────
                int bw = 340, bh = 8;
                int bx = cx - bw/2, by = H - 60;
                // Track
                g2.setColor(new Color(20, 50, 110));
                g2.fillRoundRect(bx, by, bw, bh, bh, bh);
                // Fill
                int fill = (int)(bw * progress / 100.0);
                g2.setPaint(new GradientPaint(bx, by, AppTheme.ACCENT_BLUE, bx + fill, by, AppTheme.ACCENT_CYAN));
                g2.fillRoundRect(bx, by, fill, bh, bh, bh);
                // Glow tip
                if (fill > 4) {
                    g2.setPaint(new RadialGradientPaint(bx + fill, by + bh/2f, 14,
                        new float[]{0f,1f}, new Color[]{new Color(0,210,230,140), new Color(0,0,0,0)}));
                    g2.fillOval(bx + fill - 14, by - 8, 28, bh + 16);
                }

                // Loading text
                g2.setFont(AppTheme.FONT_SMALL);
                g2.setColor(AppTheme.TEXT_SECONDARY);
                String loading = progress < 100 ? "Loading... " + progress + "%" : "Ready!";
                fm = g2.getFontMetrics();
                g2.drawString(loading, cx - fm.stringWidth(loading)/2, by + bh + 20);

                // Border
                g2.setColor(AppTheme.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, W-2, H-2, 12, 12);
            }
        };
        canvas.setBackground(AppTheme.BG_DARK);
        setContentPane(canvas);

        // ── Animation timer (60 fps) ──────────────────────────────────────
        animator = new Timer(16, e -> {
            angle += 2.2f;
            progress = Math.min(100, progress + 1);
            canvas.repaint();
            if (progress >= 100) {
                ((Timer) e.getSource()).stop();
                // Short pause then open login
                new Timer(300, ev -> {
                    ((Timer) ev.getSource()).stop();
                    dispose();
                    new LoginFrame().setVisible(true);
                }).start();
            }
        });
    }

    public void showSplash() {
        setVisible(true);
        animator.start();
    }
}
