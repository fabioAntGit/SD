package src.InterfaceGrafica;

import src.Core.Data.No;
import src.Core.Data.Veiculo;
import src.InterfaceGrafica.SwingInterface.SemaforoStatus;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrafficMapPanel extends javax.swing.JPanel {
    private final Map<String, Point> coordenadas = Map.of(
            "E1", new Point(150, 50),  "E2", new Point(450, 50),  "E3", new Point(750, 50),
            "Cr1", new Point(150, 250), "Cr2", new Point(450, 250), "Cr3", new Point(750, 250),
            "Cr4", new Point(150, 450), "Cr5", new Point(450, 450), "S",   new Point(750, 450)
    );

    private record VehicleRender(Veiculo veiculo, String origem) {}

    private final Map<String, List<VehicleRender>> veiculosNosNos = new ConcurrentHashMap<>();
    private final Map<String, SemaforoStatus> semaforosStatus;

    /**
     * Painel que renderiza nos, veiculos e estados de semaforos.
     *
     * @param semaforosStatus mapa partilhado com o estado dos semaforos
     */
    public TrafficMapPanel(Map<String, SemaforoStatus> semaforosStatus) {
        this.semaforosStatus = semaforosStatus;
        setBackground(new Color(245, 245, 245));
        coordenadas.keySet().forEach(k -> veiculosNosNos.put(k, new ArrayList<>()));
    }

    /**
     * Atualiza a posicao de um veiculo, movendo-o para o no indicado.
     *
     * @param novoNo nome do no atual
     * @param v veiculo a posicionar
     */
    public void atualizarPosicao(String novoNo, Veiculo v) {
        for (List<VehicleRender> lista : veiculosNosNos.values()) {
            lista.removeIf(render -> render.veiculo().getId() == v.getId());
        }

        if (novoNo != null && veiculosNosNos.containsKey(novoNo)) {
            veiculosNosNos.get(novoNo).add(new VehicleRender(v, origemDe(v)));
        }
    }

    /**
     * Descobre o no de origem imediato para fins de renderizacao de direcao.
     *
     * @param v veiculo em movimento
     * @return nome do no de origem anterior ou vazio
     */
    private String origemDe(Veiculo v) {
        int pos = v.getPosicaoAtual();
        if (pos > 0 && pos - 1 < v.getPercurso().size()) {
            return v.getPercurso().get(pos - 1).getName();
        }
        No origem = v.getOrigem();
        return origem != null ? origem.getName() : "";
    }

    /** Desenha o mapa, os nos, semaforos e veiculos. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(140, 140, 140));
        g2.setStroke(new BasicStroke(3));

        desenharSeta(g2, "E1", "Cr1"); desenharSeta(g2, "E2", "Cr2"); desenharSeta(g2, "E3", "Cr3");
        desenharSeta(g2, "Cr1", "Cr4"); desenharSeta(g2, "Cr2", "Cr5"); desenharSeta(g2, "Cr3", "S");
        desenharSeta(g2, "Cr4", "Cr5"); desenharSeta(g2, "Cr5", "S");
        desenharSeta(g2, "Cr1", "Cr2"); desenharSeta(g2, "Cr2", "Cr1");
        desenharSeta(g2, "Cr2", "Cr3"); desenharSeta(g2, "Cr3", "Cr2");

        for (var entry : coordenadas.entrySet()) {
            String nome = entry.getKey();
            Point p = entry.getValue();

            g2.setColor(new Color(140, 170, 210));
            g2.fillOval(p.x - 25, p.y - 25, 50, 50);

            g2.setColor(new Color(30, 30, 30));
            g2.setStroke(new BasicStroke(1));
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nome, p.x - fm.stringWidth(nome)/2, p.y + fm.getAscent()/2 - 3);
        }

        for (var entry : semaforosStatus.entrySet()) {
            String key = entry.getKey();
            String[] partes = key.split("->");
            if (partes.length != 2) continue;
            String origem = partes[0];
            String destino = partes[1];

            Point destinoP = coordenadas.get(destino);
            if (destinoP == null) continue;

            Point offset = offsetSemaforo(origem, destino);
            int x = destinoP.x + offset.x;
            int y = destinoP.y + offset.y;

            SemaforoStatus status = entry.getValue();
            g2.setColor(status.ativo() ? new Color(0, 180, 0) : new Color(200, 60, 60));
            g2.fillOval(x - 8, y - 8, 16, 16);
            g2.setColor(Color.DARK_GRAY);
            g2.drawOval(x - 8, y - 8, 16, 16);
        }

        for (var entry : veiculosNosNos.entrySet()) {
            String destino = entry.getKey();
            Point pNo = coordenadas.get(destino);
            List<VehicleRender> lista = entry.getValue();

            Map<String, Integer> contadorDirecao = new java.util.HashMap<>();

            for (VehicleRender render : lista) {
                Veiculo v = render.veiculo();
                String origem = render.origem();
                String direcao = direcao(origem, destino);
                int idx = contadorDirecao.getOrDefault(direcao, 0);
                contadorDirecao.put(direcao, idx + 1);

                Point off = offsetVeiculo(direcao, idx);
                int x = pNo.x + off.x;
                int y = pNo.y + off.y;

                String emoji = switch (v.getTipo()) {
                    case CARRO -> "\uD83D\uDE97";
                    case MOTO -> "\uD83C\uDFCD\uFE0F";
                    case CAMIAO -> "\uD83D\uDE9B";
                };

                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                g2.drawString(emoji, x - 10, y + 6);
            g2.setColor(new Color(40, 40, 40));
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString(String.valueOf(v.getId()), x - 5, y - 12);
        }
    }
    }

    /**
     * Desenha uma linha representando ligacao entre dois nos.
     *
     * @param g2 contexto grafico
     * @param de no de origem
     * @param para no de destino
     */
    private void desenharSeta(Graphics2D g2, String de, String para) {
        if(!coordenadas.containsKey(de) || !coordenadas.containsKey(para)) return;
        Point p1 = coordenadas.get(de);
        Point p2 = coordenadas.get(para);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Ajusta a posicao visual de um semaforo em relacao ao no.
     *
     * @param origem nome do no de origem
     * @param destino nome do no de destino
     * @return deslocamento em relacao ao centro do no de destino
     */
    private Point offsetSemaforo(String origem, String destino) {
        switch (destino) {
            case "Cr2":
                if ("E2".equals(origem)) return new Point(0, -40);
                if ("Cr3".equals(origem)) return new Point(40, 0);
                return new Point(-40, 0);
            case "Cr1":
                if ("E1".equals(origem)) return new Point(0, -40);
                return new Point(40, 0);
            case "Cr3":
                if ("E3".equals(origem)) return new Point(0, -40);
                return new Point(-40, 0);
            case "Cr4":
                return new Point(0, -40);
            case "Cr5":
                if ("Cr2".equals(origem)) return new Point(0, -40);
                return new Point(-40, 0);
            case "S":
                if ("Cr3".equals(origem)) return new Point(0, -40);
                return new Point(-40, 0);
            default:
                return new Point(0, -40);
        }
    }

    /**
     * Determina a direcao de chegada do veiculo para calculo do offset.
     *
     * @param origem no anterior
     * @param destino no atual
     * @return identificador de direcao (top/right/left/bottom)
     */
    private String direcao(String origem, String destino) {
        return switch (destino) {
            case "Cr2" -> {
                if ("E2".equals(origem)) yield "top";
                if ("Cr3".equals(origem)) yield "right";
                yield "left";
            }
            case "Cr1" -> "E1".equals(origem) ? "top" : "right";
            case "Cr3" -> "E3".equals(origem) ? "top" : "left";
            case "Cr4" -> "top";
            case "Cr5" -> "Cr2".equals(origem) ? "top" : "left";
            case "S"   -> "Cr3".equals(origem) ? "top" : "left";
            default -> "top";
        };
    }

    /**
     * Calcula o deslocamento para desenhar um veiculo evitando sobreposicao.
     *
     * @param direcao direcao de origem
     * @param idx indice do veiculo nessa direcao
     * @return ponto com deslocamento
     */
    private Point offsetVeiculo(String direcao, int idx) {
        int espa = 20;
        return switch (direcao) {
            case "top" -> new Point(0, -70 - idx * espa);
            case "right" -> new Point(70 + idx * espa, 0);
            case "left" -> new Point(-70 - idx * espa, 0);
            case "bottom" -> new Point(0, 70 + idx * espa);
            default -> new Point(0, -70 - idx * espa);
        };
    }
}
