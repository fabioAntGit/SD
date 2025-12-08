package src.Core;

import src.Core.Data.Veiculo;
import src.Core.Data.No;
import src.Servers.ClientSocket;

public class EntradaSistema {
    /**
     * Envia o veiculo para o no de origem da sua rota.
     *
     * @param veiculo veiculo recem-criado
     * @throws Exception em falha de rede ou serializacao
     */
    public void enviarVeiculo(Veiculo veiculo) throws Exception {
        No origem = veiculo.getOrigem();
        ClientSocket.enviarObjeto(origem.getHost(), origem.getPort(), veiculo);
    }
}
