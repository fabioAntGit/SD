package src.Servers;

import src.Core.Data.dtos.*;
import src.InterfaceGrafica.SwingInterface;
import java.io.ObjectInputStream;
import java.net.Socket;

public class DashboardServer {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 6000;
    private Thread thread;

    /** Inicia o servidor de recepcao de eventos do dashboard. */
    public void start() {
        if (thread != null && thread.isAlive()) return;
        thread = new Thread(this::runLoop, "DashboardServer");
        thread.setDaemon(true);
        thread.start();
    }

    /** Loop de escuta que aceita ligacoes e cria workers para cada cliente. */
    private void runLoop() {
        try (java.net.ServerSocket server = new java.net.ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                new Thread(() -> processar(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Processa uma ligacao recebendo um unico objeto de evento.
     *
     * @param socket ligacao com o emissor
     */
    private void processar(Socket socket) {
        try (socket; ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Object obj = in.readObject();
            if (obj instanceof DashboardEvent event) {
                despachar(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Encaminha eventos para a interface grafica conforme o tipo.
     *
     * @param event evento recebido
     */
    private void despachar(DashboardEvent event) {
        if (event instanceof PositionEvent e) {
            SwingInterface.notificarPosicaoVeiculo(e.nodeName(), e.veiculo());
        } else if (event instanceof SemaforoStateEvent e) {
            SwingInterface.notificarEstadoSemaforo(e.semaforoId(), e.ativo(), e.fila());
        } else if (event instanceof MovementEvent e) {
            SwingInterface.notificarEnvio(e.semaforoId(), e.destino(), e.veiculo());
        } else if (event instanceof SemaforoStatsEvent e) {
            SwingInterface.notificarStatsSemaforos("dash", e.stats());
        } else if (event instanceof VeiculoStatsEvent e) {
            SwingInterface.notificarStatsVeiculos("dash", e.snapshot());
        } else if (event instanceof SingleVeiculoEvent e) {
            SwingInterface.notificarStatsVeiculo(e.veiculo());
        } else if (event instanceof GlobalStatsEvent e) {
            SwingInterface.notificarStatsGlobais(e.snapshot());
        }
    }
}
