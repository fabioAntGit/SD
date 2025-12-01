package src.Servers;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.lang.ProcessBuilder;

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

    /**
     * Envia um objeto serializado pela ligacao aberta.
     *
     * @param objeto objeto a enviar
     * @throws IOException em falha de E/S
     */
    public void enviarObjeto(Object objeto) throws IOException {
        out.writeObject(objeto);
        out.flush();
    }

    /** Fecha o socket subjacente. */
    @Override
    public void close() throws IOException {
        socket.close();
    }

    /**
     * Abre uma ligacao breve apenas para enviar um objeto.
     *
     * @param host destino
     * @param port porta do destino
     * @param objeto objeto serializado
     * @throws IOException em falha de rede
     */
    public static void enviar(String host, int port, Object objeto) throws IOException {
        try (ClientSocket cliente = new ClientSocket(host, port)) {
            cliente.enviarObjeto(objeto);
        }
    }

    /**
     * Inicia um processo separado para enviar um objeto como base64.
     *
     * @param host destino
     * @param port porta do destino
     * @param objeto objeto a serializar
     * @throws Exception em falha de serializacao ou execucao
     */
    public static void enviarObjeto(String host, int port, Object objeto) throws Exception {
        String base64 = serializarBase64(objeto);
        String cp = System.getProperty("java.class.path");
        ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", cp, "src.Servers.ClientSocket", host, String.valueOf(port), base64
        );
        Process p = pb.start();
        int exit = p.waitFor();
        if (exit != 0) {
            return;
        }
    }

    /**
     * Serializa o objeto e codifica em base64.
     *
     * @param objeto objeto a serializar
     * @return representacao em base64
     * @throws IOException em falha de serializacao
     */
    private static String serializarBase64(Object objeto) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(objeto);
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * Envia um objeto diretamente ao servidor do dashboard.
     *
     * @param objeto evento a enviar
     * @throws Exception em falha de envio
     */
    public static void enviarParaDashboard(Object objeto) throws Exception {
        enviarObjeto(src.Servers.DashboardServer.HOST, src.Servers.DashboardServer.PORT, objeto);
    }

    /**
     * Permite invocacao em sub-processo para envio de objetos.
     *
     * @param args host, porta e objeto codificado em base64
     * @throws Exception em falha de envio
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Uso: ClientSocket <host> <port> <objetoBase64>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String base64 = args[2];

        Object objeto = desserializar(base64);
        try {
            ClientSocket.enviar(host, port, objeto);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Reconstrui um objeto a partir de uma string base64.
     *
     * @param base64 payload serializado
     * @return objeto reconstruido
     * @throws Exception em falha de decodificacao
     */
    private static Object desserializar(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }
}
