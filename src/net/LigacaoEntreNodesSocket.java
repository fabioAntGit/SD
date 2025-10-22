package src.net;

import src.common.Mensagem;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LigacaoEntreNodesSocket {

    private final String hostDestino;
    private final int portDestino;

    public LigacaoEntreNodesSocket(String hostDestino, int portDestino) {
        this.hostDestino = hostDestino;
        this.portDestino = portDestino;
    }

    public void enviarMensagem(Mensagem msg) {
        try (Socket socket = new Socket(hostDestino, portDestino);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // envia a mensagem completa
            out.writeObject(msg);
            out.flush();

            System.out.println("📤 [" + msg.getCruzamentoOrigem() + "] → " +
                    hostDestino + ":" + portDestino + " | Enviada: " + msg);

        } catch (IOException e) {
            System.err.println("❌ Erro ao enviar mensagem para " + hostDestino + ":" + portDestino);
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "LigacaoEntreCruzamentos{" +
                "destino='" + hostDestino + '\'' +
                ", porta=" + portDestino +
                '}';
    }
}