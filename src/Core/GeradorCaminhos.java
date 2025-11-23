package src.Core;

import src.Core.Data.Evento;
import src.Core.Data.Topologia;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeradorCaminhos {
    private final Topologia topologia;
    private final Random random = new Random();

    public GeradorCaminhos(Topologia topologia) {
        this.topologia = topologia;
    }

    public List<Evento> gerarCaminho(String entrada) {
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
                throw new IllegalArgumentException("Entrada inválida: " + entrada);
        }

        List<Evento> caminhoDeNos = new ArrayList<>();
        for (String nome : nomesCaminho) {
            Evento node = topologia.get(nome);
            if (node == null) {
                throw new RuntimeException("Node não encontrado na topologia: " + nome);
            }
            caminhoDeNos.add(node);
        }

        return caminhoDeNos;
    }
}