package src.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Topologia {
    private final Map<String, Node> graph = new HashMap<>();

    public Topologia() {
        graph.put("E1", new Node("E1", 5001, List.of("Cr1")));
        graph.put("E2", new Node("E2", 5002, List.of("Cr2")));
        graph.put("E3", new Node("E3", 5003, List.of("Cr3")));
        graph.put("Cr1", new Node("Cr1", 5011, List.of("Cr2", "Cr4")));
        graph.put("Cr2", new Node("Cr2", 5012, List.of("Cr1", "Cr3", "Cr5")));
        graph.put("Cr3", new Node("Cr3", 5013, List.of("Cr2", "S")));
        graph.put("Cr4", new Node("Cr4", 5014, List.of("Cr5")));
        graph.put("Cr5", new Node("Cr5", 5015, List.of("S")));
        graph.put("S",   new Node("S",   5020, List.of()));
    }

    public Node get(String name) {
        return graph.get(name);
    }

    public Set<String> getEntradas() {
        return Set.of("E1", "E2", "E3");
    }
}
