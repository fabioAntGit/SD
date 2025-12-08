package src.Servers;

import java.io.*;
import java.net.Socket;

public class ClientSocket implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;

    /**
     * Abre um socket e prepara o stream de objetos.
     *
     * @param host destino
     * @param port porta do destino
     * @throws IOException caso a ligacao falhe
     */
    public ClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    /** Envia um objeto serializado pela ligacao aberta. */
    private void enviar(Object objeto) throws IOException {
        out.writeObject(objeto);
        out.flush();
    }

    /** Fecha o socket subjacente. */
    @Override
    public void close() throws IOException {
        socket.close();
    }

    /**
     * Envia um objeto de forma inline sem criar sub-processos.
     *
     * @param host destino
     * @param port porta do destino
     * @param objeto objeto a serializar
     * @throws Exception em falha de envio
     */
    public static void enviarObjeto(String host, int port, Object objeto) throws Exception {
        try (ClientSocket cliente = new ClientSocket(host, port)) {
            cliente.enviar(objeto);
        }
    }

    /**
     * Envia um objeto diretamente ao servidor do dashboard.
     *
     * @param objeto evento a enviar
     * @throws Exception em falha de envio
     */
    public static void enviarParaDashboard(Object objeto) throws Exception {
        enviarObjeto(DashboardServer.HOST, DashboardServer.PORT, objeto);
    }
}
