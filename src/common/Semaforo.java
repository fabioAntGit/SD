package src.common;

import src.net.LigacaoEntreNodesSocket;
import src.nodes.Node;

import java.util.concurrent.locks.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Semaforo extends Thread {
    private final String idSemaforo;
    private final List<Veiculo> listaVeiculos;
    private final Node cruzamentoOrigem;
    private boolean ativo = false;
    private boolean emExecucao = true;
    private final Lock lock;
    private final Condition podeAvancar;

    public Semaforo(String idSemaforo, Node cruzamentoOrigem,Lock lock, Condition podeAvancar) {
        this.idSemaforo = idSemaforo;
        this.cruzamentoOrigem = cruzamentoOrigem;
        this.listaVeiculos = new CopyOnWriteArrayList<>();
        this.lock = lock;
        this.podeAvancar = podeAvancar;
    }

    public void adicionarVeiculo(Veiculo v) {
        listaVeiculos.add(v);
        System.out.println("🚗 " + v.getId() + " chegou ao semáforo " + idSemaforo);
    }

    public int getNumeroVeiculos() {
        return listaVeiculos.size();
    }

    public void pararSemaforo() {
        ativo = false;
    }

    public String getIdSemaforo(){
        return this.idSemaforo;
    }

    public Node getCruzamentoOrigem(){
        return this.cruzamentoOrigem;
    }

    public void ativar() {
        lock.lock();
        try {
            ativo = true;
            podeAvancar.signal();
        } finally {
            lock.unlock();
        }
    }

    public void parar() {
        emExecucao = false;
    }

    @Override
    public void run() {
        while (emExecucao) {
            lock.lock();
            try {
                while (!ativo) {
                    podeAvancar.await();
                }

                if (!listaVeiculos.isEmpty()) {
                    // Retira o primeiro veículo da fila
                    Veiculo v = listaVeiculos.removeFirst();
                    Thread.sleep(v.getTempoAtravessarSemaforo());

                    System.out.println("🚦 " + v + " passou no semáforo " + idSemaforo);

                    // --- Criação da mensagem a enviar ---
                    Node cruzamentoDestino = v.getProximoNode();

                    if (cruzamentoDestino == null) {
                        System.err.println("⚠️ " + cruzamentoOrigem.getNome() + " já saiu do sistema" );
                        continue;
                    }

                    // Cria a mensagem completa com origem, destino e veículo
                    Mensagem msg = new Mensagem(
                            cruzamentoOrigem,   // origem
                            cruzamentoDestino,  // destino
                            v                   // veículo
                    );

                    // --- Envia via socket ---
                    LigacaoEntreNodesSocket ligacao =
                            new LigacaoEntreNodesSocket(
                                    cruzamentoDestino.getHost(),  // IP destino (ex: "127.0.0.1")
                                    cruzamentoDestino.getPort()   // Porta TCP do destino (ex: 5001)
                            );

                    ligacao.enviarMensagem(msg);
                    System.out.println("📤 Mensagem enviada de " + cruzamentoOrigem.getNome() + " → " + cruzamentoDestino.getNome());
                }

            } catch (InterruptedException e) {
                ativo = false;
                Thread.currentThread().interrupt();
            }
        }
    }

}
