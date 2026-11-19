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
    private static final Color BORDER = new Color(58, 63, 77);
    private static final Color TEXT = new Color(235, 238, 245);
    private static final Color MUTED = new Color(150, 158, 175);
    private static final Color FOOTER_TEXT = new Color(110, 117, 133);
    private static final Color ACCENT = new Color(88, 166, 255);

    // Status colours follow the required-target rule (see statusColor).
    private static final Color RED = new Color(239, 83, 80);
    private static final Color YELLOW = new Color(255, 193, 7);
    private static final Color GREEN = new Color(76, 175, 80);

    // Soft background tints for the status panel.
    private static final Color RED_TINT = new Color(52, 32, 36);
    private static final Color YELLOW_TINT = new Color(54, 47, 26);
    private static final Color GREEN_TINT = new Color(28, 47, 37);

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
    private final DashboardCard absentCard = new DashboardCard("ABSENT");
    private final DashboardCard allowanceCard = new DashboardCard("CAN STILL MISS");

    // Status panel: a coloured title and a one-line explanation.
    private final JPanel statusPanel = new JPanel(new BorderLayout(0, 3));
    private final JLabel statusLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();

    public AttendanceFrame() {
        super("Attendance Buddy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContent());
        setSize(1000, 620);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);   // centre the window on the screen
    }

    /** Builds the header, the main area (inputs + results) and the footer. */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    /** Title and one-line subtitle, with a thin separator underneath. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(42, 46, 57)),
                BorderFactory.createEmptyBorder(0, 0, 14, 0)));

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

    /** Inputs on the left, gauge, cards and status panel on the right. */
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(18, 0));
        main.setBackground(BACKGROUND);

        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBackground(BACKGROUND);
        inputWrapper.setPreferredSize(new Dimension(330, 0));
        inputWrapper.add(buildForm(), BorderLayout.NORTH);

        main.add(inputWrapper, BorderLayout.WEST);
        main.add(buildResults(), BorderLayout.CENTER);
        return main;
    }

    /** The input card with its three fields and two buttons. */
    private JPanel buildForm() {
        JPanel form = new JPanel(new BorderLayout(0, 16));
        form.setBackground(PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));

        JPanel inputs = new JPanel(new GridLayout(3, 2, 12, 12));
        inputs.setBackground(PANEL);
        inputs.add(labelFor("Total classes"));
        inputs.add(fieldFor(totalField));
        inputs.add(labelFor("Classes absent"));
        inputs.add(fieldFor(absentField));
        inputs.add(labelFor("Required (%)"));
        inputs.add(fieldFor(targetField));

        JLabel formTitle = new JLabel("ENTER YOUR NUMBERS");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        formTitle.setForeground(MUTED);

        JPanel formTop = new JPanel(new BorderLayout(0, 12));
        formTop.setBackground(PANEL);
        formTop.add(formTitle, BorderLayout.NORTH);
        formTop.add(inputs, BorderLayout.CENTER);
        form.add(formTop, BorderLayout.NORTH);

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

    /** Gauge and cards on top, the status message underneath. */
    private JPanel buildResults() {
        JPanel results = new JPanel(new BorderLayout(0, 14));
        results.setBackground(BACKGROUND);

        JPanel topRow = new JPanel(new BorderLayout(18, 0));
        topRow.setBackground(BACKGROUND);

        JPanel gaugeHolder = new JPanel(new BorderLayout());
        gaugeHolder.setBackground(BACKGROUND);
        gaugeHolder.add(gauge, BorderLayout.CENTER);
        topRow.add(gaugeHolder, BorderLayout.WEST);

        JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
        cards.setBackground(BACKGROUND);
        cards.add(totalCard);
        cards.add(attendedCard);
        cards.add(absentCard);
        cards.add(allowanceCard);

        JPanel cardHolder = new JPanel(new BorderLayout());
        cardHolder.setBackground(BACKGROUND);
        cardHolder.add(cards, BorderLayout.CENTER);
        topRow.add(cardHolder, BorderLayout.CENTER);

        results.add(topRow, BorderLayout.CENTER);
        results.add(buildStatusPanel(), BorderLayout.SOUTH);
        return results;
    }

    /** The coloured bar that explains the current status. */
    private JPanel buildStatusPanel() {
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detailLabel.setForeground(MUTED);

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(detailLabel, BorderLayout.CENTER);
        showNeutralStatus();
        return statusPanel;
    }

    /** A small, quiet credit line at the bottom of the window. */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BACKGROUND);

        JLabel credit = new JLabel("Built by Charan AM");
        credit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        credit.setForeground(FOOTER_TEXT);

        footer.add(credit, BorderLayout.EAST);
        return footer;
    }

    /** Reads the fields, asks the engine, shows the answer. */
    private void onCalculate() {
        try {
            int total = Integer.parseInt(totalField.getText().trim());
            int absent = Integer.parseInt(absentField.getText().trim());
            double target = Double.parseDouble(targetField.getText().trim());

            AttendanceResult result = calculator.calculate(total, absent, target);
            updateDashboard(result);
            showResultStatus(result);
        } catch (NumberFormatException ex) {
            // Empty or non-numeric text.
            resetDashboard();
            showErrorStatus("Please enter whole numbers for classes "
                    + "and a number for the required percentage.");
        } catch (IllegalArgumentException ex) {
            // The engine rejected the values, for example absent > total.
            resetDashboard();
            showErrorStatus(ex.getMessage());
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

    /** Chooses the status text, colour and tint that belong to the result. */
    private void showResultStatus(AttendanceResult result) {
        Color color = statusColor(result);
        double target = result.getTargetPercentage();

        if (color == RED) {
            styleStatus("ATTENDANCE BELOW TARGET", RED, RED_TINT, String.format(
                    "Attend %d consecutive %s to reach %.1f%%. "
                    + "Remaining leave allowance is 0.",
                    result.getClassesToRecover(),
                    classes(result.getClassesToRecover()),
                    target));
        } else if (color == YELLOW) {
            styleStatus("MAINTAIN ATTENDANCE", YELLOW, YELLOW_TINT, String.format(
                    "You can still miss %d %s and stay at or above %.1f%%. "
                    + "Do not skip too many in a row.",
                    result.getLeaveAllowance(),
                    classes(result.getLeaveAllowance()),
                    target));
        } else {
            styleStatus("SAFE", GREEN, GREEN_TINT, String.format(
                    "You can still miss %d %s and stay above %.1f%%. "
                    + "Your attendance has a comfortable buffer.",
                    result.getLeaveAllowance(),
                    classes(result.getLeaveAllowance()),
                    target));
        }
    }

    /** The state shown before the first calculation and after Clear. */
    private void showNeutralStatus() {
        styleStatus("READY", MUTED, PANEL,
                "Enter your numbers, then press Calculate.");
    }

    /** Invalid input: red title with the engine's explanation. */
    private void showErrorStatus(String message) {
        styleStatus("CHECK YOUR INPUTS", RED, RED_TINT, message);
    }

    /** Applies one consistent look to the status panel in every state. */
    private void styleStatus(String title, Color color, Color background, String detail) {
        statusLabel.setText(title);
        statusLabel.setForeground(color);
        detailLabel.setText(detail);
        statusPanel.setBackground(background);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
    }

    /** "1 class" reads better than "1 classes". */
    private String classes(int count) {
        return count == 1 ? "class" : "classes";
    }

    /** Empties the fields, restores the default target and clears results. */
    private void onClear() {
        totalField.setText("");
        absentField.setText("");
        targetField.setText("75");
        resetDashboard();
        showNeutralStatus();
        totalField.requestFocusInWindow();
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
                BorderFactory.createLineBorder(BORDER),
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
