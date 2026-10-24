package org.firstinspires.ftc.teamcode.PracticeRobot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="PracticeAuto", group="Linear OpMode")

public class PracticeAuto extends LinearOpMode {
    private final PracticeRobot Robot = new PracticeRobot();
    double timer;

    double vx, vy, omega;

    public void runOpMode() {
        Robot.init(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        sleep(500);

        Robot.left.setPower(0.5);
        Robot.right.setPower(0.5);

        sleep(650);

        Robot.left.setPower(0);
        Robot.right.setPower(0);

        sleep(300);

        Robot.shooter.setPower(1);

        sleep(2500);

        Robot.intake.setPower(0.4);

        sleep(3000);

        Robot.shooter.setPower(0);
        Robot.intake.setPower(0);

        sleep(400);

        Robot.left.setPower(0.7);
        Robot.right.setPower(0);

        sleep(800);

        Robot.left.setPower(1);
        Robot.right.setPower(1);

        sleep(750);

        Robot.left.setPower(0);
        Robot.right.setPower(0);

        sleep(250);

        Robot.left.setPower(0);
        Robot.right.setPower(0.7);

        sleep(500);

        Robot.left.setPower(1);
        Robot.right.setPower(1);

        sleep(1500);

        Robot.left.setPower(0);
        Robot.right.setPower(0);

    }
}