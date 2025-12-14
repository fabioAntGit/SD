package src.app;

import src.InterfaceGrafica.SwingInterface;

import javax.swing.*;

public class Launcher {
    /**
     * Ponto de entrada da aplicacao desktop.
     *
     * @param args argumentos da linha de comando (nao utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingInterface ui = new SwingInterface();
            ui.setVisible(true);
        });
    }
}
