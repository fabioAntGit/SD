package src.net;

import src.common.Semaforo;

import java.util.List;
import java.util.concurrent.locks.Lock;

public class ControladorSemaforos extends Thread {
    private final List<Semaforo> semaforos;
    private final Lock lock;
    private final int TEMPO_VERDE = 4000;
    private final int TEMPO_INTERVALO = 1000; // tempo “tudo vermelho”

    public ControladorSemaforos(List<Semaforo> semaforos, Lock lock) {
        this.semaforos = semaforos;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (true) {
            Semaforo prioritario = escolherSemaforoMaisImportante();

            if (prioritario == null) continue;

            lock.lock();
            try {
                prioritario.ativar();
            } finally {
                lock.unlock();
            }

            try {
                Thread.sleep(TEMPO_VERDE);
            } catch (InterruptedException e) {
                return;
            }

            // pausa breve antes de ativar outro
            try {
                Thread.sleep(TEMPO_INTERVALO);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private Semaforo escolherSemaforoMaisImportante() {
        Semaforo melhor = null;
        int prioridadeMax = -1;

        for (Semaforo s : semaforos) {
            int prioridade = s.getNumeroVeiculos();
            if (prioridade > prioridadeMax) {
                prioridadeMax = prioridade;
                melhor = s;
            }
        }

        if (melhor != null) {
            System.out.println("🧮 Controlador escolheu " + melhor.getIdSemaforo() +
                    " (prioridade = " + prioridadeMax + ")");
        }

        return melhor;
    }
}