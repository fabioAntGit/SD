package src.Core.Data;

import java.io.Serializable;
import java.util.List;

public class No implements Serializable {
    String name;
    String host;
    int port;
    List<String> neighbors;

    /**
     * Cria um no do grafo de trafego.
     *
     * @param name identificador do no
     * @param host endereco do servidor responsavel pelo no
     * @param port porta TCP do no
     * @param neighbors nomes dos nos vizinhos alcancaveis
     */
    public No(String name, String host, int port, List<String> neighbors) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.neighbors = neighbors;
    }

    /** @return nome do no. */
    public String getName() { return name; }

    /** @return host configurado para o no. */
    public String getHost() { return host; }

    /** @return porta onde o no escuta ligacoes. */
    public int getPort() { return port; }

    /** @return lista de vizinhos diretos do no. */
    public List<String> getNeighbors() { return neighbors; }
}
