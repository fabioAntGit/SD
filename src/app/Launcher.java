package src.app;

public class Launcher {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            SwingInterface ui = new SwingInterface();
            ui.setVisible(true);
        });
    }
}
