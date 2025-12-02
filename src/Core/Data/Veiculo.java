package src.Core.Data;

import src.Core.Enums.VeiculoType;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Veiculo implements Serializable {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final int id;
    private final VeiculoType tipo;
    private final No origem;
    private final List<No> percurso;
    private int posicaoAtual;
    private final LocalDateTime tempoEntrada;
    private LocalDateTime tempoSaida;
    private double tempoAtravessarSemaforo;
    private double tempoAteProximoNode;

    /**
     * Cria um veiculo com percurso pre-definido.
     *
     * @param tipo    categoria do veiculo
     * @param origem  no inicial
     * @param percurso lista ordenada de nos a visitar
     */
    public Veiculo(VeiculoType tipo, No origem, List<No> percurso) {
        this.id = counter.incrementAndGet();
        this.tipo = tipo;
        this.origem = origem;
        this.percurso = percurso;
        this.posicaoAtual = -1;
        this.tempoEntrada = LocalDateTime.now();

        double tempoBaseSemaforoMoto = 1.0;
        double tempoBaseDeslocamentoMoto = 1.5;

        switch (tipo) {
            case MOTO -> {
                tempoAtravessarSemaforo = tempoBaseSemaforoMoto;
                tempoAteProximoNode = tempoBaseDeslocamentoMoto;
            }
            case CARRO -> {
                tempoAtravessarSemaforo = 2 * tempoBaseSemaforoMoto;
                tempoAteProximoNode = 2 * tempoBaseDeslocamentoMoto;
            }
            case CAMIAO -> {
                tempoAtravessarSemaforo = 4 * tempoBaseSemaforoMoto;
                tempoAteProximoNode = 4 * tempoBaseDeslocamentoMoto;
            }
        }
    }

    /** @return identificador sequencial do veiculo. */
    public int getId() { return id; }

    /** @return tipo do veiculo. */
    public VeiculoType getTipo() { return tipo; }

    /** @return no de origem. */
    public No getOrigem() { return origem; }

    /** @return percurso completo do veiculo. */
    public List<No> getPercurso() { return percurso; }

    /** @return indice do no atual no percurso. */
    public int getPosicaoAtual() { return posicaoAtual; }

    /** @return instante de entrada no sistema. */
    public LocalDateTime getTempoEntrada() { return tempoEntrada; }

    /** @return instante de saida do sistema. */
    public LocalDateTime getTempoSaida() { return tempoSaida; }

    /**
     * Marca o tempo de saida do sistema.
     *
     * @param tempoSaida instante de saida
     */
    public void setTempoSaida(LocalDateTime tempoSaida) { this.tempoSaida = tempoSaida; }

    /** @return tempo em segundos para atravessar um semaforo. */
   public double getTempoAtravessarSemaforo() { return tempoAtravessarSemaforo; }

    /** @return tempo estimado ate o proximo no em segundos. */
    public double getTempoAteProximoNode() { return tempoAteProximoNode; }

    /**
     * Avanca a posicao atual para o proximo no do percurso.
     */
    public void avancar() {
        if (posicaoAtual < percurso.size() - 1)
            posicaoAtual++;
    }

    /**
     * Obtem o proximo no a visitar.
     *
     * @return proximo no ou null se o percurso terminou
     */
    public No getProximo() {
        if (posicaoAtual + 1 < percurso.size())
            return percurso.get(posicaoAtual + 1);
        return null;
    }

    /**
     * Calcula o tempo total no sistema somando tempo real e estimado.
     *
     * @return tempo total em segundos ou -1 se ainda nao saiu
     */
    public double getTempoTotal() {
        if (tempoEntrada == null || tempoSaida == null) return -1;
        double tempoReal = Duration.between(tempoEntrada, tempoSaida).toSeconds();
        double tempoEstimado = percurso.size() * tempoAteProximoNode;
        return tempoReal + tempoEstimado;
    }

    /**
     * @return representacao textual com identificador, tipo e percurso.
     */
    @Override
    public String toString() {
        String posStr = posicaoAtual == -1 ? origem.getName() : percurso.get(posicaoAtual).getName();
        return String.format(
                "Veiculo{id=%d, tipo=%s, origem=%s, pos=%s, percurso=%s}",
                id, tipo, origem.getName(), posStr,
                percurso.stream().map(No::getName).toList()
        );
    }
}
