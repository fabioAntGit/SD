package src.InterfaceGrafica;

import src.Core.Data.Veiculo;
import src.Core.Enums.VeiculoType;
import src.Estatisticas.VeiculoStats;
import src.Servers.CruzamentoServer;
import src.Servers.EntradaSistema;
import src.Core.GeradorCaminhos;
import src.Core.Data.Evento;
import src.Core.Data.Topologia;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import src.Estatisticas.SemaforoStats;
import src.Estatisticas.GlobalStats;

public class SwingInterface extends JFrame {
    private final JTextArea logArea = new JTextArea();
    private final TrafficMapPanel mapPanel = new TrafficMapPanel(); // Painel de desenho
    private final JButton startServersButton = new JButton("Iniciar servidores");
    private final JButton gerarVeiculoButton = new JButton("Gerar veiculo");
    private final JButton fluxoFixoButton = new JButton("Simulação automática");
    private final JComboBox<VeiculoType> tipoCombo = new JComboBox<>(VeiculoType.values());
    private final JComboBox<String> entradaCombo = new JComboBox<>();
    private final DefaultListModel<String> semaforosListModel = new DefaultListModel<>();
    private final DefaultListModel<String> movimentosListModel = new DefaultListModel<>();
    private final DefaultListModel<String> estatisticasListModel = new DefaultListModel<>();
    private final DefaultListModel<String> veiculosListModel = new DefaultListModel<>();
    private final JTextArea globalStatsArea = new JTextArea(5, 25);
    private Topologia topologia;
    private GeradorCaminhos geradorCaminhos;
    private volatile boolean servidoresAtivos;
    private final List<Thread> servidores = new ArrayList<>();
    private volatile boolean fluxoFixoAtivo;
    private Thread fluxoFixoThread;
    private static SwingInterface instance;
    private static final Map<String, SemaforoStatus> semaforosStatus = new ConcurrentHashMap<>();
    private static final Map<String, SemaforoStats.Snapshot> semaforosSnapshots = new ConcurrentHashMap<>();
    private static volatile GlobalStats.Snapshot globalSnapshot;

    public SwingInterface() {
        super("Simulador de Transito - Visualizacao em Tempo Real");
        instance = this;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(startServersButton);
        controls.add(new JLabel("Tipo:"));
        controls.add(tipoCombo);
        controls.add(new JLabel("Entrada:"));
        controls.add(entradaCombo);
        controls.add(gerarVeiculoButton);
        controls.add(fluxoFixoButton);

        gerarVeiculoButton.setEnabled(false);
        fluxoFixoButton.setEnabled(false);
        entradaCombo.setEnabled(false);

        startServersButton.addActionListener(e -> startServers());
        gerarVeiculoButton.addActionListener(e -> gerarVeiculo());
        fluxoFixoButton.addActionListener(e -> alternarFluxoFixo());

        add(controls, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setPreferredSize(new Dimension(800, 150));

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        JList<String> semaforosList = new JList<>(semaforosListModel);
        semaforosList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane semaforoScroll = new JScrollPane(semaforosList);
        semaforoScroll.setBorder(BorderFactory.createTitledBorder("Semaforos (ativo / fila)"));
        semaforoScroll.setPreferredSize(new Dimension(260, 260));

        JList<String> movimentosList = new JList<>(movimentosListModel);
        movimentosList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane movimentosScroll = new JScrollPane(movimentosList);
        movimentosScroll.setBorder(BorderFactory.createTitledBorder("Movimentos Veiculos"));
        movimentosScroll.setPreferredSize(new Dimension(260, 260));

        JList<String> veiculosList = new JList<>(veiculosListModel);
        veiculosList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane veiculosScroll = new JScrollPane(veiculosList);
        veiculosScroll.setBorder(BorderFactory.createTitledBorder("Stats Veiculos"));
        veiculosScroll.setPreferredSize(new Dimension(260, 260));

        JList<String> estatList = new JList<>(estatisticasListModel);
        estatList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane estatScroll = new JScrollPane(estatList);
        estatScroll.setBorder(BorderFactory.createTitledBorder("Stats Semaforos"));
        estatScroll.setPreferredSize(new Dimension(260, 260));

        globalStatsArea.setEditable(false);
        globalStatsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane globalScroll = new JScrollPane(globalStatsArea);
        globalScroll.setBorder(BorderFactory.createTitledBorder("Stats globais (S)"));
        globalScroll.setPreferredSize(new Dimension(260, 260));

        statusPanel.add(semaforoScroll);
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(movimentosScroll);
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(veiculosScroll);
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(estatScroll);
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(globalScroll);

        JSplitPane topo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, statusPanel);
        topo.setResizeWeight(0.75);

        splitPane.setTopComponent(topo);
        splitPane.setBottomComponent(scrollLog);
        splitPane.setResizeWeight(0.75);

        add(splitPane, BorderLayout.CENTER);

    }

    private void startServers() {
        if (servidoresAtivos) {
            appendLog("Servidores ja estao ativos.");
            return;
        }

        topologia = new Topologia();
        geradorCaminhos = new GeradorCaminhos(topologia);

        entradaCombo.removeAllItems();
        topologia.getEntradas().forEach(entradaCombo::addItem);
        if (entradaCombo.getItemCount() > 0) entradaCombo.setSelectedIndex(0);
        entradaCombo.setEnabled(true);

        for (Evento node : new ArrayList<>(topologia.getGraph().values())) {
            Thread thread = new Thread(() -> {
                try {
                    new CruzamentoServer(node, topologia).start();
                } catch (Exception e) {
                    appendLog("Falha ao iniciar " + node.getName() + ": " + e.getMessage());
                }
            }, "Servidor-" + node.getName());
            thread.setDaemon(true);
            thread.start();
            servidores.add(thread);
        }

        servidoresAtivos = true;
        startServersButton.setEnabled(false);
        gerarVeiculoButton.setEnabled(true);
        fluxoFixoButton.setEnabled(true);
        appendLog("Servidores iniciados. Mapa pronto.");
    }

    //TODO
    public void registarStatsVeiculo(Veiculo v) {

        int id = v.getId();
        LocalDateTime entrada = v.getTempoEntrada();
        LocalDateTime saida = v.getTempoSaida();

        String entradaStr = entrada != null ? entrada.toString() : "-";
        String saidaStr = saida != null ? saida.toString() : "-";

        double tempoTotal = v.getTempoTotal();
        long tempoSemaforo = v.getTempoAtravessarSemaforo();
        double tempoNodes = v.getTempoAteProximoNode();

        String linha = String.format(
                "%s | Entrada: %s | Saída: %s | Total: %.1fs | Nó: %.2fs | Semáforo: %ds",
                id, entradaStr, saidaStr, tempoTotal, tempoNodes, tempoSemaforo
        );

        veiculosListModel.addElement(linha);
    }

    private void gerarVeiculo() {
        if (!servidoresAtivos || topologia == null || geradorCaminhos == null) {
            appendLog("Inicie os servidores antes de gerar veiculos.");
            return;
        }

        gerarVeiculoButton.setEnabled(false);

        new Thread(() -> {
            try {
                gerarVeiculoEntrada((String) entradaCombo.getSelectedItem(), (VeiculoType) tipoCombo.getSelectedItem());

            } catch (Exception e) {
                appendLog("Erro ao gerar veiculo: " + e.getMessage());
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> gerarVeiculoButton.setEnabled(true));
            }
        }, "GeradorVeiculo").start();
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + LocalTime.now().withNano(0) + "] " + text + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void notificarPosicaoVeiculo(String nomeNo, Veiculo veiculo) {
        if (instance != null) {
            instance.registarMovimento(nomeNo, veiculo);
        }
    }

    public static void notificarEstadoSemaforo(String semaforoId, boolean ativo, int fila) {
        if (instance != null) {
            instance.atualizarEstadoSemaforo(semaforoId, ativo, fila);
        }
    }

    public static void notificarEnvio(String semaforoId, String destino, Veiculo veiculo) {
        if (instance != null) {
            instance.registarMovimentoSemaforo(semaforoId, destino, veiculo);
        }
    }

    public static void notificarStatsSemaforos(String cruzamento, Map<String, SemaforoStats.Snapshot> snaps) {
        if (instance != null) {
            snaps.forEach(semaforosSnapshots::put);
            instance.atualizarEstatisticasSemaforosUI();
        }
    }

    //TODO
    public static void notificarStatsVeiculos(String veiculoId, VeiculoStats.Snapshot snapshot) {
        if (instance != null) {
            instance.atualizarStatsVeiculosUI(snapshot);
        }
    }

    public static void notificarStatsGlobais(GlobalStats.Snapshot snapshot) {
        globalSnapshot = snapshot;
        if (instance != null) {
            instance.atualizarStatsGlobaisUI();
        }
    }

    private void registarMovimento(String nomeNo, Veiculo veiculo) {
        SwingUtilities.invokeLater(() -> {
            mapPanel.atualizarPosicao(nomeNo, veiculo);
            mapPanel.repaint();
        });
    }

    //TODO
    private void atualizarStatsVeiculosUI(VeiculoStats.Snapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            veiculosListModel.clear();

            snapshot.porVeiculo().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        VeiculoStats.VeiculoSnapshot s = entry.getValue();

                        String linha = String.format(
                                "V%-3d | %-7s | total=%.1fs | semaforos=%.1fs | nodes=%.1fs | estimado=%.1fs",
                                s.id(),
                                s.tipo(),
                                s.tempoNoSistema(),
                                s.tempoSemaforo(),
                                s.tempoNodes(),
                                s.tempoTotalEstimado()
                        );

                        veiculosListModel.addElement(linha);
                    });
        });
    }

    private void atualizarEstadoSemaforo(String semaforoId, boolean ativo, int fila) {
        semaforosStatus.put(semaforoId, new SemaforoStatus(ativo, fila));

        SwingUtilities.invokeLater(() -> {
            semaforosListModel.clear();
            semaforosStatus.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        SemaforoStatus status = entry.getValue();
                        String[] partes = entry.getKey().split("->");
                        String origem = partes.length == 2 ? partes[0] : "";
                        String destino = partes.length == 2 ? partes[1] : entry.getKey();
                        String label = destino + " <= " + origem;
                        String linha = String.format("%-14s | %s | fila=%d",
                                label,
                                status.ativo ? "VERDE" : "VERM",
                                status.fila);
                        semaforosListModel.addElement(linha);
                    });
            mapPanel.repaint();
        });
    }

    private void registarMovimentoSemaforo(String semaforoId, String destino, Veiculo veiculo) {
        SwingUtilities.invokeLater(() -> {
            String linha = "V" + veiculo.getId() + " via " + semaforoId + " -> " + destino;
            movimentosListModel.add(0, linha);
            while (movimentosListModel.size() > 50) {
                movimentosListModel.removeElementAt(movimentosListModel.size() - 1);
            }
        });
    }

    private void atualizarEstatisticasSemaforosUI() {
        SwingUtilities.invokeLater(() -> {
            estatisticasListModel.clear();
            semaforosSnapshots.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        SemaforoStats.Snapshot s = entry.getValue();
                        String linha = String.format("%s | filaAtual=%d filaMax=%d filaMedia=%.1f processados=%s",
                                entry.getKey(), s.filaAtual(), s.filaMax(), s.filaMedia(), s.processadosPorTipo());
                        estatisticasListModel.addElement(linha);
                    });
        });
    }

    private void atualizarStatsGlobaisUI() {
        SwingUtilities.invokeLater(() -> {
            if (globalSnapshot == null) {
                globalStatsArea.setText("Sem dados globais ainda.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Dwelling por tipo:\n");
            globalSnapshot.porTipo().forEach((tipo, snap) -> {
                sb.append(tipo).append(": ")
                        .append("veiculos=").append(snap.count())
                        .append(" tempoMin=").append(formatTempo(snap.min()))
                        .append(" tempoMedio=").append(formatTempo(snap.media()))
                        .append(" tempoMax=").append(formatTempo(snap.max()))
                        .append("\n");
            });
            globalStatsArea.setText(sb.toString());
        });
    }

    private String formatTempo(double segundos) {
        if (segundos >= 60) {
            double minutos = segundos / 60.0;
            return String.format("%.1fm", minutos);
        }
        return String.format("%.1fs", segundos);
    }

    private void alternarFluxoFixo() {
        if (!servidoresAtivos || topologia == null || geradorCaminhos == null) {
            appendLog("Inicie os servidores antes de iniciar o Simulação automática.");
            return;
        }
        if (fluxoFixoAtivo) {
            fluxoFixoAtivo = false;
            fluxoFixoButton.setText("Simulação automática");
            appendLog("Simulação automática parado.");
            return;
        }
        fluxoFixoAtivo = true;
        fluxoFixoButton.setText("Parar Simulação automática");
        appendLog("Simulação automática iniciada.");
        fluxoFixoThread = new Thread(this::loopFluxoFixo, "Simulação automática");
        fluxoFixoThread.setDaemon(true);
        fluxoFixoThread.start();
    }

    private void loopFluxoFixo() {
        Map<String, Long> intervalo = Map.of("E1", 2000L, "E2", 2500L, "E3", 3000L);
        Map<String, Long> proximoEnvio = new ConcurrentHashMap<>();
        long agora = System.currentTimeMillis();
        long finalAgora = agora;
        intervalo.forEach((k, v) -> proximoEnvio.put(k, finalAgora + v));

        while (fluxoFixoAtivo) {
            agora = System.currentTimeMillis();
            for (Map.Entry<String, Long> ent : proximoEnvio.entrySet()) {
                String entrada = ent.getKey();
                long when = ent.getValue();
                if (agora >= when) {
                    try {
                        gerarVeiculoEntrada(entrada, (VeiculoType) tipoCombo.getSelectedItem());
                    } catch (Exception e) {
                        appendLog("Erro ao gerar veiculo em " + entrada + ": " + e.getMessage());
                    }
                    proximoEnvio.put(entrada, agora + intervalo.get(entrada));
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                return;
            }
        }
    }

    private void gerarVeiculoEntrada(String entradaSelecionada, VeiculoType tipo) throws Exception {
        if (entradaSelecionada == null) return;
        List<Evento> caminho = geradorCaminhos.gerarCaminho(entradaSelecionada);
        Evento origem = caminho.get(0);

        Veiculo veiculo = new Veiculo(tipo, origem, caminho);

        String rotaStr = caminho.stream().map(Evento::getName).collect(Collectors.joining("->"));
        appendLog("Gerado: Veiculo " + veiculo.getId() + " (" + tipo + ") Rota: " + rotaStr);

        registarMovimento(origem.getName(), veiculo);

        new EntradaSistema().enviarVeiculo(veiculo);
    }

    class TrafficMapPanel extends JPanel {
        private final Map<String, Point> coordenadas = Map.of(
                "E1", new Point(150, 50),  "E2", new Point(450, 50),  "E3", new Point(750, 50),
                "Cr1", new Point(150, 250), "Cr2", new Point(450, 250), "Cr3", new Point(750, 250),
                "Cr4", new Point(150, 450), "Cr5", new Point(450, 450), "S",   new Point(750, 450)
        );

        private record VehicleRender(Veiculo veiculo, String origem) {}
        private final Map<String, List<VehicleRender>> veiculosNosNos = new ConcurrentHashMap<>();

        public TrafficMapPanel() {
            setBackground(Color.WHITE);
            coordenadas.keySet().forEach(k -> veiculosNosNos.put(k, new ArrayList<>()));
        }

        public void atualizarPosicao(String novoNo, Veiculo v) {
            for (List<VehicleRender> lista : veiculosNosNos.values()) {
                lista.removeIf(render -> render.veiculo().getId() == v.getId());
            }

            if (novoNo != null && veiculosNosNos.containsKey(novoNo)) {
                veiculosNosNos.get(novoNo).add(new VehicleRender(v, origemDe(v)));
            }
        }

        private String origemDe(Veiculo v) {
            int pos = v.getPosicaoAtual();
            if (pos > 0 && pos - 1 < v.getPercurso().size()) {
                return v.getPercurso().get(pos - 1).getName();
            }
            return v.getOrigem().getName();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(3));

            desenharSeta(g2, "E1", "Cr1"); desenharSeta(g2, "E2", "Cr2"); desenharSeta(g2, "E3", "Cr3");
            desenharSeta(g2, "Cr1", "Cr4"); desenharSeta(g2, "Cr2", "Cr5"); desenharSeta(g2, "Cr3", "S");
            desenharSeta(g2, "Cr4", "Cr5"); desenharSeta(g2, "Cr5", "S");
            desenharSeta(g2, "Cr1", "Cr2"); desenharSeta(g2, "Cr2", "Cr1");
            desenharSeta(g2, "Cr2", "Cr3"); desenharSeta(g2, "Cr3", "Cr2");

            for (var entry : coordenadas.entrySet()) {
                String nome = entry.getKey();
                Point p = entry.getValue();

                g2.setColor(new Color(230, 240, 255)); // Azul bem claro
                g2.fillOval(p.x - 25, p.y - 25, 50, 50);

                g2.setColor(Color.BLACK);
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
                g2.setColor(status.ativo ? new Color(0, 200, 0) : Color.RED);
                g2.fillOval(x - 8, y - 8, 16, 16);
                g2.setColor(Color.BLACK);
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
                        case CARRO -> "🚗";
                        case MOTO -> "🏍";
                        case CAMIAO -> "🚚";
                    };

                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                    g2.drawString(emoji, x - 10, y + 6);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString(String.valueOf(v.getId()), x - 5, y - 12);
                }
            }
        }

        private void desenharSeta(Graphics2D g2, String de, String para) {
            if(!coordenadas.containsKey(de) || !coordenadas.containsKey(para)) return;
            Point p1 = coordenadas.get(de);
            Point p2 = coordenadas.get(para);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

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

    private record SemaforoStatus(boolean ativo, int fila) {}
}
