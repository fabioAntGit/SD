package src.common;

import src.nodes.Evento;

import java.io.Serializable;

public class Mensagem implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Evento cruzamentoOrigem;
    private final Evento cruzamentoDestino;
    private final Veiculo veiculo;


    public Mensagem(Evento cruzamentoOrigem, Evento cruzamentoDestino, Veiculo veiculo) {
        this.cruzamentoOrigem = cruzamentoOrigem;
        this.cruzamentoDestino = cruzamentoDestino;
        this.veiculo = veiculo;
    }

    public Evento getCruzamentoOrigem() { return cruzamentoOrigem; }
    public Evento getCruzamentoDestino() { return cruzamentoDestino; }
    public Veiculo getVeiculo() { return veiculo; }

    @Override
    public String toString() {
        return "Mensagem{" +
                ", origem='" + cruzamentoOrigem.getNome() + '\'' +
                ", destino='" + cruzamentoDestino.getNome() + '\'' +
                ", veiculo=" + (veiculo != null ? veiculo.getId() : "null") +
                '}';
    }
}