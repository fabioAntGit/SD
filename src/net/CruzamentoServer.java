package src.net;

import src.nodes.Node;

import java.net.ServerSocket;
import java.net.Socket;

public class CruzamentoServer {
    private final Node node;

    public CruzamentoServer(Node node) {
        this.node = node;
    }

    public void start() throws Exception {
        ServerSocket server = new ServerSocket(node.getPort());
        System.out.println("Cruzamento " + node.getName() +
                " ativo na porta " + node.getPort());

        while (true) {
            Socket socket = server.accept();
            new Thread(new SemaforoThread(socket, node)).start();
        }
    }
}
