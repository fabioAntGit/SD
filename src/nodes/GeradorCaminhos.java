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

    public List<Node> gerarCaminhoAleatorio() {
        List<String> entradas = new ArrayList<>(topologia.getEntradas());
        String atualName = entradas.get(random.nextInt(entradas.size()));
        Node atual = topologia.get(atualName);

        List<Node> caminho = new ArrayList<>();
        caminho.add(atual);

        // 2) gerar caminho até "S"
        while (!atual.getName().equals("S")) {
            List<Node> vizinhosNodes = atual.getNeighborsNodes(topologia);
            if (vizinhosNodes.isEmpty()) break;

            Node proximo = vizinhosNodes.get(random.nextInt(vizinhosNodes.size()));
            caminho.add(proximo);
            atual = proximo;
        }

        return caminho;
    }
}
