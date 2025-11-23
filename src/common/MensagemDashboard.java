package src.common;

import src.nodes.Evento;

import java.io.Serializable;
import java.util.Map;

public class MensagemDashboard implements Serializable {
    private final Evento cruzamento;
    private final Map<String, Integer> tamanhoFilas;  // semáforo -> nº veículos
    private final Map<String, Integer> totalPorTipo;  // tipo de veículo -> total
    private final double tempoMedioPermanencia;

    public MensagemDashboard(Evento cruzamento,
                             Map<String, Integer> tamanhoFilas,
                             Map<String, Integer> totalPorTipo,
                             double tempoMedioPermanencia) {
        this.cruzamento = cruzamento;
        this.tamanhoFilas = tamanhoFilas;
        this.totalPorTipo = totalPorTipo;
        this.tempoMedioPermanencia = tempoMedioPermanencia;
    }

    public String getCruzamentoNome() { return cruzamento.getNome(); }
    public Map<String, Integer> getTamanhoFilas() { return tamanhoFilas; }
    public Map<String, Integer> getTotalPorTipo() { return totalPorTipo; }
    public double getTempoMedioPermanencia() { return tempoMedioPermanencia; }

    @Override
    public String toString() {
        return "Filas=" + tamanhoFilas +
                " | Por tipo=" + totalPorTipo +
                " | Tempo médio=" + String.format("%.2f", tempoMedioPermanencia) + "s";
    }
}