package src.sim;

import src.common.Veiculo;
import src.common.enums.VeiculoType;
import src.net.CruzamentoServer;
import src.net.EntradaSistema;
import src.nodes.GeradorCaminhos;
import src.nodes.Node;
import src.nodes.Topologia;

import java.util.List;

public class TrafficGenerator {

    public static void main(String[] args) throws Exception {
        Topologia topo = new Topologia();
        GeradorCaminhos gerador = new GeradorCaminhos(topo);

        for (String nome : topo.getGraph().keySet()) {
            Node n = topo.get(nome);
            new Thread(() -> {
                try {
                    new CruzamentoServer(n).start();
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }

        Thread.sleep(1000);

        // 2. Criar um veículo AO ENTRAR
        List<Node> caminho = gerador.gerarCaminhoAleatorio();
        Node origem = caminho.get(0);

        Veiculo veiculo = new Veiculo(VeiculoType.CARRO, origem, caminho);

        // 3. Enviar para o sistema
        new EntradaSistema().enviarVeiculo(veiculo);
    }
}
