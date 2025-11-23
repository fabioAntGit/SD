package src.nodes;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    String host;
    int port;
    List<String> neighbors;

    public Node(String name, String host, int port, List<String> neighbors) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.neighbors = neighbors;
    }

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public List<String> getNeighbors() { return neighbors; }
    public List<Node> getNeighborsNodes(Topologia topologia) {
        return neighbors.stream()
                .map(topologia::get)
                .collect(Collectors.toList());
    }
}
