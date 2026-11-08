package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// Tune F first with P at 0, then raise P. Copy the final values into PracticeShooter
@TeleOp(name="PracticeShooterTuner", group="Linear OpMode")

public class PracticeShooterTuner extends LinearOpMode {
    private final PracticeShooter shooter = new PracticeShooter();

    double highVelocity = PracticeShooter.TARGET_VELOCITY;
    double lowVelocity = highVelocity * 0.6;
    double targetVelocity = highVelocity;

    double[] stepSizes = {10, 1, 0.1, 0.01, 0.001};
    int stepIndex = 1;

    double P = 0;
    double F = 0;

    enum shooterMode {
        velocity,
        fullPower,
        off
    }

    shooterMode mode = shooterMode.velocity;

    public void runOpMode() {
        shooter.init(hardwareMap);
        shooter.setPIDF(P, F);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            double oldP = P;
            double oldF = F;

            // Y goes back to velocity control after X or A, otherwise it switches high/low
            if (gamepad1.yWasPressed()) {
                if (mode == shooterMode.velocity) {
                    targetVelocity = (targetVelocity == highVelocity) ? lowVelocity : highVelocity;
                } else {
                    mode = shooterMode.velocity;
                }
            }

            if (gamepad1.xWasPressed()) {
                mode = shooterMode.fullPower;
            }
            if (gamepad1.aWasPressed()) {
                mode = shooterMode.off;
            }

            if (gamepad1.bWasPressed()) {
                stepIndex = (stepIndex + 1) % stepSizes.length;
            }

            if (gamepad1.dpadRightWasPressed()) {
                F += stepSizes[stepIndex];
            }
            if (gamepad1.dpadLeftWasPressed()) {
                F = Math.max(0, F - stepSizes[stepIndex]);
            }
            if (gamepad1.dpadUpWasPressed()) {
                P += stepSizes[stepIndex];
            }
            if (gamepad1.dpadDownWasPressed()) {
                P = Math.max(0, P - stepSizes[stepIndex]);
            }

            if (P != oldP || F != oldF) {
                shooter.setPIDF(P, F);
            }
            switch (mode) {
                case velocity:
                    shooter.setVelocity(targetVelocity);
                    break;
                case fullPower:
                    shooter.setPower(1);
                    break;
                case off:
                    shooter.setPower(0);
                    break;
            }

            double velocity = shooter.getVelocity();

            telemetry.addData("Mode", mode);
            telemetry.addData("Target", "%.0f ticks/s", targetVelocity);
            telemetry.addData("Velocity", "%.0f ticks/s", velocity);
            telemetry.addData("Error", "%.0f ticks/s", targetVelocity - velocity);
            telemetry.addData("F (dpad left/right)", "%.4f", F);
            telemetry.addData("P (dpad up/down)", "%.4f", P);
            telemetry.addData("Step (B)", "%.4f", stepSizes[stepIndex]);
            telemetry.addLine("Y switches high/low target, X full power, A off");
            telemetry.update();
        }
    }
}
