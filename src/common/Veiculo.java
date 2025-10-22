package src.common;

import src.common.enums.VeiculoType;
import src.nodes.Node;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;

    // Contador global para gerar IDs únicos
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final int id;
    private final VeiculoType tipo;
    private final Node origem;          // Ex: "E1", "E2", "E3"
    private final List<Node> percurso;  // Sequência de cruzamentos: ["Cr1", "Cr2", "S"]
    private int posicaoAtual;             // Índice no percurso atual
    private final LocalDateTime tempoEntrada;    // Instante de entrada no sistema
    private LocalDateTime tempoSaida;            // Instante de saída (quando termina o percurso)
    private long tempoAtravessarSemaforo;
    private double tempoAteProximoNode;


    public Veiculo(VeiculoType tipo, Node origem, List<Node> percurso) {
        this.id = counter.incrementAndGet();
        this.tipo = tipo;
        this.origem = origem;
        this.percurso = percurso;
        this.tempoEntrada = LocalDateTime.now();
        this.tempoSaida = null; // ainda não saiu
        this.posicaoAtual = -1; // no inicio ele esta na origem, como a origem nao esta no percurso metemos -1
        switch (tipo){
            case MOTO -> this.tempoAtravessarSemaforo = 1;
            case CARRO -> this.tempoAtravessarSemaforo = 2;
            case CAMIAO -> this.tempoAtravessarSemaforo = 3;
        }
        switch (tipo){
            case MOTO -> this.tempoAteProximoNode = 1.5;
            case CARRO -> this.tempoAteProximoNode = 3;
            case CAMIAO -> this.tempoAteProximoNode = 6;
        }


    }

    public int getId() {
        return id;
    }

    public VeiculoType getTipo() {
        return tipo;
    }

    public Node getOrigem() {
        return origem;
    }

    public List<Node> getPercurso() {
        return percurso;
    }

    public LocalDateTime getTempoEntrada() {
        return tempoEntrada;
    }

    public LocalDateTime getTempoSaida() {
        return tempoSaida;
    }

    public int getPosicaoAtual() {
        return posicaoAtual;
    }

    /** Atualiza o tempo de saída quando o veículo termina o percurso */
    public void setTempoSaida(LocalDateTime tempoSaida) {
        this.tempoSaida = tempoSaida;
    }

    /** Move o veículo para o próximo cruzamento */
    public void avancar() {
        if (posicaoAtual < percurso.size() - 1) {
            posicaoAtual++;
        }
    }

    /** Retorna o próximo cruzamento, ou null se já terminou */
    public Node getProximoNode() {
        if (posicaoAtual + 1 < percurso.size()) {
            return percurso.get(posicaoAtual + 1);
        }
        return null;
    }

    /** Tempo total que o veículo passou no sistema (se já saiu) */
    public double getTempoTotal() {
        if (tempoSaida == null || tempoEntrada == null) {
            return -1; // ainda não saiu do sistema
        }

        double tempoEntreDatas = Duration.between(tempoEntrada, tempoSaida).toSeconds();

        double tempoDeslocacao = percurso.size() * tempoAteProximoNode;

        return tempoEntreDatas + tempoDeslocacao;
    }

    @Override
    public String toString() {
        return String.format("Vehicle{id=%d, tipo=%s, origem=%s, pos=%s, percurso=%s}",
                id, tipo, origem, percurso.get(posicaoAtual), percurso);
    }

    public long getTempoAtravessarSemaforo() {
        return tempoAtravessarSemaforo;
    }
}
