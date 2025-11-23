package src.nodes;

import src.common.enums.EventoType;

public class Evento {
    private String nome;
    private EventoType tipo;
    private final String host;
    private final int port;

    public Evento(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public String getNome() {return nome;}

    public String getHost() { return host; }

    public int getPort() { return port; }
}
