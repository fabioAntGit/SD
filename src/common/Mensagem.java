package src.common;

import src.nodes.Evento;
import src.nodes.Node;

import java.io.Serializable;

public class Mensagem implements Serializable {
    private final Node cruzamentoOrigem;
    private final Node cruzamentoDestino;
    private final Veiculo veiculo;


    public Mensagem(Node cruzamentoOrigem, Node cruzamentoDestino, Veiculo veiculo) {
        this.cruzamentoOrigem = cruzamentoOrigem;
        this.cruzamentoDestino = cruzamentoDestino;
        this.veiculo = veiculo;
    }

    public Node getCruzamentoOrigem() { return cruzamentoOrigem; }
    public Node getCruzamentoDestino() { return cruzamentoDestino; }
    public Veiculo getVeiculo() { return veiculo; }

    @Override
    public String toString() {
        return "Mensagem{" +
                ", origem='" + cruzamentoOrigem.getName()+ '\'' +
                ", destino='" + cruzamentoDestino.getName() + '\'' +
                ", veiculo=" + (veiculo != null ? veiculo.getId() : "null") +
                '}';
    }
}