package src.Core;

import src.Core.Data.Semaforo;
import java.util.Collection;
import java.util.List;

public class ControladorSemaforos extends Thread {
    private final List<Semaforo> semaforos;
    private final int TEMPO_VERDE = 4000;
    private final int TEMPO_INTERVALO = 300;
    private final int TEMPO_ESPERA = 300;

    /**
     * Cria um controlador que alterna a abertura entre varios semaforos.
     *
     * @param semaforos colecao de semaforos a gerir
     */
    public ControladorSemaforos(Collection<Semaforo> semaforos) {
        this.semaforos = List.copyOf(semaforos);
        setName("ControladorSemaforos");
        setDaemon(true);
    }

    /** Ciclo que escolhe o semaforo prioritario e faz a alternancia. */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Semaforo prioritario = escolherSemaforoMaisImportante();

            if (prioritario == null) {
                dormir(TEMPO_ESPERA);
                continue;
            }

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

    /**
     * Seleciona o semaforo com maior fila.
     *
     * @return semaforo escolhido ou null se nenhum disponivel
     */
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

        return melhor;
    }

    /**
     * Suspende a execucao por um periodo.
     *
     * @param millis duracao em milissegundos
     */
    private void dormir(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
