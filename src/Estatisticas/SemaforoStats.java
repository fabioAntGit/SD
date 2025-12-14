package src.Estatisticas;

import src.Core.Enums.VeiculoType;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public class SemaforoStats {
    private int filaMax;
    private long somaFilasEventos;
    private long eventos;
    private final Map<VeiculoType, Integer> veiculosProcessados = new EnumMap<>(VeiculoType.class);

    /**
     * Regista o estado atual da fila quando um veiculo chega.
     *
     * @param filaAtual tamanho da fila apos o enfileiramento
     */
    public synchronized void registarEnfileiramento(int filaAtual) {
        filaMax = Math.max(filaMax, filaAtual);
        somaFilasEventos += filaAtual;
        eventos++;
    }

    /**
     * Regista a saida de um veiculo e o tamanho da fila no momento.
     *
     * @param tipo tipo do veiculo que saiu
     * @param filaAtual tamanho da fila apos a saida
     */
    public synchronized void registarSaida(VeiculoType tipo, int filaAtual) {
        filaMax = Math.max(filaMax, filaAtual);
        somaFilasEventos += filaAtual;
        eventos++;
        veiculosProcessados.merge(tipo, 1, Integer::sum);
    }

    /**
     * Produz um snapshot imutavel das estatisticas recolhidas.
     *
     * @return estatisticas consolidadas
     */
    public synchronized Snapshot snapshot() {
        double media = eventos > 0 ? (double) somaFilasEventos / eventos : 0.0;
        return new Snapshot(filaMax, media, new EnumMap<>(veiculosProcessados));
    }

    public record Snapshot(int filaMax, double filaMedia, Map<VeiculoType, Integer> processadosPorTipo) implements Serializable {}
}
