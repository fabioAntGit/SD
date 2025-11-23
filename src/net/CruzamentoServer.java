package src.net;

import src.app.SwingInterface;
import src.common.Semaforo;
import src.common.SemaforoStats;
import src.common.Veiculo;
import src.common.GlobalStats;
import src.net.ControladorSemaforos;
import src.net.SocketCliente;
import src.nodes.Node;
import src.nodes.Topologia;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CruzamentoServer {
    private final Node node;
    private final Topologia topologia;
    private final Map<String, Semaforo> semaforos = new HashMap<>();
    private ControladorSemaforos controlador;
    private StatsLogger statsLogger;
    private static final GlobalStats globalStats = new GlobalStats(); // usado por nodo S

    public CruzamentoServer(Node node, Topologia topologia) {
        this.node = node;
        this.topologia = topologia;

        // Na saída S não há semáforos
        if (!"S".equals(node.getName())) {
            for (Node predecessor : topologia.getPredecessores(node.getName())) {
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
        System.out.println("Cruzamento " + node.getName() +
                " ativo na porta " + node.getPort());

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> processarConexao(socket)).start();
        }
    }

    private void processarConexao(Socket socket) {
        try (socket; ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Veiculo veiculo = (Veiculo) in.readObject();

            int posAntes = veiculo.getPosicaoAtual();
            veiculo.avancar(); // marca que chegou a este nodo
            System.out.println(node.getName() + " recebeu veiculo " + veiculo.getId());
            SwingInterface.notificarPosicaoVeiculo(node.getName(), veiculo);

            String origem = posAntes >= 0 ? veiculo.getPercurso().get(posAntes).getName() : null;

            // Se nao ha proximo, acabou o percurso
            Node proximo = veiculo.getProximo();
            if (proximo == null) {
                System.out.println("Veiculo " + veiculo.getId() + " terminou no cruzamento " + node.getName());
                SwingInterface.notificarPosicaoVeiculo(null, veiculo);
                // Nodo S agrega estatisticas globais
                if ("S".equals(node.getName())) {
                    veiculo.setTempoSaida(java.time.LocalDateTime.now());
                    globalStats.registrar(veiculo);
                    logGlobalStats();
                    SwingInterface.notificarStatsGlobais(globalStats.snapshot());
                }
                return;
            }

            Semaforo semaforo = semaforos.get(origem);
            if (semaforo != null) {
                semaforo.adicionarVeiculo(veiculo);
            } else {
                // entrada sem semaforo encaminha direto
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

    /**
     * Thread simples para logar estatisticas de semaforos periodicamente.
     */
    private static class StatsLogger extends Thread {
        private final String nomeNode;
        private final Map<String, Semaforo> semaforos;

        StatsLogger(String nomeNode, Map<String, Semaforo> semaforos) {
            this.nomeNode = nomeNode;
            this.semaforos = semaforos;
            setName("Stats-" + nomeNode);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                Map<String, SemaforoStats.Snapshot> snaps = new HashMap<>();
                for (Map.Entry<String, Semaforo> entry : semaforos.entrySet()) {
                    snaps.put(entry.getValue().getIdSemaforo(), entry.getValue().getStatsSnapshot());
                }
                SwingInterface.notificarStatsSemaforos(nomeNode, snaps);
            }
        }
    }
}
