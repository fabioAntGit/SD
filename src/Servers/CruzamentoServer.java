package src.Servers;

import src.Core.*;
import src.Core.Data.Evento;
import src.Core.Data.Semaforo;
import src.Core.Data.Topologia;
import src.Core.Data.Veiculo;
import src.Estatisticas.StatsLogger;
import src.InterfaceGrafica.SwingInterface;
import src.Estatisticas.GlobalStats;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CruzamentoServer {
    private final Evento node;
    private final Topologia topologia;
    private final Map<String, Semaforo> semaforos = new HashMap<>();
    private ControladorSemaforos controlador;
    private StatsLogger statsLogger;
    private static final GlobalStats globalStats = new GlobalStats();

    public CruzamentoServer(Evento node, Topologia topologia) {
        this.node = node;
        this.topologia = topologia;

        if (!"S".equals(node.getName())) {
            for (Evento predecessor : topologia.getPredecessores(node.getName())) {
                String id = predecessor.getName() + "->" + node.getName();
                Semaforo semaforo = new Semaforo(id, node);
                semaforos.put(predecessor.getName(), semaforo);
                semaforo.start();
            }
            if (!semaforos.isEmpty()) {
                controlador = new ControladorSemaforos(semaforos.values());
                controlador.start();
                statsLogger = new StatsLogger(node.getName(), semaforos);
                statsLogger.setDaemon(true);
                statsLogger.start();
            }
        }
    }

    public void start() throws Exception {
        ServerSocket server = new ServerSocket(node.getPort());
        System.out.println("Cruzamento " + node.getName() + " ativo na porta " + node.getPort());

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> processarConexao(socket)).start();
        }
    }

    private void processarConexao(Socket socket) {
        try (socket; ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Veiculo veiculo = (Veiculo) in.readObject();

            int posAntes = veiculo.getPosicaoAtual();
            veiculo.avancar();
            System.out.println(node.getName() + " recebeu veiculo " + veiculo.getId());
            SwingInterface.notificarPosicaoVeiculo(node.getName(), veiculo);

            String origem = posAntes >= 0 ? veiculo.getPercurso().get(posAntes).getName() : null;

            Evento proximo = veiculo.getProximo();
            if (proximo == null) {
                System.out.println("Veiculo " + veiculo.getId() + " terminou no cruzamento " + node.getName());
                SwingInterface.notificarPosicaoVeiculo(null, veiculo);
                if ("S".equals(node.getName())) {
                    veiculo.setTempoSaida(java.time.LocalDateTime.now());
                    globalStats.registar(veiculo);
                    logGlobalStats();
                    SwingInterface.notificarStatsGlobais(globalStats.snapshot());
                }
                return;
            }

            Semaforo semaforo = semaforos.get(origem);
            if (semaforo != null) {
                semaforo.adicionarVeiculo(veiculo);
            } else {
                try (SocketCliente cliente = new SocketCliente(proximo.getHost(), proximo.getPort())) {
                    cliente.enviarObjeto(veiculo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logGlobalStats() {
        if (!"S".equals(node.getName())) return;
        GlobalStats.Snapshot snap = globalStats.snapshot();
        System.out.println("[GLOBAL] Estatisticas de dwelling (s): " + snap.porTipo());
    }
}
