package src.Core.Data;

import java.io.Serializable;
import java.util.List;

public class Evento implements Serializable {
    String name;
    String host;
    int port;
    List<String> neighbors;

    public Evento(String name, String host, int port, List<String> neighbors) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.neighbors = neighbors;
    }

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public List<String> getNeighbors() { return neighbors; }
}
