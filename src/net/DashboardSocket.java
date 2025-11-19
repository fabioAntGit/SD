package src.net;

import src.common.Dashboard;
import src.common.MensagemDashboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class DashboardSocket {
    private final int port;
    private final Dashboard dashboard;
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public DashboardSocket(int port) {
        this.port = port;
        this.dashboard = new Dashboard();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Dashboard à escuta na porta " + port + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(() -> tratarLigacao(socket));
            }
        } catch (IOException e) {
            System.err.println("Erro no Dashboard: " + e.getMessage());
        }
    }

    private void tratarLigacao(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Object obj = in.readObject();

            if (obj instanceof MensagemDashboard msg) {
                dashboard.atualizar(msg);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao receber estatísticas: " + e.getMessage());
        }
    }
}