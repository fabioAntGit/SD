package src.Core.Data;

import src.Estatisticas.SemaforoStats;
import src.Servers.ClientSocket;
import src.Core.Data.dtos.SemaforoStateEvent;
import src.Core.Data.dtos.MovementEvent;
import src.Core.Data.dtos.PositionEvent;
import src.Core.Data.dtos.SemaforoStatsEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Semaforo extends Thread {
    private final String idSemaforo;
    private final No cruzamentoOrigem;
    private final Deque<Veiculo> filaVeiculos = new ArrayDeque<>();
    private final SemaforoStats stats = new SemaforoStats();
    private final Lock lock = new ReentrantLock();
    private final Condition podeProcessar = lock.newCondition();
    private volatile boolean aberto = false;
    private volatile boolean emExecucao = true;

    /**
     * Cria um semaforo associado a um cruzamento de origem.
     *
     * @param idSemaforo identificador unico do semaforo
     * @param cruzamentoOrigem cruzamento que possui este semaforo
     */
    public Semaforo(String idSemaforo, No cruzamentoOrigem) {
        this.idSemaforo = idSemaforo;
        this.cruzamentoOrigem = cruzamentoOrigem;
        emitirEstado(0, false);
        enviarStats();
    }

    /**
     * Enfileira um veiculo para processamento e notifica o dashboard.
     *
     * @param veiculo veiculo que chegou ao semaforo
     */
    public void adicionarVeiculo(Veiculo veiculo) {
        lock.lock();
        try {
            filaVeiculos.addLast(veiculo);
            stats.registarEnfileiramento(filaVeiculos.size());
            enviarStats();
            emitirEstado(filaVeiculos.size(), false);
            podeProcessar.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Obtem o numero de veiculos aguardando no semaforo.
     *
     * @return quantidade de veiculos em fila
     */
    public int getNumeroVeiculos() {
        lock.lock();
        try {
            return filaVeiculos.size();
        } finally {
            lock.unlock();
        }
    }

    /** Abre o semaforo, permitindo passagem e acordando aguardantes. */
    public void abrir() {
        aberto = true;
        emitirEstado(filaVeiculos.size(), true);
        lock.lock();
        try {
            podeProcessar.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /** Fecha o semaforo e emite o estado atual para o dashboard. */
    public void fechar() {
        aberto = false;
        emitirEstado(filaVeiculos.size(), false);
    }

    /** Loop principal que processa a fila de veiculos enquanto em execucao. */
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
                enviarStats();
                emitirEstado(filaVeiculos.size(), true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }

            try {
                Thread.sleep((long) (veiculo.getTempoAtravessarSemaforo() * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            No cruzamentoDestino = veiculo.getProximo();
            if (cruzamentoDestino == null) {
                veiculo.setTempoSaida(LocalDateTime.now());
                emitirMovimento("S", veiculo);
                emitirEstado(0, false);
                emitirPosicao(null, veiculo);
                continue;
            }

            try {
                ClientSocket.enviarObjeto(cruzamentoDestino.getHost(), cruzamentoDestino.getPort(), veiculo);
                emitirMovimento(cruzamentoDestino.getName(), veiculo);
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
                    enviarStats();
                    emitirEstado(filaRestante, filaRestante > 0);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    /** Envia as estatisticas atuais do semaforo ao dashboard. */
    private void enviarStats() {
        try {
            ClientSocket.enviarParaDashboard(new SemaforoStatsEvent(Map.of(idSemaforo, stats.snapshot())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Emite estado de atividade e tamanho da fila para o dashboard.
     *
     * @param fila tamanho atual da fila
     * @param ativo indica se o semaforo esta a libertar veiculos
     */
    private void emitirEstado(int fila, boolean ativo) {
        try {
            ClientSocket.enviarParaDashboard(new SemaforoStateEvent(idSemaforo, ativo, fila));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Regista movimento de um veiculo entre nos.
     *
     * @param destino identificador do no de destino
     * @param veiculo veiculo em transito
     */
    private void emitirMovimento(String destino, Veiculo veiculo) {
        try {
            ClientSocket.enviarParaDashboard(new MovementEvent(idSemaforo, destino, veiculo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Atualiza a posicao do veiculo no dashboard.
     *
     * @param no identificador do no atual
     * @param veiculo veiculo posicionado
     */
    private void emitirPosicao(String no, Veiculo veiculo) {
        try {
            ClientSocket.enviarParaDashboard(new PositionEvent(no, veiculo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** @return instantaneo das estatisticas deste semaforo. */
    public SemaforoStats.Snapshot getStatsSnapshot() {
        return stats.snapshot();
    }
}
