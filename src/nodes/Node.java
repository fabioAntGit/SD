package src.nodes;

import src.common.enums.NodeType;

public class Node {
    private String nome;
    private NodeType tipo;
    private final String host;
    private final int port;

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void Node(String nomeCruzamento){
        this.nome = nomeCruzamento;
    }

    public String getNome() {return nome;}

    public String getHost() { return host; }

    public int getPort() { return port; }
}
