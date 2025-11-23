package src.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Pequeno wrapper para criar e enviar objetos via socket TCP.
 */
public class SocketCliente implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;

    public SocketCliente(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    public void enviarObjeto(Object objeto) throws IOException {
        out.writeObject(objeto);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public static void enviar(String host, int port, Object objeto) throws IOException {
        try (SocketCliente cliente = new SocketCliente(host, port)) {
            cliente.enviarObjeto(objeto);
        }
    }
}
