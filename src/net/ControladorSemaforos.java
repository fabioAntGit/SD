package src.net;

import src.common.Semaforo;

import java.util.Collection;
import java.util.List;

/**
 * Controla abertura/fecho dos semaforos de um cruzamento, escolhendo o de maior fila.
 */
public class ControladorSemaforos extends Thread {
    private final List<Semaforo> semaforos;
    private final int TEMPO_VERDE = 4000;
    private final int TEMPO_INTERVALO = 300;
    private final int TEMPO_ESPERA = 300;

    public ControladorSemaforos(Collection<Semaforo> semaforos) {
        this.semaforos = List.copyOf(semaforos);
        setName("ControladorSemaforos");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Semaforo prioritario = escolherSemaforoMaisImportante();

            if (prioritario == null) {
                dormir(TEMPO_ESPERA);
                continue;
            }

            // Fecha todos os outros e abre o prioritario
            for (Semaforo s : semaforos) {
                if (s == prioritario) continue;
                s.fechar();
            }
            prioritario.abrir();

            dormir(TEMPO_VERDE);

            prioritario.fechar();
            dormir(TEMPO_INTERVALO);
        }
    }

    private Semaforo escolherSemaforoMaisImportante() {
        Semaforo melhor = null;
        int prioridadeMax = 0;

        for (Semaforo s : semaforos) {
            int prioridade = s.getNumeroVeiculos();
            if (prioridade > prioridadeMax) {
                prioridadeMax = prioridade;
                melhor = s;
            }
        }

        if (melhor != null) {
            System.out.println("Controlador escolheu " + melhor.getIdSemaforo() + " (fila = " + prioridadeMax + ")");
        }

        return melhor;
    }

    private void dormir(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
