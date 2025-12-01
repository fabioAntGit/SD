package src.Estatisticas;

import src.Core.Data.Veiculo;
import src.Core.Enums.VeiculoType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public class GlobalStats {
    private final Map<VeiculoType, TipoStats> statsPorTipo = new EnumMap<>(VeiculoType.class);

    /** Inicializa os acumuladores para todos os tipos de veiculo. */
    public GlobalStats() {
        for (VeiculoType t : VeiculoType.values()) {
            statsPorTipo.put(t, new TipoStats());
        }
    }

    /**
     * Regista o tempo total de um veiculo concluido.
     *
     * @param veiculo veiculo com tempo de entrada e saida definidos
     */
    public synchronized void registar(Veiculo veiculo) {
        LocalDateTime entrada = veiculo.getTempoEntrada();
        LocalDateTime saida = veiculo.getTempoSaida();
        if (entrada == null || saida == null) return;
        double segundos = Duration.between(entrada, saida).toMillis() / 1000.0;
        statsPorTipo.get(veiculo.getTipo()).registar(segundos);
    }

    /**
     * Gera um snapshot consolidado por tipo de veiculo.
     *
     * @return estatisticas globais
     */
    public synchronized Snapshot snapshot() {
        Map<VeiculoType, TipoStats.Snapshot> map = new EnumMap<>(VeiculoType.class);
        statsPorTipo.forEach((k, v) -> map.put(k, v.snapshot()));
        return new Snapshot(map);
    }

    public record Snapshot(Map<VeiculoType, TipoStats.Snapshot> porTipo) implements java.io.Serializable {}
}
