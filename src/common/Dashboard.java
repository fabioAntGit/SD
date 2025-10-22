package src.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//AINDA FALTA FAZER ISTO COMO DEVE SER



public class Dashboard {
    // Mapa: nome do cruzamento → última estatística recebida
    private final Map<String, MensagemDashboard> estatisticasPorCruzamento = new ConcurrentHashMap<>();

    public void atualizar(MensagemDashboard msg) {
        estatisticasPorCruzamento.put(msg.getCruzamentoNome(), msg);
    }

    public Map<String, MensagemDashboard> getEstatisticasPorCruzamento() {
        return estatisticasPorCruzamento;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== DASHBOARD ==========\n");

        if (estatisticasPorCruzamento.isEmpty()) {
            sb.append("Sem dados de cruzamentos ainda.\n");
            sb.append("================================\n");
            return sb.toString();
        }

        // Agregadores globais
        Map<String, Integer> totalPorTipoGlobal = new HashMap<>();
        int totalFilasAtuais = 0;
        int numSemaforos = 0;
        double somaTemposMedios = 0.0;

        // Mostrar estatísticas individuais
        for (MensagemDashboard msg : estatisticasPorCruzamento.values()) {
            sb.append("📍 ").append(msg.getCruzamentoNome()).append(" -> ").append(msg).append("\n");

            // Somar por tipo (CARRO, MOTO, CAMIAO)
            for (var entry : msg.getTotalPorTipo().entrySet()) {
                totalPorTipoGlobal.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }

            // Somar filas
            for (var entry : msg.getTamanhoFilas().entrySet()) {
                totalFilasAtuais += entry.getValue();
                numSemaforos++;
            }

            somaTemposMedios += msg.getTempoMedioPermanencia();
        }

        // Cálculo global
        double mediaTempoGlobal = somaTemposMedios / estatisticasPorCruzamento.size();
        double mediaFilaPorSemaforo = numSemaforos > 0 ? (double) totalFilasAtuais / numSemaforos : 0;

        // Mostrar resumo global
        sb.append("\n----- ESTATÍSTICAS GLOBAIS -----\n");
        sb.append("🚗 Totais por tipo: ").append(totalPorTipoGlobal).append("\n");
        sb.append("🚦 Fila média por semáforo: ").append(String.format("%.2f", mediaFilaPorSemaforo)).append("\n");
        sb.append("⏱️ Tempo médio global de permanência: ").append(String.format("%.2f", mediaTempoGlobal)).append("s\n");

        sb.append("================================\n");
        return sb.toString();
    }
}