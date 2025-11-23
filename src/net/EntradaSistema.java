package src.net;

import src.common.Veiculo;
import src.nodes.Node;

public class EntradaSistema {

    public void enviarVeiculo(Veiculo veiculo) throws Exception {
        Node origem = veiculo.getOrigem();
        SocketCliente.enviar(origem.getHost(), origem.getPort(), veiculo);
        System.out.println("Veiculo enviado para " + origem.getName());
    }
}
