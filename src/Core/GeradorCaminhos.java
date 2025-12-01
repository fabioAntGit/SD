package src.Core;

import src.Core.Data.No;
import src.Core.Data.Topologia;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeradorCaminhos {
    private final Topologia topologia;
    private final Random random = new Random();

    /**
     * Inicializa o gerador com a topologia conhecida.
     *
     * @param topologia grafo de nos e ligacoes
     */
    public GeradorCaminhos(Topologia topologia) {
        this.topologia = topologia;
    }

    /**
     * Gera um percurso completo partindo de uma entrada ate a saida S.
     *
     * @param entrada nome do no de entrada
     * @return lista ordenada de nos a visitar
     */
    public List<No> gerarCaminho(String entrada) {
        List<String> nomesCaminho;

        int p = random.nextInt(100);

        switch (entrada) {
            case "E1":
                if (p < 34) {
                    nomesCaminho = Arrays.asList("E1", "Cr1", "Cr4", "Cr5", "S");
                } else if (p < 67) {
                    nomesCaminho = Arrays.asList("E1", "Cr1", "Cr2", "Cr5", "S");
                } else {
                    nomesCaminho = Arrays.asList("E1", "Cr1", "Cr2", "Cr3", "S");
                }
                break;

            case "E2":
                if (p < 34) {
                    nomesCaminho = Arrays.asList("E2", "Cr2", "Cr5", "S");
                } else if (p < 67) {
                    nomesCaminho = Arrays.asList("E2", "Cr2", "Cr3", "S");
                } else {
                    nomesCaminho = Arrays.asList("E2", "Cr2", "Cr1", "Cr4", "Cr5", "S");
                }
                break;

            case "E3":
                if (p < 34) {
                    nomesCaminho = Arrays.asList("E3", "Cr3", "S");
                } else if (p < 67) {
                    nomesCaminho = Arrays.asList("E3", "Cr3", "Cr2", "Cr5", "S");
                } else {
                    nomesCaminho = Arrays.asList("E3", "Cr3", "Cr2", "Cr1", "Cr4", "Cr5", "S");
                }
                break;

            default:
                throw new IllegalArgumentException("Entrada invalida: " + entrada);
        }

        List<No> caminhoDeNos = new ArrayList<>();
        for (String nome : nomesCaminho) {
            No node = topologia.get(nome);
            if (node == null) {
                throw new RuntimeException("No nao encontrado na topologia: " + nome);
            }
            caminhoDeNos.add(node);
        }

        return caminhoDeNos;
    }
}
