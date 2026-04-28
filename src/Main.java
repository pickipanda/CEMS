import gui.SplashScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply dark-style UI defaults before anything opens
        UIManager.put("OptionPane.background",       new java.awt.Color(18, 45, 100));
        UIManager.put("Panel.background",            new java.awt.Color(18, 45, 100));
        UIManager.put("OptionPane.messageForeground",new java.awt.Color(220, 235, 255));
        UIManager.put("Button.background",           new java.awt.Color(30, 130, 255));
        UIManager.put("Button.foreground",           java.awt.Color.WHITE);

        SwingUtilities.invokeLater(() -> new SplashScreen().showSplash());
    }
}
