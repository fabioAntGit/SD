package src.net;

import src.common.Veiculo;
import src.nodes.Node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SemaforoThread implements Runnable {
    private final Socket socket;
    private final Node node;

    public SemaforoThread(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Veiculo veiculo = (Veiculo) in.readObject();

            System.out.println(node.getName() + " recebeu " + veiculo.getId());

            // Simula tempo no semáforo
            Thread.sleep(veiculo.getTempoAtravessarSemaforo() * 1000);

            veiculo.avancar();
            Node proximo = veiculo.getProximo();
            if (proximo == null) {
                System.out.println("Veículo " + veiculo.getId() + " terminou o percurso!");
                return;
            }

            // Enviar para o próximo cruzamento
            Socket nextSocket = new Socket(proximo.getHost(), proximo.getPort());
            ObjectOutputStream out = new ObjectOutputStream(nextSocket.getOutputStream());
            out.writeObject(veiculo);
            out.flush();
            nextSocket.close();

            System.out.println("Enviado veículo " + veiculo.getId() +
                    " para " + proximo.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
