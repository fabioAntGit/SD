package src.Estatisticas;

import src.Core.Data.Veiculo;
import src.Core.Enums.VeiculoType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class VeiculoStats {
    private final Map<Integer, VeiculoSnapshot> statsPorVeiculo = new HashMap<>();

    /**
     * Armazena as estatisticas finais de um veiculo.
     *
     * @param v veiculo concluido
     */
    public synchronized void registar(Veiculo v) {
        VeiculoSnapshot snap = fromVeiculo(v);
        if (snap == null) return;

        statsPorVeiculo.put(snap.id(), snap);
    }

    /**
     * Obtem uma copia imutavel do estado atual.
     *
     * @return snapshot com estatisticas por veiculo
     */
    public synchronized Snapshot snapshot() {
        return new Snapshot(new HashMap<>(statsPorVeiculo));
    }

    /**
     * Construi um snapshot individual a partir de um veiculo.
     *
     * @param v veiculo de origem
     * @return estatisticas calculadas ou null se dados incompletos
     */
    public static VeiculoSnapshot fromVeiculo(Veiculo v) {
        if (v == null || v.getTempoEntrada() == null || v.getTempoSaida() == null) return null;

        double tempoNoSistema = Duration.between(v.getTempoEntrada(), v.getTempoSaida()).toSeconds();
        double tempoTotalEstimado = v.getTempoTotal();

        double tempoSemaforo = v.getTempoAtravessarSemaforo();
        double tempoNodes = v.getTempoAteProximoNode();

        return new VeiculoSnapshot(
                v.getId(),
                v.getTipo(),
                v.getTempoEntrada(),
                v.getTempoSaida(),
                tempoNoSistema,
                tempoSemaforo,
                tempoNodes,
                tempoTotalEstimado
        );
    }

    public record Snapshot(Map<Integer, VeiculoSnapshot> porVeiculo) implements java.io.Serializable {}

    public record VeiculoSnapshot(
            int id,
            VeiculoType tipo,
            LocalDateTime entrada,
            LocalDateTime saida,
            double tempoNoSistema,
            double tempoSemaforo,
            double tempoNodes,
            double tempoTotalEstimado
    ) implements java.io.Serializable {}
}
