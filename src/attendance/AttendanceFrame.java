package attendance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * The application window.
 *
 * <p>This class only handles the screen: it reads text from the fields,
 * asks {@link AttendanceCalculator} for a result, and displays it. No
 * attendance formula lives here.</p>
 */
public class AttendanceFrame extends JFrame {

    // --- Dark theme colours, kept in one place so the look is easy to change ---
    private static final Color BACKGROUND = new Color(24, 26, 32);
    private static final Color PANEL = new Color(34, 37, 46);
    private static final Color FIELD = new Color(45, 49, 60);
    private static final Color TEXT = new Color(235, 238, 245);
    private static final Color MUTED = new Color(150, 158, 175);
    private static final Color ACCENT = new Color(88, 166, 255);
    private static final Color ERROR = new Color(255, 138, 128);

    // Status colours follow the required-target rule (see statusColor).
    private static final Color RED = new Color(239, 83, 80);
    private static final Color YELLOW = new Color(255, 193, 7);
    private static final Color GREEN = new Color(76, 175, 80);

    /** One engine object, reused for every calculation. */
    private final AttendanceCalculator calculator = new AttendanceCalculator();

    // Input fields are fields (not local variables) so every method can reach them.
    private final JTextField totalField = new JTextField();
    private final JTextField absentField = new JTextField();
    private final JTextField targetField = new JTextField("75");

    // Dashboard pieces.
    private final AttendanceGauge gauge = new AttendanceGauge();
    private final DashboardCard totalCard = new DashboardCard("TOTAL CLASSES");
    private final DashboardCard attendedCard = new DashboardCard("ATTENDED");
    private final DashboardCard absentCard = new DashboardCard("ABSENT / BUNKED");
    private final DashboardCard allowanceCard = new DashboardCard("CAN STILL MISS");

    private final JLabel messageLabel =
            new JLabel("Enter your numbers, then press Calculate.");

    public AttendanceFrame() {
        super("Attendance Buddy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContent());
        setSize(980, 600);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);   // centre the window on the screen
    }

    /** Builds the header, the input card, the dashboard and the message bar. */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        messageLabel.setForeground(MUTED);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(messageLabel, BorderLayout.SOUTH);
        return root;
    }

    /** Title and one-line subtitle. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(BACKGROUND);

        JLabel title = new JLabel("Attendance Buddy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Know your percentage. Know how many classes you can still miss.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        header.add(title);
        header.add(subtitle);
        return header;
    }

    /** Inputs on the left, gauge and result cards on the right. */
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(18, 0));
        main.setBackground(BACKGROUND);

        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBackground(BACKGROUND);
        inputWrapper.setPreferredSize(new Dimension(330, 0));
        inputWrapper.add(buildForm(), BorderLayout.CENTER);

        main.add(inputWrapper, BorderLayout.WEST);
        main.add(buildDashboard(), BorderLayout.CENTER);
        return main;
    }

    /** The input card with its three fields and two buttons. */
    private JPanel buildForm() {
        JPanel form = new JPanel(new BorderLayout(0, 16));
        form.setBackground(PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inputs = new JPanel(new GridLayout(3, 2, 12, 12));
        inputs.setBackground(PANEL);
        inputs.add(labelFor("Total classes"));
        inputs.add(fieldFor(totalField));
        inputs.add(labelFor("Classes absent"));
        inputs.add(fieldFor(absentField));
        inputs.add(labelFor("Required (%)"));
        inputs.add(fieldFor(targetField));
        form.add(inputs, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(PANEL);

        JButton calculateButton = buttonFor("Calculate", ACCENT);
        JButton clearButton = buttonFor("Clear", FIELD);
        calculateButton.addActionListener(e -> onCalculate());
        clearButton.addActionListener(e -> onClear());

        buttons.add(clearButton);
        buttons.add(calculateButton);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    /** Gauge on the left, four result cards in a 2 x 2 grid on the right. */
    private JPanel buildDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(18, 0));
        dashboard.setBackground(BACKGROUND);

        JPanel gaugeHolder = new JPanel(new BorderLayout());
        gaugeHolder.setBackground(BACKGROUND);
        gaugeHolder.add(gauge, BorderLayout.CENTER);
        dashboard.add(gaugeHolder, BorderLayout.WEST);

        JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
        cards.setBackground(BACKGROUND);
        cards.add(totalCard);
        cards.add(attendedCard);
        cards.add(absentCard);
        cards.add(allowanceCard);

        JPanel cardHolder = new JPanel(new BorderLayout());
        cardHolder.setBackground(BACKGROUND);
        cardHolder.add(cards, BorderLayout.CENTER);
        dashboard.add(cardHolder, BorderLayout.CENTER);
        return dashboard;
    }

    /** Reads the fields, asks the engine, shows the answer. */
    private void onCalculate() {
        try {
            int total = Integer.parseInt(totalField.getText().trim());
            int absent = Integer.parseInt(absentField.getText().trim());
            double target = Double.parseDouble(targetField.getText().trim());

            AttendanceResult result = calculator.calculate(total, absent, target);
            updateDashboard(result);

            messageLabel.setForeground(statusColor(result));
            messageLabel.setText(String.format(
                    "%s attendance  ·  %d of %d attended  ·  %d classes can still be missed",
                    result.getPercentageText(),
                    result.getAttendedClasses(),
                    result.getTotalClasses(),
                    result.getLeaveAllowance()));
        } catch (NumberFormatException ex) {
            // Empty or non-numeric text.
            resetDashboard();
            showError("Please enter whole numbers for classes and a number for the target.");
        } catch (IllegalArgumentException ex) {
            // The engine rejected the values, for example absent > total.
            resetDashboard();
            showError(ex.getMessage());
        }
    }

    /** Copies every value from the result into the gauge and the cards. */
    private void updateDashboard(AttendanceResult result) {
        Color color = statusColor(result);
        gauge.showValue(result.getPercentage(), color);
        totalCard.setValue(String.valueOf(result.getTotalClasses()));
        attendedCard.setValue(String.valueOf(result.getAttendedClasses()));
        absentCard.setValue(String.valueOf(result.getAbsentClasses()));
        allowanceCard.setValue(String.valueOf(result.getLeaveAllowance()));
        allowanceCard.setValueColor(color);
    }

    /** Empties the dashboard (used by Clear and by invalid input). */
    private void resetDashboard() {
        gauge.reset();
        totalCard.reset();
        attendedCard.reset();
        absentCard.reset();
        allowanceCard.reset();
    }

    /**
     * Decides the status colour from the percentage and the required target.
     * Below target = red; target to target + 10 inclusive = yellow;
     * above target + 10 = green.
     */
    private Color statusColor(AttendanceResult result) {
        double percentage = result.getPercentage();
        double target = result.getTargetPercentage();

        if (percentage + 1e-9 < target) {
            return RED;
        }
        if (percentage <= target + 10 + 1e-9) {
            return YELLOW;
        }
        return GREEN;
    }

    /** Empties the fields and restores the default target. */
    private void onClear() {
        totalField.setText("");
        absentField.setText("");
        targetField.setText("75");
        resetDashboard();
        messageLabel.setForeground(MUTED);
        messageLabel.setText("Enter your numbers, then press Calculate.");
        totalField.requestFocusInWindow();
    }

    private void showError(String message) {
        messageLabel.setForeground(ERROR);
        messageLabel.setText(message);
    }

    // --- Small helpers that keep the build methods short and consistent ---

    private JLabel labelFor(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(MUTED);
        return label;
    }

    private JTextField fieldFor(JTextField field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(58, 63, 77)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JButton buttonFor(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(background.equals(FIELD) ? TEXT : Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** Entry point: runs on the Event Dispatch Thread, as Swing requires. */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // If the look and feel cannot be set, the default one is still usable.
        }
        SwingUtilities.invokeLater(() -> new AttendanceFrame().setVisible(true));
    }
}
