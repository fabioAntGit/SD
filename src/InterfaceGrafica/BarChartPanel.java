package src.InterfaceGrafica;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Painel simples para desenhar graficos de barras sem dependencias externas.
 */
public class BarChartPanel extends JPanel {
    private Map<String, Double> data = new LinkedHashMap<>();
    private String title;
    private String subtitle = "";
    private final Color barColor = new Color(60, 120, 210);
    private final Color textColor = new Color(30, 30, 30);
    private final Color gridColor = new Color(190, 190, 190);

    public BarChartPanel(String title) {
        this.title = title;
        setPreferredSize(new Dimension(260, 200));
        setBackground(new Color(245, 245, 245));
    }

    /**
     * Atualiza os valores a representar no grafico.
     *
     * @param novaData mapa com labels e valores
     * @param novoSubtitle texto auxiliar exibido por baixo do titulo
     */
    public void setData(Map<String, Double> novaData, String novoSubtitle) {
        SwingUtilities.invokeLater(() -> {
            this.data = novaData != null ? new LinkedHashMap<>(novaData) : new LinkedHashMap<>();
            if (novoSubtitle != null) {
                this.subtitle = novoSubtitle;
            }
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(textColor);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(title, 12, 20);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        if (!subtitle.isEmpty()) {
            g2.drawString(subtitle, 12, 38);
        }

        if (data.isEmpty()) {
            g2.drawString("Sem dados ainda.", 12, h / 2);
            return;
        }

        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (max <= 0) max = 1.0;

        int paddingLeft = 40;
        int paddingBottom = 30;
        int topOffset = 60;
        int chartWidth = w - paddingLeft - 20;
        int chartHeight = h - paddingBottom - topOffset;
        int baseY = topOffset + chartHeight;

        g2.setColor(gridColor);
        g2.drawLine(paddingLeft, topOffset, paddingLeft, baseY);
        g2.drawLine(paddingLeft, baseY, paddingLeft + chartWidth, baseY);

        int n = data.size();
        int barSpacing = 12;
        int barWidth = Math.max(10, (chartWidth - barSpacing * (n - 1)) / n);
        int idx = 0;

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            int barHeight = (int) Math.round((value / max) * chartHeight);
            int x = paddingLeft + idx * (barWidth + barSpacing);
            int y = baseY - barHeight;

            g2.setColor(barColor);
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);
            g2.setColor(gridColor);
            g2.drawRoundRect(x, y, barWidth, barHeight, 6, 6);

            String label = entry.getKey();
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            int labelX = x + (barWidth - labelWidth) / 2;
            g2.setColor(textColor);
            g2.drawString(label, labelX, baseY + 14);

            String valorTxt = String.format("%.1f", value);
            int valorWidth = g2.getFontMetrics().stringWidth(valorTxt);
            int valorX = x + (barWidth - valorWidth) / 2;
            g2.drawString(valorTxt, valorX, y - 6);

            idx++;
        }
    }
}
