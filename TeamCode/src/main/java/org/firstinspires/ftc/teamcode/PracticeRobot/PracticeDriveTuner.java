package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// Before pressing start, push the robot by hand: both distances should change the same way,
// and turning it counterclockwise should raise the heading. Tune in PracticeRobot
@TeleOp(name="PracticeDriveTuner", group="Linear OpMode")

public class PracticeDriveTuner extends LinearOpMode {
    private static final double TEST_DISTANCE = 60; // cm

    private final PracticeRobot Robot = new PracticeRobot();

    double targetHeading = 0;
    double lastMoveTime = 0;

    public void runOpMode() {
        Robot.init(hardwareMap);
        Robot.resetHeading();

        while (opModeInInit()) {
            displayTelemetry();
        }

        Robot.resetHeading();

        while (opModeIsActive()) {
            double loopStart = getRuntime();

            if (gamepad1.yWasPressed()) {
                Robot.driveDistance(this, TEST_DISTANCE);
            }
            if (gamepad1.aWasPressed()) {
                Robot.driveDistance(this, -TEST_DISTANCE);
            }
            if (gamepad1.xWasPressed()) {
                targetHeading = 90;
                Robot.turnToHeading(this, targetHeading);
            }
            if (gamepad1.bWasPressed()) {
                targetHeading = 0;
                Robot.turnToHeading(this, targetHeading);
            }

            // Moves block, so a long loop means one just ran
            double loopTime = getRuntime() - loopStart;
            if (loopTime > 0.1) {
                lastMoveTime = loopTime;
            }

            displayTelemetry();
        }
    }

    private void displayTelemetry() {
        telemetry.addData("Heading", "%.1f deg (target %.0f)", Robot.getHeading(), targetHeading);
        telemetry.addData("Left distance", "%.1f cm", Robot.getLeftDistance());
        telemetry.addData("Right distance", "%.1f cm", Robot.getRightDistance());
        // 3 s for a turn or 5 s for a drive means it timed out instead of finishing
        telemetry.addData("Last move", "%.1f s", lastMoveTime);
        telemetry.addLine("Y/A drive +/-60 cm, X turn to 90, B turn to 0");
        telemetry.update();
    }
}
