package src.InterfaceGrafica;

import src.Core.Data.Veiculo;
import src.Core.Enums.VeiculoType;
import src.Estatisticas.VeiculoStats;
import src.Servers.EntradaSistema;
import src.Core.GeradorCaminhos;
import src.Core.Data.No;
import src.Core.Data.Topologia;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import src.Estatisticas.SemaforoStats;
import src.Estatisticas.GlobalStats;
import src.Servers.DashboardServer;

public class SwingInterface extends JFrame {
    private final JTextArea logArea = new JTextArea();
    private TrafficMapPanel mapPanel;
    private final JButton startServersButton = new JButton("Iniciar servidores");
    private final JButton gerarVeiculoButton = new JButton("Gerar veiculo");
    private final JButton fluxoFixoButton = new JButton("Simulacao automatica");
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
    private final List<Process> servidores = new ArrayList<>();
    private volatile boolean fluxoFixoAtivo;
    private Thread fluxoFixoThread;
    private static SwingInterface instance;
    private static final Map<String, SemaforoStatus> semaforosStatus = new ConcurrentHashMap<>();
    private static final Map<String, SemaforoStats.Snapshot> semaforosSnapshots = new ConcurrentHashMap<>();
    private static volatile GlobalStats.Snapshot globalSnapshot;
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DashboardServer dashboardServer = new DashboardServer();
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color PANEL_DARK = new Color(45, 45, 45);
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color BORDER_DARK = new Color(70, 70, 70);

    /** Inicializa a interface grafica e o servidor do dashboard. */
    public SwingInterface() {
        super("Simulador de Transito - Visualizacao em Tempo Real");
        instance = this;
        dashboardServer.start();
        mapPanel = new TrafficMapPanel(semaforosStatus);

        aplicarTemaEscuro();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopServers();
            }
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBackground(BG_DARK);
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
        logArea.setBackground(PANEL_DARK);
        logArea.setForeground(TEXT_LIGHT);
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setPreferredSize(new Dimension(800, 150));
        scrollLog.getViewport().setBackground(PANEL_DARK);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(BG_DARK);

        JList<String> semaforosList = new JList<>(semaforosListModel);
        semaforosList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        semaforosList.setBackground(PANEL_DARK);
        semaforosList.setForeground(TEXT_LIGHT);
        JScrollPane semaforoScroll = new JScrollPane(semaforosList);
        semaforoScroll.setBorder(BorderFactory.createTitledBorder("Semaforos (ativo / fila)"));
        semaforoScroll.setPreferredSize(new Dimension(260, 260));
        semaforoScroll.getViewport().setBackground(PANEL_DARK);

        JList<String> movimentosList = new JList<>(movimentosListModel);
        movimentosList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        movimentosList.setBackground(PANEL_DARK);
        movimentosList.setForeground(TEXT_LIGHT);
        JScrollPane movimentosScroll = new JScrollPane(movimentosList);
        movimentosScroll.setBorder(BorderFactory.createTitledBorder("Movimentos Veiculos"));
        movimentosScroll.setPreferredSize(new Dimension(260, 260));
        movimentosScroll.getViewport().setBackground(PANEL_DARK);

        JList<String> veiculosList = new JList<>(veiculosListModel);
        veiculosList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        veiculosList.setBackground(PANEL_DARK);
        veiculosList.setForeground(TEXT_LIGHT);
        JScrollPane veiculosScroll = new JScrollPane(veiculosList);
        veiculosScroll.setBorder(BorderFactory.createTitledBorder("Stats Veiculos"));
        veiculosScroll.setPreferredSize(new Dimension(260, 260));
        veiculosScroll.getViewport().setBackground(PANEL_DARK);

        JList<String> estatList = new JList<>(estatisticasListModel);
        estatList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        estatList.setBackground(PANEL_DARK);
        estatList.setForeground(TEXT_LIGHT);
        JScrollPane estatScroll = new JScrollPane(estatList);
        estatScroll.setBorder(BorderFactory.createTitledBorder("Stats Semaforos"));
        estatScroll.setPreferredSize(new Dimension(260, 260));
        estatScroll.getViewport().setBackground(PANEL_DARK);

        globalStatsArea.setEditable(false);
        globalStatsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        globalStatsArea.setBackground(PANEL_DARK);
        globalStatsArea.setForeground(TEXT_LIGHT);
        JScrollPane globalScroll = new JScrollPane(globalStatsArea);
        globalScroll.setBorder(BorderFactory.createTitledBorder("Stats globais (S)"));
        globalScroll.setPreferredSize(new Dimension(260, 260));
        globalScroll.getViewport().setBackground(PANEL_DARK);

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

    /** Inicia processos dos servidores e prepara controlos da UI. */
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

        String cp = System.getProperty("java.class.path");
        for (No node : new ArrayList<>(topologia.getGraph().values())) {
            try {
                Process p = new ProcessBuilder("java", "-cp", cp, "src.Servers.NodeServer", node.getName())
                        .inheritIO()
                        .start();
                servidores.add(p);
            } catch (Exception e) {
                appendLog("Falha ao iniciar " + node.getName() + ": " + e.getMessage());
            }
        }

        servidoresAtivos = true;
        startServersButton.setEnabled(false);
        gerarVeiculoButton.setEnabled(true);
        fluxoFixoButton.setEnabled(true);
        appendLog("Servidores iniciados. Mapa pronto.");
    }

    /** Aplica o tema escuro a componentes Swing padrao. */
    private void aplicarTemaEscuro() {
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("OptionPane.background", BG_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_LIGHT);
        UIManager.put("TextArea.background", PANEL_DARK);
        UIManager.put("TextArea.foreground", TEXT_LIGHT);
        UIManager.put("List.background", PANEL_DARK);
        UIManager.put("List.foreground", TEXT_LIGHT);
        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("Label.foreground", TEXT_LIGHT);
        UIManager.put("Button.background", PANEL_DARK);
        UIManager.put("Button.foreground", TEXT_LIGHT);
        UIManager.put("ComboBox.background", PANEL_DARK);
        UIManager.put("ComboBox.foreground", TEXT_LIGHT);
    }

    /** Finaliza processos dos servidores e repoe controlos. */
    private void stopServers() {
        fluxoFixoAtivo = false;
        for (Process p : servidores) {
            try {
                p.destroy();
            } catch (Exception ignored) {}
            try {
                p.destroyForcibly();
            } catch (Exception ignored) {}
        }
        servidores.clear();
        servidoresAtivos = false;
        startServersButton.setEnabled(true);
        gerarVeiculoButton.setEnabled(false);
        fluxoFixoButton.setEnabled(false);
        appendLog("Servidores terminados.");
    }

    /**
     * Regista e exibe estatisticas agregadas de um veiculo concluido.
     *
     * @param v veiculo processado
     */
    public void registarStatsVeiculo(Veiculo v) {

        VeiculoStats.VeiculoSnapshot snap = VeiculoStats.fromVeiculo(v);
        if (snap == null) return;

        SwingUtilities.invokeLater(() -> {
            removerVeiculoDaLista(snap.id());
            veiculosListModel.add(0, formatVeiculoSnapshot(snap));
            while (veiculosListModel.size() > 100) {
                veiculosListModel.removeElementAt(veiculosListModel.size() - 1);
            }
        });
    }

    /** Gera um veiculo a partir da entrada selecionada. */
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

    /**
     * Adiciona texto ao log da interface com timestamp.
     *
     * @param text mensagem a registar
     */
    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + LocalTime.now().withNano(0) + "] " + text + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Atualiza mapa com nova posicao de um veiculo.
     *
     * @param nomeNo no atual
     * @param veiculo veiculo movido
     */
    public static void notificarPosicaoVeiculo(String nomeNo, Veiculo veiculo) {
        if (instance != null) {
            instance.registarMovimento(nomeNo, veiculo);
        }
    }

    /**
     * Atualiza estado de um semaforo na UI.
     *
     * @param semaforoId identificador do semaforo
     * @param ativo se esta libertado
     * @param fila tamanho da fila
     */
    public static void notificarEstadoSemaforo(String semaforoId, boolean ativo, int fila) {
        if (instance != null) {
            instance.atualizarEstadoSemaforo(semaforoId, ativo, fila);
        }
    }

    /**
     * Regista envio de veiculo entre semaforos.
     *
     * @param semaforoId id do semaforo
     * @param destino no destino
     * @param veiculo veiculo enviado
     */
    public static void notificarEnvio(String semaforoId, String destino, Veiculo veiculo) {
        if (instance != null) {
            instance.registarMovimentoSemaforo(semaforoId, destino, veiculo);
        }
    }

    /**
     * Recebe estatisticas de semaforos e atualiza a UI.
     *
     * @param cruzamento identificador da origem
     * @param snaps mapa de snapshots por semaforo
     */
    public static void notificarStatsSemaforos(String cruzamento, Map<String, SemaforoStats.Snapshot> snaps) {
        if (instance != null) {
            snaps.forEach(semaforosSnapshots::put);
            instance.atualizarEstatisticasSemaforosUI();
        }
    }

    /**
     * Atualiza estatisticas agregadas de veiculos na UI.
     *
     * @param veiculoId id do veiculo
     * @param snapshot dados do veiculo
     */
    public static void notificarStatsVeiculos(String veiculoId, VeiculoStats.Snapshot snapshot) {
        if (instance != null) {
            instance.atualizarStatsVeiculosUI(snapshot);
        }
    }

    /**
     * Regista estatisticas de um veiculo individual.
     *
     * @param veiculo veiculo concluido
     */
    public static void notificarStatsVeiculo(Veiculo veiculo) {
        if (instance != null) {
            instance.registarStatsVeiculo(veiculo);
        }
    }

    /**
     * Atualiza o snapshot global de estatisticas.
     *
     * @param snapshot estado global
     */
    public static void notificarStatsGlobais(GlobalStats.Snapshot snapshot) {
        globalSnapshot = snapshot;
        if (instance != null) {
            instance.atualizarStatsGlobaisUI();
        }
    }

    /**
     * Atualiza a posicao de um veiculo no painel de mapa.
     *
     * @param nomeNo no atual
     * @param veiculo veiculo movido
     */
    private void registarMovimento(String nomeNo, Veiculo veiculo) {
        SwingUtilities.invokeLater(() -> {
            mapPanel.atualizarPosicao(nomeNo, veiculo);
            mapPanel.repaint();
        });
    }

    /**
     * Refresca lista de estatisticas por veiculo.
     *
     * @param snapshot snapshot a exibir
     */
    private void atualizarStatsVeiculosUI(VeiculoStats.Snapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            veiculosListModel.clear();

            snapshot.porVeiculo().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .map(this::formatVeiculoSnapshot)
                    .forEach(veiculosListModel::addElement);
        });
    }

    /**
     * Atualiza o estado local de um semaforo e repinta o painel.
     *
     * @param semaforoId identificador do semaforo
     * @param ativo se esta aberto
     * @param fila tamanho da fila
     */
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
                        String label = destino + " vindo de " + origem;
                        String linha = String.format("%-14s | %s | fila=%d",
                                label,
                                status.ativo ? "\uD83D\uDFE2" : "\uD83D\uDD34",
                                status.fila);
                        semaforosListModel.addElement(linha);
                    });
            mapPanel.repaint();
        });
    }

    /**
     * Regista um movimento de veiculo por um semaforo para exibir na lista.
     *
     * @param semaforoId id do semaforo
     * @param destino no destino
     * @param veiculo veiculo enviado
     */
    private void registarMovimentoSemaforo(String semaforoId, String destino, Veiculo veiculo) {
        SwingUtilities.invokeLater(() -> {
            String linha = "V" + veiculo.getId() + " via " + semaforoId + " -> " + destino;
            movimentosListModel.add(0, linha);
            while (movimentosListModel.size() > 50) {
                movimentosListModel.removeElementAt(movimentosListModel.size() - 1);
            }
        });
    }

    /** Atualiza a lista com snapshots de estatisticas por semaforo. */
    private void atualizarEstatisticasSemaforosUI() {
        SwingUtilities.invokeLater(() -> {
            estatisticasListModel.clear();
            semaforosSnapshots.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        SemaforoStats.Snapshot s = entry.getValue();
                        String linha = String.format("%s | filaMax=%d filaMedia=%.1f processados=%s",
                                entry.getKey(), s.filaMax(), s.filaMedia(), s.processadosPorTipo());
                        estatisticasListModel.addElement(linha);
                    });
        });
    }

    /** Atualiza painel textual com dados globais acumulados. */
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

    /**
     * Formata um valor em segundos para string amigavel.
     *
     * @param segundos tempo em segundos
     * @return representacao com sufixo m ou s
     */
    private String formatTempo(double segundos) {
        if (segundos >= 60) {
            double minutos = segundos / 60.0;
            return String.format("%.1fm", minutos);
        }
        return String.format("%.1fs", segundos);
    }

    /**
     * Converte um snapshot de veiculo em linha descritiva.
     *
     * @param s snapshot a formatar
     * @return texto pronto para a lista
     */
    private String formatVeiculoSnapshot(VeiculoStats.VeiculoSnapshot s) {
        String entrada = s.entrada() != null ? s.entrada().format(HORA_FMT) : "-";
        String saida = s.saida() != null ? s.saida().format(HORA_FMT) : "-";
        return String.format(
                "V%-3d | Entrada:%s  Saida:%s | Tempo total=%.1fs | Tempo para arrancar=%.1fs | Tempo entre cruzamentos=%.1fs",
                s.id(),
                entrada,
                saida,
                s.tempoNoSistema(),
                s.tempoSemaforo(),
                s.tempoNodes()
        );
    }

    /**
     * Remove entrada de um veiculo especifico da lista de estatisticas.
     *
     * @param id id do veiculo a remover
     */
    private void removerVeiculoDaLista(int id) {
        for (int i = veiculosListModel.size() - 1; i >= 0; i--) {
            String linha = veiculosListModel.get(i);
            int fimId = linha.indexOf(' ', 1);
            if (fimId == -1) fimId = linha.indexOf('|', 1);
            if (fimId == -1) continue;
            try {
                int parsed = Integer.parseInt(linha.substring(1, fimId).trim());
                if (parsed == id) {
                    veiculosListModel.remove(i);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    /** Liga ou desliga o fluxo automatico de geracao de veiculos. */
    private void alternarFluxoFixo() {
        if (!servidoresAtivos || topologia == null || geradorCaminhos == null) {
            appendLog("Inicie os servidores antes de iniciar o Simulacao automatica.");
            return;
        }
        if (fluxoFixoAtivo) {
            fluxoFixoAtivo = false;
            fluxoFixoButton.setText("Simulacao automatica");
            appendLog("Simulacao automatica parada.");
            return;
        }
        fluxoFixoAtivo = true;
        fluxoFixoButton.setText("Parar Simulacao automatica");
        appendLog("Simulacao automatica iniciada.");
        fluxoFixoThread = new Thread(this::loopFluxoFixo, "Simulacao automatica");
        fluxoFixoThread.setDaemon(true);
        fluxoFixoThread.start();
    }

    /** Gera veiculos periodicamente enquanto o fluxo fixo estiver ativo. */
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

    /**
     * Cria e envia um veiculo partindo de uma entrada especifica.
     *
     * @param entradaSelecionada nome da entrada
     * @param tipo tipo de veiculo
     * @throws Exception em falha de envio
     */
    private void gerarVeiculoEntrada(String entradaSelecionada, VeiculoType tipo) throws Exception {
        if (entradaSelecionada == null) return;
        List<No> caminho = geradorCaminhos.gerarCaminho(entradaSelecionada);
        No origem = caminho.get(0);

        Veiculo veiculo = new Veiculo(tipo, origem, caminho);

        String rotaStr = caminho.stream().map(No::getName).collect(Collectors.joining("->"));
        appendLog("Gerado: Veiculo " + veiculo.getId() + " (" + tipo + ") Rota: " + rotaStr);

        registarMovimento(origem.getName(), veiculo);

        new EntradaSistema().enviarVeiculo(veiculo);
    }

    public record SemaforoStatus(boolean ativo, int fila) {}
}
