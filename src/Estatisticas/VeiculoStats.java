package src.Estatisticas;

import src.Core.Data.Veiculo;
import src.Core.Enums.VeiculoType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class VeiculoStats {
    private final Map<Integer, VeiculoSnapshot> statsPorVeiculo = new HashMap<>();

    public synchronized void registar(Veiculo v) {
        if (v.getTempoEntrada() == null || v.getTempoSaida() == null) return;

        int id = v.getId();

        double tempoNoSistema = Duration.between(v.getTempoEntrada(), v.getTempoSaida()).toSeconds();
        double tempoTotalEstimado = v.getTempoTotal();

        double tempoSemaforo = v.getTempoAtravessarSemaforo();
        double tempoNodes = v.getTempoAteProximoNode() * v.getPercurso().size();

        VeiculoSnapshot snap = new VeiculoSnapshot(
                id,
                v.getTipo(),
                v.getTempoEntrada(),
                v.getTempoSaida(),
                tempoNoSistema,
                tempoSemaforo,
                tempoNodes,
                tempoTotalEstimado
        );

        statsPorVeiculo.put(id, snap);
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(new HashMap<>(statsPorVeiculo));
    }

    public record Snapshot(Map<Integer, VeiculoSnapshot> porVeiculo) {}

    public record VeiculoSnapshot(
            int id,
            VeiculoType tipo,
            LocalDateTime entrada,
            LocalDateTime saida,
            double tempoNoSistema,
            double tempoSemaforo,
            double tempoNodes,
            double tempoTotalEstimado
    ) {}
}
