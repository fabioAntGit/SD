package src.common;

import src.nodes.Evento;
import src.nodes.Node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
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

    public Semaforo(String idSemaforo, Node cruzamentoOrigem, Lock lock, Condition podeAvancar) {
        this.idSemaforo = idSemaforo;
        this.cruzamentoOrigem = cruzamentoOrigem;
        this.listaVeiculos = new CopyOnWriteArrayList<>();
        this.lock = lock;
        this.podeAvancar = podeAvancar;
    }

    public void adicionarVeiculo(Veiculo v) {
        listaVeiculos.add(v);
        System.out.println("Veículo " + v.getId() + " chegou ao semáforo " + idSemaforo);
    }

    public int getNumeroVeiculos() {
        return listaVeiculos.size();
    }

    public void pararSemaforo() {
        ativo = false;
    }

    public String getIdSemaforo() {
        return this.idSemaforo;
    }

    public Node getCruzamentoOrigem() {
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
                // Espera até que o semáforo seja ativado
                while (!ativo) {
                    podeAvancar.await();
                }

                // Processa os veículos na fila
                if (!listaVeiculos.isEmpty()) {
                    // Retira o primeiro veículo da fila
                    Veiculo v = listaVeiculos.removeFirst();

                    // Simula tempo a atravessar o semáforo
                    Thread.sleep(v.getTempoAtravessarSemaforo() * 1000);

                    System.out.println(v + " passou no semáforo " + idSemaforo);

                    // Obtém o próximo cruzamento no percurso
                    Node cruzamentoDestino = v.getProximo();
                    if (cruzamentoDestino == null) {
                        // Veículo terminou o percurso
                        System.out.println("Veículo " + v.getId() + " terminou o percurso!");
                        v.setTempoSaida(LocalDateTime.now());
                        continue;
                    }

                    // Avança a posição do veículo
                    v.avancar();


                    // Cria a mensagem a enviar (se quiseres manter a classe Mensagem)
                    Mensagem msg = new Mensagem(
                            cruzamentoOrigem,  // este Semaforo pertence a um Node
                            cruzamentoDestino,
                            v
                    );

                    // Envia via TCP para o próximo cruzamento
                    try (Socket socket = new Socket(cruzamentoDestino.getHost(), cruzamentoDestino.getPort());
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                        out.writeObject(msg);
                        out.flush();

                        System.out.println("Mensagem enviada de " + cruzamentoOrigem.getName() +
                                " → " + cruzamentoDestino.getName() + " | Veículo " + v.getId());
                    } catch (IOException e) {
                        System.err.println("Erro ao enviar veículo " + v.getId() +
                                " para " + cruzamentoDestino.getName() + ": " + e.getMessage());
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                ativo = false;
            } finally {
                lock.unlock();
            }
        }
    }
}
