package attendance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * A circular attendance gauge drawn with Graphics2D.
 *
 * <p>The gauge stores only what it needs to draw: the percentage it is
 * currently showing, the percentage it is animating towards, and a colour.
 * A Swing {@link Timer} moves the displayed value closer to the target a few
 * times per second, which produces the smooth "count up" animation.</p>
 */
public class AttendanceGauge extends JComponent {

    // --- Fixed look of the gauge (kept here so the drawing code stays short) ---
    private static final Color TRACK = new Color(52, 57, 70);
    private static final Color NEUTRAL = new Color(120, 128, 145);
    private static final Color TEXT = new Color(235, 238, 245);
    private static final Color MUTED = new Color(150, 158, 175);

    private static final int PADDING = 14;            // space around the ring
    private static final float RING_WIDTH = 16f;      // thickness of the ring
    private static final double START_ANGLE = 90;     // 90 degrees = top of circle
    private static final double FULL_SWEEP = -360;    // negative = clockwise

    private final Timer timer;
    private double displayed;   // percentage currently drawn (animated)
    private double target;      // percentage we are animating towards
    private Color arcColor = NEUTRAL;

    public AttendanceGauge() {
        setPreferredSize(new Dimension(230, 230));
        setMinimumSize(new Dimension(180, 180));
        setOpaque(false);

        // 16 milliseconds is roughly 60 frames per second.
        timer = new Timer(16, event -> animate());
    }

    /** Starts animating towards the given percentage, using the given colour. */
    public void showValue(double percentage, Color color) {
        target = Math.max(0, Math.min(100, percentage));
        arcColor = color;
        timer.restart();
    }

    /** Animates back to an empty gauge. Used by the Clear button. */
    public void reset() {
        target = 0;
        arcColor = NEUTRAL;
        timer.restart();
    }

    /** The percentage currently drawn; useful for automated checks. */
    public double getDisplayedValue() {
        return displayed;
    }

    /** The colour of the ring; useful for automated checks. */
    public Color getArcColor() {
        return arcColor;
    }

    /** One animation step: move 16% of the remaining distance. */
    private void animate() {
        double difference = target - displayed;
        if (Math.abs(difference) < 0.05) {
            displayed = target;   // close enough: land exactly on the answer
            timer.stop();
        } else {
            displayed += difference * 0.16;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // A copy avoids changing the state of the Graphics object Swing owns.
        Graphics2D g2 = (Graphics2D) g.create();

        // Anti-aliasing = smooth, non-jagged edges for the ring and the text.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        // The ring is a square that fits inside the component, with padding.
        int size = Math.min(getWidth(), getHeight()) - 2 * PADDING;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setStroke(new BasicStroke(RING_WIDTH,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // 1. The full grey circle behind the value.
        g2.setColor(TRACK);
        g2.draw(new Arc2D.Double(x, y, size, size,
                START_ANGLE, FULL_SWEEP, Arc2D.OPEN));

        // 2. The coloured part: same circle, but only as long as the value.
        //    At 0% there is nothing to draw (a round cap would leave a dot).
        if (displayed > 0.1) {
            double sweep = FULL_SWEEP * displayed / 100.0;
            g2.setColor(arcColor);
            g2.draw(new Arc2D.Double(x, y, size, size,
                    START_ANGLE, sweep, Arc2D.OPEN));
        }

        drawCentreText(g2);
        g2.dispose();   // release the copy
    }

    /** Writes the percentage in the middle of the ring, with a small caption. */
    private void drawCentreText(Graphics2D g2) {
        String valueText = String.format("%.1f%%", displayed);

        // --- Big percentage, centred horizontally ---
        g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
        g2.setColor(TEXT);
        FontMetrics valueMetrics = g2.getFontMetrics();
        int valueX = (getWidth() - valueMetrics.stringWidth(valueText)) / 2;
        int valueBaseline = getHeight() / 2
                + (valueMetrics.getAscent() - valueMetrics.getDescent()) / 2 - 6;
        g2.drawString(valueText, valueX, valueBaseline);

        // --- Small caption just below it ---
        String caption = "ATTENDANCE";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(MUTED);
        FontMetrics captionMetrics = g2.getFontMetrics();
        int captionX = (getWidth() - captionMetrics.stringWidth(caption)) / 2;
        int captionBaseline = valueBaseline + valueMetrics.getDescent()
                + 10 + captionMetrics.getAscent();
        g2.drawString(caption, captionX, captionBaseline);
    }
}
