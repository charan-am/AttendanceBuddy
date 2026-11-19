# Attendance Buddy

A beginner-friendly Java Swing desktop application that calculates overall
attendance and tells you how many classes you can still miss, or how many
consecutive classes you must attend to recover.

Built with **Java and Swing only**: no external libraries, no database, no web
technology.

## Features

- Enter total classes conducted, classes absent and the required percentage.
- Animated circular gauge showing the exact attendance percentage.
- Four dashboard cards: total classes, attended, absent and remaining leave
  allowance.
- Colour-coded status with a clear explanation:
  - **Red**: below the required target; shows how many consecutive classes
    are needed and that the leave allowance is 0.
  - **Yellow**: at the target or up to 10 percentage points above it; shows
    how many classes can still be missed.
  - **Green**: more than 10 points above the target; shows the remaining
    allowance.
- Input validation with readable messages.
- Clear button that resets both the inputs and the results.
- Dark, resizable window with a small footer credit.

For the default target of 75%:

| Attendance | Colour |
|------------|--------|
| Below 75% | Red |
| 75% to 85% inclusive | Yellow |
| Above 85% | Green |

The same rule is applied relative to any target the user enters. A percentage
exactly equal to the target is never "below target", so it is yellow.

## Requirements

- JDK 17 or newer (developed and tested on JDK 26)
- No third-party dependencies

## Project structure

```
src/attendance/
├── AttendanceFrame.java      window, layout and button handling
├── AttendanceGauge.java      animated circular gauge (Graphics2D + Timer)
├── DashboardCard.java        one rounded result card
├── AttendanceCalculator.java all attendance formulas (no Swing code)
└── AttendanceResult.java     immutable result object returned by the calculator
```

`AttendanceFrame` also contains `main`. The five classes keep the screen code
separate from the calculation code.

## Build

From the project folder:

```bat
javac -d out src\attendance\*.java
```

## Run

```bat
java -cp out attendance.AttendanceFrame
```

## Run the calculation tests

The calculator contains a small self-test that checks the six sample
scenarios and the invalid inputs:

```bat
javac -d out src\attendance\*.java
java -cp out attendance.AttendanceCalculator
```

Expected final line: `ALL TESTS PASSED`.

## How to use

1. Enter the total number of classes conducted so far.
2. Enter how many of them you were absent for.
3. Enter the required attendance percentage (75 by default).
4. Press **Calculate**.
5. Press **Clear** to empty the inputs and reset the dashboard.

## How the numbers are calculated

Let `T` be total classes, `A` absent classes, `C = T - A` attended classes and
`p` the required percentage.

- Attendance percentage: `100 * C / T`
- Leave allowance: the largest whole number `k` such that
  `100 * C / (T + k) >= p`. If you are already below `p`, the allowance is 0.
- Classes to recover: the smallest whole number `k` such that
  `100 * (C + k) / (T + k) >= p`. If you are already at or above `p`, this is 0.

The formulas use a very small tolerance (`1e-9`) so floating-point rounding
cannot turn an exact boundary (for example 75.0%) into a wrong answer.

## Known limitations

- Attendance is calculated overall, not subject by subject.
- Classes are whole numbers.
- Nothing is saved: results exist only while the application is open.
- The required percentage must be greater than 0 and less than 100.

## Author

Built by Charan AM
