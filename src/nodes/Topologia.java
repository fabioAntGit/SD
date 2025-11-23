package src.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Topologia {
    private final Map<String, Node> graph = new HashMap<>();

    public Topologia() {
        // Entradas
        graph.put("E1", new Node("E1", "127.0.0.1", 5001, List.of("Cr1")));
        graph.put("E2", new Node("E2", "127.0.0.1", 5002, List.of("Cr2")));
        graph.put("E3", new Node("E3", "127.0.0.1", 5003, List.of("Cr3")));

        // Cruzamentos
        graph.put("Cr1", new Node("Cr1", "127.0.0.1", 5011, List.of("Cr4", "Cr2")));
        graph.put("Cr2", new Node("Cr2", "127.0.0.1", 5012, List.of("Cr3", "Cr5", "Cr1")));
        graph.put("Cr3", new Node("Cr3", "127.0.0.1", 5013, List.of("S", "Cr2")));
        graph.put("Cr4", new Node("Cr4", "127.0.0.1", 5014, List.of("Cr5")));
        graph.put("Cr5", new Node("Cr5", "127.0.0.1", 5015, List.of("S")));

        // Saída
        graph.put("S", new Node("S", "127.0.0.1", 5020, List.of()));
    }

    public Node get(String name) {
        return graph.get(name);
    }

    public Map<String, Node> getGraph() {
        return graph;
    }

    public Set<String> getEntradas() {
        return Set.of("E1", "E2", "E3");
    }

    // Retorna lista de nos que apontam para "destino" (arestas de entrada)
    public List<Node> getPredecessores(String destino) {
        return graph.values().stream()
                .filter(n -> n.getNeighbors().contains(destino))
                .toList();
    }
}
