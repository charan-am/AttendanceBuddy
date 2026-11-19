package attendance;

/**
 * The calculation engine. It contains no Swing code, so it can be compiled
 * and tested on its own (run this class from the console to see the tests).
 */
public class AttendanceCalculator {

    /** Required attendance percentage used when the student does not type one. */
    public static final double DEFAULT_TARGET = 75.0;

    /** Tolerance for floating-point comparisons (75.000000001 == 75). */
    private static final double EPS = 1e-9;

    /**
     * Calculates everything the dashboard needs.
     *
     * @throws IllegalArgumentException if an input is not valid
     */
    public AttendanceResult calculate(int totalClasses, int absentClasses, double targetPercentage) {
        validate(totalClasses, absentClasses, targetPercentage);

        int attendedClasses = totalClasses - absentClasses;

        // Current attendance: the part of all conducted classes that was attended.
        double percentage = 100.0 * attendedClasses / totalClasses;

        int allowance = leaveAllowance(attendedClasses, totalClasses, percentage, targetPercentage);
        int recovery = classesToRecover(attendedClasses, totalClasses, percentage, targetPercentage);

        return new AttendanceResult(totalClasses, absentClasses, targetPercentage,
                percentage, allowance, recovery);
    }

    /** Rejects impossible numbers before any formula runs. */
    private void validate(int totalClasses, int absentClasses, double targetPercentage) {
        if (totalClasses <= 0) {
            throw new IllegalArgumentException("Total classes must be greater than zero.");
        }
        if (absentClasses < 0) {
            throw new IllegalArgumentException("Absent classes cannot be negative.");
        }
        if (absentClasses > totalClasses) {
            throw new IllegalArgumentException("Absent classes cannot exceed total classes.");
        }
        if (targetPercentage <= 0 || targetPercentage >= 100) {
            throw new IllegalArgumentException("Required percentage must be between 0 and 100.");
        }
    }

    /**
     * Maximum whole classes that can still be missed.
     *
     * <p>Missing k classes leaves attendedClasses unchanged but raises the
     * total by k, so we need:</p>
     *
     * <pre>100 * attended / (total + k) &gt;= target</pre>
     *
     * <p>Solving for k gives k &lt;= 100 * attended / target - total.
     * We round DOWN because only whole classes count, and return 0 if the
     * student is already below the target.</p>
     */
    private int leaveAllowance(int attended, int total, double percentage, double target) {
        if (percentage + EPS < target) {
            return 0;
        }
        int k = (int) Math.floor(100.0 * attended / target - total + EPS);
        return Math.max(k, 0);
    }

    /**
     * Consecutive classes needed to climb back to the target.
     *
     * <p>Attending x classes raises both the attended count and the total:</p>
     *
     * <pre>100 * (attended + x) / (total + x) &gt;= target</pre>
     *
     * <p>Solving for x gives x &gt;= (target * total - 100 * attended) / (100 - target).
     * We round UP because a student cannot attend part of a class.</p>
     */
    private int classesToRecover(int attended, int total, double percentage, double target) {
        if (percentage + EPS >= target) {
            return 0;
        }
        double exact = (target * total - 100.0 * attended) / (100.0 - target);
        return Math.max((int) Math.ceil(exact - EPS), 0);
    }

    /**
     * Runs the calculation engine tests on the console.
     * This is a test harness, not part of the GUI.
     */
    public static void main(String[] args) {
        AttendanceCalculator calculator = new AttendanceCalculator();
        int failures = 0;

        System.out.println("AttendanceCalculator test run");
        System.out.println("-----------------------------");

        // total, absent, target, expected %, expected allowance, expected recovery
        double[][] cases = {
                {100, 20, 75, 80.0, 6, 0},
                {50, 20, 75, 60.0, 0, 30},
                {4, 1, 75, 75.0, 0, 0},
                {100, 10, 75, 90.0, 20, 0},
                {10, 8, 75, 20.0, 0, 22},
                {100, 25, 80, 75.0, 0, 25}
        };

        for (double[] c : cases) {
            int total = (int) c[0];
            int absent = (int) c[1];
            double target = c[2];
            AttendanceResult r = calculator.calculate(total, absent, target);
            boolean ok = close(r.getPercentage(), c[3])
                    && r.getLeaveAllowance() == (int) c[4]
                    && r.getClassesToRecover() == (int) c[5];
            System.out.printf("%s  total=%d absent=%d target=%.0f -> %.1f%% (can miss %d, need %d)%n",
                    ok ? "PASS" : "FAIL", total, absent, target,
                    r.getPercentage(), r.getLeaveAllowance(), r.getClassesToRecover());
            if (!ok) {
                failures++;
            }
        }

        // Inputs that must be rejected.
        int[][] invalid = {{0, 0}, {10, -1}, {10, 11}};
        for (int[] c : invalid) {
            try {
                calculator.calculate(c[0], c[1], 75);
                System.out.printf("FAIL  total=%d absent=%d should be rejected%n", c[0], c[1]);
                failures++;
            } catch (IllegalArgumentException e) {
                System.out.printf("PASS  rejected: %s%n", e.getMessage());
            }
        }
        for (double target : new double[]{0, 100}) {
            try {
                calculator.calculate(10, 1, target);
                System.out.printf("FAIL  target=%.0f should be rejected%n", target);
                failures++;
            } catch (IllegalArgumentException e) {
                System.out.printf("PASS  rejected target %.0f: %s%n", target, e.getMessage());
            }
        }

        System.out.println("-----------------------------");
        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " TEST(S) FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    /** Compares two percentages with a small tolerance. */
    private static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }
}
