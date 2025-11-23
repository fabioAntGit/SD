package src.app;

import src.InterfaceGrafica.SwingInterface;

public class Launcher {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            SwingInterface ui = new SwingInterface();
            ui.setVisible(true);
        });
    }
}