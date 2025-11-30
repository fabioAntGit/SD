package src.Core.Data;

import src.Estatisticas.SemaforoStats;
import src.InterfaceGrafica.SwingInterface;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import src.Servers.SocketCliente;

public class Semaforo extends Thread {
    private final String idSemaforo;
    private final Evento cruzamentoOrigem;
    private final Deque<Veiculo> filaVeiculos = new ArrayDeque<>();
    private final SemaforoStats stats = new SemaforoStats();
    private final Lock lock = new ReentrantLock();
    private final Condition podeProcessar = lock.newCondition();
    private volatile boolean aberto = false;
    private volatile boolean emExecucao = true;

    public Semaforo(String idSemaforo, Evento cruzamentoOrigem) {
        this.idSemaforo = idSemaforo;
        this.cruzamentoOrigem = cruzamentoOrigem;
        SwingInterface.notificarEstadoSemaforo(idSemaforo, false, 0);
    }

    public void adicionarVeiculo(Veiculo veiculo) {
        lock.lock();
        try {
            filaVeiculos.addLast(veiculo);
            stats.registarEnfileiramento(filaVeiculos.size());
            SwingInterface.notificarEstadoSemaforo(idSemaforo, false, filaVeiculos.size());
            podeProcessar.signalAll();
        } finally {
            lock.unlock();
        }
        System.out.println("Veiculo " + veiculo.getId() + " chegou ao semaforo " + idSemaforo);
    }

    public int getNumeroVeiculos() {
        lock.lock();
        try {
            return filaVeiculos.size();
        } finally {
            lock.unlock();
        }
    }

    public String getIdSemaforo() {
        return idSemaforo;
    }

    public void abrir() {
        aberto = true;
        SwingInterface.notificarEstadoSemaforo(idSemaforo, true, filaVeiculos.size());
        lock.lock();
        try {
            podeProcessar.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void fechar() {
        aberto = false;
        SwingInterface.notificarEstadoSemaforo(idSemaforo, false, filaVeiculos.size());
    }

    @Override
    public void run() {
        while (emExecucao) {
            Veiculo veiculo;

            lock.lock();
            try {
                while (emExecucao && (filaVeiculos.isEmpty() || !aberto)) {
                    podeProcessar.await();
                }
                if (!emExecucao) {
                    return;
                }
                veiculo = filaVeiculos.pollFirst();
                stats.registarSaida(veiculo.getTipo(), filaVeiculos.size());
                SwingInterface.notificarEstadoSemaforo(idSemaforo, true, filaVeiculos.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }

            if (veiculo == null) {
                continue;
            }

            try {
                Thread.sleep(veiculo.getTempoAtravessarSemaforo() * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            Evento cruzamentoDestino = veiculo.getProximo();
            if (cruzamentoDestino == null) {
                veiculo.setTempoSaida(LocalDateTime.now());
                SwingInterface.notificarEnvio(idSemaforo, "S", veiculo);
                SwingInterface.notificarEstadoSemaforo(idSemaforo, false, 0);
                SwingInterface.notificarPosicaoVeiculo(null, veiculo);
                System.out.println("Veiculo " + veiculo.getId() + " terminou o percurso no semaforo " + idSemaforo);
                continue;
            }

            try (SocketCliente cliente = new SocketCliente(cruzamentoDestino.getHost(), cruzamentoDestino.getPort())) {
                cliente.enviarObjeto(veiculo);
                SwingInterface.notificarEnvio(idSemaforo, cruzamentoDestino.getName(), veiculo);

                System.out.println("Mensagem enviada de " + cruzamentoOrigem.getName() +
                        " -> " + cruzamentoDestino.getName() + " | Veiculo " + veiculo.getId());
            } catch (IOException e) {
                System.err.println("Erro ao enviar veiculo " + veiculo.getId() +
                        " para " + cruzamentoDestino.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.lock();
                try {
                    int filaRestante = filaVeiculos.size();
                    stats.registarEnfileiramento(filaRestante);
                    SwingInterface.notificarEstadoSemaforo(idSemaforo, filaRestante > 0, filaRestante);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public SemaforoStats.Snapshot getStatsSnapshot() {
        return stats.snapshot();
    }
}
