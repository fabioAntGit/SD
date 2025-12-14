package src.Estatisticas;

import java.io.Serializable;

public class TipoStats {
    private int count;
    private double soma;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    /**
     * Acumula uma nova medicao de tempo.
     *
     * @param valorSegundos tempo em segundos
     */
    void registar(double valorSegundos) {
        count++;
        soma += valorSegundos;
        min = Math.min(min, valorSegundos);
        max = Math.max(max, valorSegundos);
    }

    /**
     * Cria um instantaneo com os valores calculados.
     *
     * @return estatisticas atuais
     */
    public Snapshot snapshot() {
        double media = count > 0 ? soma / count : 0.0;
        double minOut = count > 0 ? min : 0.0;
        double maxOut = count > 0 ? max : 0.0;
        return new Snapshot(count, minOut, media, maxOut);
    }

    public record Snapshot(int count, double min, double media, double max) implements Serializable {}
}
