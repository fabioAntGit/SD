package src.Core.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Topologia {
    private final Map<String, No> graph = new HashMap<>();

    /** Cria a topologia fixa dos nos e cruzamentos. */
    public Topologia() {
        graph.put("E1", new No("E1", "127.0.0.1", 5001, List.of("Cr1")));
        graph.put("E2", new No("E2", "127.0.0.1", 5002, List.of("Cr2")));
        graph.put("E3", new No("E3", "127.0.0.1", 5003, List.of("Cr3")));

        graph.put("Cr1", new No("Cr1", "127.0.0.1", 5011, List.of("Cr4", "Cr2")));
        graph.put("Cr2", new No("Cr2", "127.0.0.1", 5012, List.of("Cr3", "Cr5", "Cr1")));
        graph.put("Cr3", new No("Cr3", "127.0.0.1", 5013, List.of("S", "Cr2")));
        graph.put("Cr4", new No("Cr4", "127.0.0.1", 5014, List.of("Cr5")));
        graph.put("Cr5", new No("Cr5", "127.0.0.1", 5015, List.of("S")));

        graph.put("S", new No("S", "127.0.0.1", 5020, List.of()));
    }

    /**
     * Obtem o no pelo nome.
     *
     * @param name identificador do no
     * @return no correspondente ou null se nao existir
     */
    public No get(String name) {
        return graph.get(name);
    }

    /** @return grafo completo da topologia. */
    public Map<String, No> getGraph() {
        return graph;
    }

    /** @return conjunto de nos considerados entradas do sistema. */
    public Set<String> getEntradas() {
        return Set.of("E1", "E2", "E3");
    }

    /**
     * Lista nos que apontam para um destino especifico.
     *
     * @param destino nome do no de destino
     * @return lista de nos predecessores
     */
    public List<No> getPredecessores(String destino) {
        return graph.values().stream()
                .filter(n -> n.getNeighbors().contains(destino))
                .toList();
    }
}
