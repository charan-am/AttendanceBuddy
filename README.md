# Attendance Buddy

A Java Swing desktop calculator for attendance. It shows:

- your current attendance percentage,
- how many classes you can still miss without dropping below the required
  percentage,
- how many consecutive classes you must attend to recover, if you are below it.

Java only: no external libraries, no web technology, no database.

## Requirements

- JDK 17 or newer (developed and tested on JDK 26)
- No third-party dependencies

## Project structure

```
src/attendance/
├── AttendanceFrame.java      window and input handling (Stage 3)
├── AttendanceGauge.java      animated circular gauge (Stage 4)
├── DashboardCard.java        one result card (Stage 4)
├── AttendanceCalculator.java all formulas, no Swing code
└── AttendanceResult.java     the result object returned by the calculator
```

## Build

```bat
javac -d out src\attendance\*.java
```

## Run (after Stage 3)

```bat
java -cp out attendance.AttendanceFrame
```

## Run the calculation engine tests (Stage 2)

```bat
javac -d out src\attendance\*.java
java -cp out attendance.AttendanceCalculator
```

## Author

Built by Charan AM
