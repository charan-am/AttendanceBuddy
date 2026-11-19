package attendance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * One number on the dashboard, inside a rounded dark card.
 *
 * <p>The card knows nothing about attendance. It only displays a title and a
 * value, so the same class can show the total, attended, absent or leave
 * allowance numbers.</p>
 */
public class DashboardCard extends JPanel {

    private static final Color CARD = new Color(34, 37, 46);
    private static final Color TEXT = new Color(235, 238, 245);
    private static final Color MUTED = new Color(150, 158, 175);
    private static final String EMPTY = "\u2014";   // an em dash: value not calculated yet

    private final JLabel valueLabel = new JLabel(EMPTY);

    public DashboardCard(String title) {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);          // we paint our own rounded background
        setBackground(CARD);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setPreferredSize(new Dimension(150, 88));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(TEXT);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    /** Shows a new value, for example "6" or "100". */
    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /** Lets the window change the value colour for the current status. */
    public void setValueColor(Color color) {
        valueLabel.setForeground(color);
    }

    /** Returns to the "not calculated yet" state. */
    public void reset() {
        valueLabel.setText(EMPTY);
        valueLabel.setForeground(TEXT);
    }

    /** The text currently displayed; useful for automated checks. */
    public String getValue() {
        return valueLabel.getText();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Rounded rectangle = the card background.
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();

        super.paintComponent(g);   // let the labels draw on top
    }
}
