package src.nodes;

import java.util.List;

public class Node {
    String name;
    int port;
    List<String> neighbors;

    public Node(String name, int port, List<String> neighbors) {
        this.name = name;
        this.port = port;
        this.neighbors = neighbors;
    }
}