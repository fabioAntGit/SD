package src.net;

import src.common.Veiculo;
import src.nodes.Node;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class EntradaSistema {

    public void enviarVeiculo(Veiculo veiculo) throws Exception {
        Node origem = veiculo.getOrigem();

        Socket socket = new Socket(origem.getHost(), origem.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        out.writeObject(veiculo);
        out.flush();
        socket.close();

        System.out.println("Veículo enviado para " + origem.getName());
    }
}
