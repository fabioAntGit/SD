package src.common;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Vehicle {

    // Contador global para gerar IDs únicos
    private static final AtomicInteger counter = new AtomicInteger(0);

    // Tipos possíveis de veículos
    public enum Type {
        MOTO, CARRO, CAMIAO
    }

    private final int id;
    private final Type tipo;
    private final String origem;          // Ex: "E1", "E2", "E3"
    private final List<String> percurso;  // Sequência de cruzamentos: ["Cr1", "Cr2", "S"]
    private int posicaoAtual;             // Índice no percurso atual

    private final double tempoEntrada;    // Instante de entrada no sistema
    private double tempoSaida;            // Instante de saída (quando termina o percurso)

    // --- Construtor ---
    public Vehicle(Type tipo, String origem, List<String> percurso, double tempoEntrada) {
        this.id = counter.incrementAndGet();
        this.tipo = tipo;
        this.origem = origem;
        this.percurso = percurso;
        this.tempoEntrada = tempoEntrada;
        this.tempoSaida = -1; // ainda não saiu
        this.posicaoAtual = 0;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public Type getTipo() {
        return tipo;
    }

    public String getOrigem() {
        return origem;
    }

    public List<String> getPercurso() {
        return percurso;
    }

    public double getTempoEntrada() {
        return tempoEntrada;
    }

    public double getTempoSaida() {
        return tempoSaida;
    }

    public int getPosicaoAtual() {
        return posicaoAtual;
    }

    // --- Métodos auxiliares ---

    /** Atualiza o tempo de saída quando o veículo termina o percurso */
    public void setTempoSaida(double tempoSaida) {
        this.tempoSaida = tempoSaida;
    }

    /** Move o veículo para o próximo nó do percurso (ex: próximo cruzamento) */
    public void avancar() {
        if (posicaoAtual < percurso.size() - 1) {
            posicaoAtual++;
        }
    }

    /** Retorna o próximo nó do percurso, ou null se já terminou */
    public String getProximoNo() {
        if (posicaoAtual + 1 < percurso.size()) {
            return percurso.get(posicaoAtual + 1);
        }
        return null;
    }

    /** Tempo total que o veículo passou no sistema (se já saiu) */
    public double getTempoTotal() {
        return (tempoSaida >= 0) ? (tempoSaida - tempoEntrada) : -1;
    }

    @Override
    public String toString() {
        return String.format("Vehicle{id=%d, tipo=%s, origem=%s, pos=%s, percurso=%s}",
                id, tipo, origem, percurso.get(posicaoAtual), percurso);
    }
}
