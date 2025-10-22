package src.net;

import src.common.Mensagem;
import src.common.Semaforo;
import src.common.Veiculo;
import src.nodes.Node;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.*;

public class NodeServerSocket {
    private final int port;
    private final Node cruzamento;
    private Lock lock;
    private final List<Semaforo> semaforos = new ArrayList<>();
    ControladorSemaforos controladorSemaforos;

    public NodeServerSocket(Node cruzamento, int port) {
        this.cruzamento = cruzamento;
        this.port = port;
        this.lock = new ReentrantLock();

        this.controladorSemaforos = new ControladorSemaforos(semaforos, lock);
        controladorSemaforos.start();
    }

    public void addSemaforo(Node cruzamentoOrigem){
        Condition podeAvancar = lock.newCondition();
        Semaforo newSemaforo = new Semaforo(cruzamento.getNome() + "-S" + semaforos.size(), cruzamentoOrigem, lock, podeAvancar);
        semaforos.add(newSemaforo);
        newSemaforo.start();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🏙️ " + cruzamento.getNome() + " à escuta na porta " + port + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> tratarLigacao(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no " + cruzamento.getNome() + ": " + e.getMessage());
        }
    }

    private void tratarLigacao(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Object obj = in.readObject();

            if (obj instanceof Mensagem msg) {
                System.out.println("📨 [" + cruzamento.getNome() + "] recebeu: " + msg);

                Veiculo v = msg.getVeiculo();
                Node cruzamentoOrigem = msg.getCruzamentoOrigem();

                Semaforo s = semaforos.stream()
                        .filter(sf -> cruzamentoOrigem.equals(sf.getCruzamentoOrigem()))
                        .findFirst()
                        .orElse(semaforos.getFirst());

                s.adicionarVeiculo(v);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro a receber veículo em " + cruzamento.getNome() + ": " + e.getMessage());
        }
    }

    //FALTA ENVIAR OS DADOS PARA O DASHBOARDSOCKET
}
