package src.common;

import src.common.enums.VeiculoType;
import src.nodes.Evento;
import src.nodes.Node;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger counter = new AtomicInteger(0);

    private final int id;
    private final VeiculoType tipo;
    private final Node origem;
    private final List<Node> percurso;
    private int posicaoAtual;
    private final LocalDateTime tempoEntrada;
    private LocalDateTime tempoSaida;
    private long tempoAtravessarSemaforo;
    private double tempoAteProximoNode;

    public Veiculo(VeiculoType tipo, Node origem, List<Node> percurso) {
        this.id = counter.incrementAndGet();
        this.tipo = tipo;
        this.origem = origem;
        this.percurso = percurso;
        this.posicaoAtual = -1; // no início ainda não entrou no percurso
        this.tempoEntrada = LocalDateTime.now();

        switch (tipo) {
            case MOTO -> tempoAtravessarSemaforo = 1;
            case CARRO -> tempoAtravessarSemaforo = 2;
            case CAMIAO -> tempoAtravessarSemaforo = 3;
        }

        switch (tipo) {
            case MOTO -> tempoAteProximoNode = 1.5;
            case CARRO -> tempoAteProximoNode = 3;
            case CAMIAO -> tempoAteProximoNode = 6;
        }
    }

    // Getters e Setters
    public int getId() { return id; }

    public VeiculoType getTipo() { return tipo; }

    public Node getOrigem() { return origem; }

    public List<Node> getPercurso() { return percurso; }

    public int getPosicaoAtual() { return posicaoAtual; }

    public LocalDateTime getTempoEntrada() { return tempoEntrada; }

    public LocalDateTime getTempoSaida() { return tempoSaida; }

    public void setTempoSaida(LocalDateTime tempoSaida) { this.tempoSaida = tempoSaida; }

    public long getTempoAtravessarSemaforo() { return tempoAtravessarSemaforo; }

    public double getTempoAteProximoNode() { return tempoAteProximoNode; }

    // Avança para o próximo nó no percurso
    public void avancar() {
        if (posicaoAtual < percurso.size() - 1)
            posicaoAtual++;
    }

    // Retorna o próximo nó do percurso ou null se terminou
    public Node getProximo() {
        if (posicaoAtual + 1 < percurso.size())
            return percurso.get(posicaoAtual + 1);
        return null;
    }

    // Tempo total no sistema (em segundos)
    public double getTempoTotal() {
        if (tempoEntrada == null || tempoSaida == null) return -1;
        double tempoReal = Duration.between(tempoEntrada, tempoSaida).toSeconds();
        double tempoEstimado = percurso.size() * tempoAteProximoNode;
        return tempoReal + tempoEstimado;
    }

    @Override
    public String toString() {
        String posStr = posicaoAtual == -1 ? origem.getName() : percurso.get(posicaoAtual).getName();
        return String.format(
                "Veiculo{id=%d, tipo=%s, origem=%s, pos=%s, percurso=%s}",
                id, tipo, origem.getName(), posStr,
                percurso.stream().map(Node::getName).toList()
        );
    }
}