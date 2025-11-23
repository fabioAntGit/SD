package src.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeradorCaminhos {
    private final Topologia topologia;
    private final Random random = new Random();

    public GeradorCaminhos(Topologia topologia) {
        this.topologia = topologia;
    }

    public List<String> gerarCaminhoAleatorio() {
        // 1) escolher entrada aleatória
        List<String> entradas = new ArrayList<>(topologia.getEntradas());
        String atual = entradas.get(random.nextInt(entradas.size()));

        List<String> caminho = new ArrayList<>();
        caminho.add(atual);

        // 2) gerar caminho até S
        while (!atual.equals("S")) {
            Node node = topologia.get(atual);

            if (node.neighbors.isEmpty())
                break; // isto só acontece em S

            List<String> vizinhos = node.neighbors;
            String proximo = vizinhos.get(random.nextInt(vizinhos.size()));

            caminho.add(proximo);
            atual = proximo;
        }
        return caminho;
    }
}
