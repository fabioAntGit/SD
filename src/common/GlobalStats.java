package src.common;

import src.common.enums.VeiculoType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public class GlobalStats {
    private final Map<VeiculoType, TipoStats> statsPorTipo = new EnumMap<>(VeiculoType.class);

    public GlobalStats() {
        for (VeiculoType t : VeiculoType.values()) {
            statsPorTipo.put(t, new TipoStats());
        }
    }

    public synchronized void registrar(Veiculo veiculo) {
        LocalDateTime entrada = veiculo.getTempoEntrada();
        LocalDateTime saida = veiculo.getTempoSaida();
        if (entrada == null || saida == null) return;
        double segundos = Duration.between(entrada, saida).toMillis() / 1000.0;
        statsPorTipo.get(veiculo.getTipo()).registrar(segundos);
    }

    public synchronized Snapshot snapshot() {
        Map<VeiculoType, TipoStats.Snapshot> map = new EnumMap<>(VeiculoType.class);
        statsPorTipo.forEach((k, v) -> map.put(k, v.snapshot()));
        return new Snapshot(map);
    }

    public record Snapshot(Map<VeiculoType, TipoStats.Snapshot> porTipo) {}

    public static class TipoStats {
        private int count;
        private double soma;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;

        void registrar(double valorSegundos) {
            count++;
            soma += valorSegundos;
            min = Math.min(min, valorSegundos);
            max = Math.max(max, valorSegundos);
        }

        public Snapshot snapshot() {
            double media = count > 0 ? soma / count : 0.0;
            double minOut = count > 0 ? min : 0.0;
            double maxOut = count > 0 ? max : 0.0;
            return new Snapshot(count, minOut, media, maxOut);
        }

        public record Snapshot(int count, double min, double media, double max) {}
    }
}
