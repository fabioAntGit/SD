package src.Core.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Topologia {
    private final Map<String, Evento> graph = new HashMap<>();

    public Topologia() {
        graph.put("E1", new Evento("E1", "127.0.0.1", 5001, List.of("Cr1")));
        graph.put("E2", new Evento("E2", "127.0.0.1", 5002, List.of("Cr2")));
        graph.put("E3", new Evento("E3", "127.0.0.1", 5003, List.of("Cr3")));

        graph.put("Cr1", new Evento("Cr1", "127.0.0.1", 5011, List.of("Cr4", "Cr2")));
        graph.put("Cr2", new Evento("Cr2", "127.0.0.1", 5012, List.of("Cr3", "Cr5", "Cr1")));
        graph.put("Cr3", new Evento("Cr3", "127.0.0.1", 5013, List.of("S", "Cr2")));
        graph.put("Cr4", new Evento("Cr4", "127.0.0.1", 5014, List.of("Cr5")));
        graph.put("Cr5", new Evento("Cr5", "127.0.0.1", 5015, List.of("S")));

        graph.put("S", new Evento("S", "127.0.0.1", 5020, List.of()));
    }

    public Evento get(String name) {
        return graph.get(name);
    }

    public Map<String, Evento> getGraph() {
        return graph;
    }

    public Set<String> getEntradas() {
        return Set.of("E1", "E2", "E3");
    }

    public List<Evento> getPredecessores(String destino) {
        return graph.values().stream()
                .filter(n -> n.getNeighbors().contains(destino))
                .toList();
    }
}
