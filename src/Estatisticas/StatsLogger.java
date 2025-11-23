package src.Estatisticas;

import src.InterfaceGrafica.SwingInterface;
import src.Core.Data.Semaforo;
import java.util.HashMap;
import java.util.Map;

public class StatsLogger extends Thread {
    private final String nomeNode;
    private final Map<String, Semaforo> semaforos;

    public StatsLogger(String nomeNode, Map<String, Semaforo> semaforos) {
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
