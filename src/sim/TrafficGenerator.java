package src.sim;

import src.nodes.GeradorCaminhos;
import src.nodes.Topologia;

import java.util.List;

public class TrafficGenerator {

    public static void main(String[] args) {

        Topologia topo = new Topologia();
        GeradorCaminhos gerador = new GeradorCaminhos(topo);

        for (int i = 0; i < 5; i++) {
            List<String> caminho = gerador.gerarCaminhoAleatorio();
            System.out.println("Veículo " + i + " → " + caminho);
        }
    }
}
