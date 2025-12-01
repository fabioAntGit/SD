package src.Servers;

import src.Core.*;
import src.Core.Data.No;
import src.Core.Data.Semaforo;
import src.Core.Data.Topologia;
import src.Core.Data.Veiculo;
import src.Estatisticas.GlobalStats;
import src.Estatisticas.VeiculoStats;
import src.Servers.ClientSocket;
import src.Core.Data.dtos.PositionEvent;
import src.Core.Data.dtos.VeiculoStatsEvent;
import src.Core.Data.dtos.SingleVeiculoEvent;
import src.Core.Data.dtos.GlobalStatsEvent;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NodeServer {
    private final No node;
    private final Map<String, Semaforo> semaforos = new HashMap<>();
    private ControladorSemaforos controlador;
    private static final GlobalStats globalStats = new GlobalStats();
    private static final VeiculoStats veiculoStats = new VeiculoStats();

    /**
     * Cria um servidor para um no da topologia, iniciando semaforos se necessario.
     *
     * @param node no representado por este servidor
     * @param topologia grafo completo
     */
    public NodeServer(No node, Topologia topologia) {
        this.node = node;

        if (!"S".equals(node.getName())) {
            for (No predecessor : topologia.getPredecessores(node.getName())) {
                String id = predecessor.getName() + "->" + node.getName();
                Semaforo semaforo = new Semaforo(id, node);
                semaforos.put(predecessor.getName(), semaforo);
                semaforo.start();
            }
            if (!semaforos.isEmpty()) {
                controlador = new ControladorSemaforos(semaforos.values());
                controlador.start();
            }
        }
    }

    /**
     * Inicia o socket servidor e processa ligacoes de forma continua.
     *
     * @throws Exception se houver falha ao abrir a porta
     */
    public void start() throws Exception {
        java.net.ServerSocket server;
        try {
            server = new java.net.ServerSocket(node.getPort());
        } catch (java.net.BindException e) {
            System.err.println("Porta " + node.getPort() + " ja esta em uso para o no " + node.getName() + ". Encerrando NodeServer.");
            return;
        }

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> processarConexao(socket)).start();
        }
    }

    /**
     * Le um veiculo recebido, avanca o percurso e reencaminha para o proximo no.
     *
     * @param socket ligacao com o no anterior
     */
    private void processarConexao(Socket socket) {
        try (socket; ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Veiculo veiculo = (Veiculo) in.readObject();

            int posAntes = veiculo.getPosicaoAtual();
            veiculo.avancar();
            ClientSocket.enviarParaDashboard(new PositionEvent(node.getName(), veiculo));

            String origem = posAntes >= 0 ? veiculo.getPercurso().get(posAntes).getName() : null;

            No proximo = veiculo.getProximo();
            if (proximo == null) {
                ClientSocket.enviarParaDashboard(new PositionEvent(null, veiculo));
                if ("S".equals(node.getName())) {
                    veiculo.setTempoSaida(java.time.LocalDateTime.now());
                    globalStats.registar(veiculo);
                    veiculoStats.registar(veiculo);
                    ClientSocket.enviarParaDashboard(new GlobalStatsEvent(globalStats.snapshot()));
                    ClientSocket.enviarParaDashboard(new VeiculoStatsEvent(veiculoStats.snapshot()));
                    ClientSocket.enviarParaDashboard(new SingleVeiculoEvent(veiculo));
                }
                return;
            }

            Semaforo semaforo = semaforos.get(origem);
            if (semaforo != null) {
                semaforo.adicionarVeiculo(veiculo);
            } else {
                ClientSocket.enviarObjeto(proximo.getHost(), proximo.getPort(), veiculo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ponto de entrada para iniciar um no especifico por nome.
     *
     * @param args argumentos da linha de comando
     * @throws Exception se o servidor falhar
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: NodeServer <nomeNo>");
            System.exit(1);
        }
        String nomeNo = args[0];
        Topologia topologia = new Topologia();
        No node = topologia.get(nomeNo);
        if (node == null) {
            System.err.println("No desconhecido: " + nomeNo);
            System.exit(2);
        }
        new NodeServer(node, topologia).start();
    }
}
