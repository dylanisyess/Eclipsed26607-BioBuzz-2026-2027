package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="teleop", group="Linear OpMode")

public class PracticeTeleop extends LinearOpMode {
    private final PracticeRobot Robot = new PracticeRobot();

    double vx, vy, omega;

    public void runOpMode() {
        Robot.init(hardwareMap);
//        telemetry.addData("FL lamprey", Robot.frontLeftLamprey == null ? "NULL" : "OK");
        telemetry.update();

        Robot.intaking = false;

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();

        Robot.runtime.reset();

        while (opModeIsActive()) {
            vx = gamepad1.left_stick_x;
            vy = -gamepad1.left_stick_y;
            omega = gamepad1.right_stick_x;

            Robot.left.setPower(vx);
            Robot.right.setPower(vy);

            if (gamepad1.y) {
                Robot.intake.setPower(1);
            }
            if (gamepad1.b) {
                Robot.intake.setPower(0.8);
            }
            if (gamepad1.a) {
                Robot.intake.setPower(0.6);
            }
            if (gamepad1.x) {
                Robot.intake.setPower(0);
            }

            if (gamepad1.right_bumper) {
                Robot.intake.setPower(-0.5);
            }

            if (gamepad1.dpad_up) {
                Robot.shooter.setPower(1);
            }
            if (gamepad1.dpad_right) {
                Robot.shooter.setPower(0.8);
            }
            if (gamepad1.dpad_down) {
                Robot.shooter.setPower(0.6);
            }
            if (gamepad1.dpad_left) {
                Robot.shooter.setPower(0);
            }

            if (gamepad1.left_bumper) {
                Robot.shooter.setPower(-0.5);
            }

            telemetry.update();
        }
    }
}