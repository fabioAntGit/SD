package src.common;

import src.common.enums.VeiculoType;

import java.util.EnumMap;
import java.util.Map;

public class SemaforoStats {
    private int filaAtual;
    private int filaMax;
    private long somaFilas;
    private long amostras;
    private final Map<VeiculoType, Integer> veiculosProcessados = new EnumMap<>(VeiculoType.class);

    public synchronized void registrarEnfileiramento(int filaAtual) {
        this.filaAtual = filaAtual;
        filaMax = Math.max(filaMax, filaAtual);
        somaFilas += filaAtual;
        amostras++;
    }

    public synchronized void registrarSaida(VeiculoType tipo, int filaAtual) {
        this.filaAtual = filaAtual;
        filaMax = Math.max(filaMax, filaAtual);
        somaFilas += filaAtual;
        amostras++;
        veiculosProcessados.merge(tipo, 1, Integer::sum);
    }

    public synchronized Snapshot snapshot() {
        double media = amostras > 0 ? (double) somaFilas / amostras : 0.0;
        return new Snapshot(filaAtual, filaMax, media, new EnumMap<>(veiculosProcessados));
    }

    public record Snapshot(int filaAtual, int filaMax, double filaMedia, Map<VeiculoType, Integer> processadosPorTipo) {}
}
