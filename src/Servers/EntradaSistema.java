package src.Servers;

import src.Core.Data.Veiculo;
import src.Core.Data.Evento;

public class EntradaSistema {
    public void enviarVeiculo(Veiculo veiculo) throws Exception {
        Evento origem = veiculo.getOrigem();
        SocketCliente.enviar(origem.getHost(), origem.getPort(), veiculo);
        System.out.println("Veiculo enviado para " + origem.getName());
    }
}