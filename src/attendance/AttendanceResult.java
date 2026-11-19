package attendance;

/**
 * One attendance calculation, stored as plain data.
 *
 * <p>The fields are private and final, so a result object cannot be changed
 * after it is created. That is called an <b>immutable</b> object: it is safe
 * to pass around, and every screen that reads it sees the same values.</p>
 */
public class AttendanceResult {

    private final int totalClasses;      // classes conducted so far
    private final int absentClasses;     // classes missed so far
    private final int attendedClasses;   // totalClasses - absentClasses
    private final double targetPercentage;
    private final double percentage;     // current attendance, e.g. 68.5
    private final int leaveAllowance;    // more whole classes that can be missed
    private final int classesToRecover;  // consecutive classes needed to reach target

    public AttendanceResult(int totalClasses, int absentClasses, double targetPercentage,
                            double percentage, int leaveAllowance, int classesToRecover) {
        this.totalClasses = totalClasses;
        this.absentClasses = absentClasses;
        this.attendedClasses = totalClasses - absentClasses;
        this.targetPercentage = targetPercentage;
        this.percentage = percentage;
        this.leaveAllowance = leaveAllowance;
        this.classesToRecover = classesToRecover;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getAbsentClasses() {
        return absentClasses;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public double getTargetPercentage() {
        return targetPercentage;
    }

    public double getPercentage() {
        return percentage;
    }

    public int getLeaveAllowance() {
        return leaveAllowance;
    }

    public int getClassesToRecover() {
        return classesToRecover;
    }

    /** True when the student is strictly below the required percentage. */
    public boolean isBelowTarget() {
        // 1e-9 absorbs tiny floating-point errors, so 74.999999999 counts as 75.
        return percentage + 1e-9 < targetPercentage;
    }

    /** Percentage formatted with one decimal place, for labels: "78.6%". */
    public String getPercentageText() {
        return String.format("%.1f%%", percentage);
    }
}
