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
    private final Evento origem;
    private final List<Evento> percurso;
    private int posicaoAtual;
    private final LocalDateTime tempoEntrada;
    private LocalDateTime tempoSaida;
    private long tempoAtravessarSemaforo;
    private double tempoAteProximoNode;

    public Veiculo(VeiculoType tipo, Evento origem, List<Evento> percurso) {
        this.id = counter.incrementAndGet();
        this.tipo = tipo;
        this.origem = origem;
        this.percurso = percurso;
        this.posicaoAtual = -1;
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

    public int getId() { return id; }

    public VeiculoType getTipo() { return tipo; }

    public Evento getOrigem() { return origem; }

    public List<Evento> getPercurso() { return percurso; }

    public int getPosicaoAtual() { return posicaoAtual; }

    public LocalDateTime getTempoEntrada() { return tempoEntrada; }

    public LocalDateTime getTempoSaida() { return tempoSaida; }

    public void setTempoSaida(LocalDateTime tempoSaida) { this.tempoSaida = tempoSaida; }

    public long getTempoAtravessarSemaforo() { return tempoAtravessarSemaforo; }

    public double getTempoAteProximoNode() { return tempoAteProximoNode; }

    public void avancar() {
        if (posicaoAtual < percurso.size() - 1)
            posicaoAtual++;
    }

    public Evento getProximo() {
        if (posicaoAtual + 1 < percurso.size())
            return percurso.get(posicaoAtual + 1);
        return null;
    }

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
                percurso.stream().map(Evento::getName).toList()
        );
    }
}